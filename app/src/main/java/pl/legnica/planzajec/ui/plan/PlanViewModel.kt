package pl.legnica.planzajec.ui.plan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private fun observeData() {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                val groupCode = prefs.selectedGroupCode ?: return@collect
                val subgroup = prefs.selectedSubgroup

                _uiState.update {
                    it.copy(
                        groupCode = groupCode,
                        subgroup = subgroup,
                        viewMode = prefs.viewMode,
                        filterMode = prefs.filterMode,
                        mergeBlocks = prefs.mergeConsecutiveBlocks
                    )
                }

                // Load lessons and metadata
                combine(
                    repository.getLessons(groupCode, subgroup),
                    repository.getMetadata(groupCode)
                ) { lessonsList, metadata ->
                    Pair(lessonsList, metadata)
                }.collect { (lessonsList, metadata) ->
                    val lastUpdatedFormatted = if (metadata != null && metadata.lastSyncedMillis > 0) {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(metadata.lastSyncedMillis))
                    } else ""

                    val subgroups = repository.getAvailableSubgroups(groupCode)

                    val filteredLessons = when (prefs.filterMode) {
                        FilterMode.CAMPUS_ONLY -> lessonsList.filter { !it.isOnline }
                        FilterMode.ONLINE_ONLY -> lessonsList.filter { it.isOnline }
                        FilterMode.ALL -> lessonsList
                    }

                    _uiState.update {
                        it.copy(
                            lessons = filteredLessons,
                            availableSubgroups = subgroups,
                            lastUpdated = lastUpdatedFormatted,
                            isRefreshing = false
                        )
                    }

                    if (filteredLessons.isNotEmpty()) {
                        ScheduleAlarmReceiver.triggerImmediateUpdate(context)
                    }
                }
            }
        }
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
