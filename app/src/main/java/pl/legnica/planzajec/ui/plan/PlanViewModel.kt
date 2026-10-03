package pl.legnica.planzajec.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import pl.legnica.planzajec.parser.model.WeekOption
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PlanUiState(
    val groupCode: String = "",
    val subgroup: String? = null,
    val availableSubgroups: List<String> = emptyList(),
    val lessons: List<LessonEntity> = emptyList(),
    val availableWeeks: List<WeekOption> = emptyList(),
    val selectedWeek: String = "",
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
    private val preferencesRepository: UserPreferencesRepository
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

                    val weeksList = parseWeeksJson(metadata?.availableWeeksJson.orEmpty(), metadata?.selectedWeek.orEmpty())
                    val subgroups = repository.getAvailableSubgroups(groupCode)

                    val filteredLessons = when (prefs.filterMode) {
                        FilterMode.CAMPUS_ONLY -> lessonsList.filter { !it.isOnline }
                        FilterMode.ONLINE_ONLY -> lessonsList.filter { it.isOnline }
                        FilterMode.ALL -> lessonsList
                    }

                    _uiState.update {
                        it.copy(
                            lessons = filteredLessons,
                            availableWeeks = weeksList,
                            selectedWeek = metadata?.selectedWeek.orEmpty(),
                            availableSubgroups = subgroups,
                            lastUpdated = lastUpdatedFormatted,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    fun refresh(weekDate: String? = null) {
        val groupCode = _uiState.value.groupCode
        if (groupCode.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            repository.refreshSchedule(groupCode, weekDate)
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

    fun selectWeek(weekValue: String) {
        refresh(weekValue)
    }

    fun selectSubgroup(subgroup: String?) {
        viewModelScope.launch {
            preferencesRepository.updateSubgroup(subgroup)
        }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            preferencesRepository.setViewMode(mode)
        }
    }

    private fun parseWeeksJson(json: String, selectedWeek: String): List<WeekOption> {
        if (json.isBlank()) return emptyList()
        return json.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size >= 2) {
                val value = parts[0]
                val label = parts[1]
                WeekOption(value = value, label = label, isSelected = value == selectedWeek)
            } else null
        }
    }
}
