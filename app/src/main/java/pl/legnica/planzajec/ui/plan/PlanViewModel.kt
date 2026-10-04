package pl.legnica.planzajec.ui.plan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.data.preferences.FilterMode
import pl.legnica.planzajec.data.preferences.UserPreferencesRepository
import pl.legnica.planzajec.data.preferences.ViewMode
import pl.legnica.planzajec.data.repository.ScheduleRepository
import pl.legnica.planzajec.notification.ScheduleAlarmReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PlanUiState(
    val groupCode: String = "",
    val subgroup: String? = null,
    val availableSubgroups: List<String> = emptyList(),
    val lessons: List<LessonEntity> = emptyList(),
    val viewMode: ViewMode = ViewMode.UPCOMING,
    val filterMode: FilterMode = FilterMode.ALL,
    val mergeBlocks: Boolean = false,
    val isRefreshing: Boolean = false,
    val lastUpdated: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val preferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanUiState(isRefreshing = true))
    val uiState: StateFlow<PlanUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow
                .flatMapLatest { prefs ->
                    val groupCode = prefs.selectedGroupCode ?: return@flatMapLatest flowOf(null)
                    val subgroup = prefs.selectedSubgroup

                    combine(
                        repository.getLessons(groupCode, subgroup),
                        repository.getMetadata(groupCode)
                    ) { lessonsList, metadata ->
                        Triple(prefs, lessonsList, metadata)
                    }
                }
                .collect { tuple ->
                    if (tuple == null) return@collect
                    val (prefs, lessonsList, metadata) = tuple
                    val groupCode = prefs.selectedGroupCode.orEmpty()
                    val subgroup = prefs.selectedSubgroup

                    val lastUpdatedFormatted = if (metadata != null && metadata.lastSyncedMillis > 0) {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(metadata.lastSyncedMillis))
                    } else ""

                    val subgroups = repository.getAvailableSubgroups(groupCode)

                    val filteredByMode = when (prefs.filterMode) {
                        FilterMode.CAMPUS_ONLY -> lessonsList.filter { !it.isOnline }
                        FilterMode.ONLINE_ONLY -> lessonsList.filter { it.isOnline }
                        FilterMode.ALL -> lessonsList
                    }

                    val finalLessons = if (prefs.mergeConsecutiveBlocks) {
                        mergeConsecutiveLessons(filteredByMode)
                    } else {
                        filteredByMode
                    }

                    _uiState.update {
                        it.copy(
                            groupCode = groupCode,
                            subgroup = subgroup,
                            viewMode = prefs.viewMode,
                            filterMode = prefs.filterMode,
                            mergeBlocks = prefs.mergeConsecutiveBlocks,
                            lessons = finalLessons,
                            availableSubgroups = subgroups,
                            lastUpdated = lastUpdatedFormatted,
                            isRefreshing = false
                        )
                    }

                    if (finalLessons.isNotEmpty()) {
                        ScheduleAlarmReceiver.triggerImmediateUpdate(context)
                    }
                }
        }
    }

    private fun mergeConsecutiveLessons(lessons: List<LessonEntity>): List<LessonEntity> {
        if (lessons.isEmpty()) return emptyList()
        val sorted = lessons.sortedWith(compareBy({ it.date }, { it.startTime }))
        val result = mutableListOf<LessonEntity>()
        var current: LessonEntity? = null

        for (next in sorted) {
            if (current == null) {
                current = next
                continue
            }

            val isSameDay = current.date == next.date
            val minutesBetween = java.time.Duration.between(current.endTime, next.startTime).toMinutes()
            val isContiguousTime = minutesBetween in 0..25
            val isSameSubject = current.subjectShort.equals(next.subjectShort, ignoreCase = true) ||
                    current.subjectFull.equals(next.subjectFull, ignoreCase = true)
            val isSameType = current.type == next.type
            val isSameRoom = current.room.equals(next.room, ignoreCase = true)
            val isSameTeacher = current.teacher.equals(next.teacher, ignoreCase = true)
            val isSameOnline = current.isOnline == next.isOnline
            val isSameSubgroup = current.subgroup.equals(next.subgroup, ignoreCase = true) ||
                    current.subgroup.isNullOrBlank() || next.subgroup.isNullOrBlank()

            if (isSameDay && isContiguousTime && isSameSubject && isSameType && isSameRoom && isSameTeacher && isSameOnline && isSameSubgroup) {
                current = current.copy(endTime = next.endTime)
            } else {
                result.add(current)
                current = next
            }
        }
        if (current != null) {
            result.add(current)
        }
        return result
    }

    fun refresh() {
        val groupCode = _uiState.value.groupCode
        if (groupCode.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            repository.refreshSchedule(groupCode)
                .onSuccess {
                    ScheduleAlarmReceiver.triggerImmediateUpdate(context)
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = "Nie udało się zaktualizować planu. Działasz w trybie offline."
                        )
                    }
                }
        }
    }

    fun selectSubgroup(subgroup: String?) {
        viewModelScope.launch {
            preferencesRepository.updateSubgroup(subgroup)
            ScheduleAlarmReceiver.triggerImmediateUpdate(context)
        }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            preferencesRepository.setViewMode(mode)
        }
    }
}
