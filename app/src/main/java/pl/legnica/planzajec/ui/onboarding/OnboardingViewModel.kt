package pl.legnica.planzajec.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.legnica.planzajec.data.local.entity.DepartmentEntity
import pl.legnica.planzajec.data.local.entity.StudyCourseEntity
import pl.legnica.planzajec.data.local.entity.StudyGroupEntity
import pl.legnica.planzajec.data.preferences.UserPreferencesRepository
import pl.legnica.planzajec.data.repository.ScheduleRepository
import javax.inject.Inject

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val departments: List<DepartmentEntity> = emptyList(),
    val selectedDepartment: DepartmentEntity? = null,
    val courses: List<StudyCourseEntity> = emptyList(),
    val selectedCourse: StudyCourseEntity? = null,
    val availableYears: List<Int> = emptyList(),
    val selectedYear: Int? = null,
    val groups: List<StudyGroupEntity> = emptyList(),
    val selectedGroup: StudyGroupEntity? = null,
    val availableSubgroups: List<String> = emptyList(),
    val showSubgroupDialog: Boolean = false,
    val isOnboardingComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState(isLoading = true))
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadDepartments()
    }

    fun loadDepartments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.refreshDepartments()
                .onSuccess { depts ->
                    _uiState.update { it.copy(departments = depts, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Nie udało się pobrać listy wydziałów: ${error.localizedMessage}"
                        )
                    }
                }
        }
    }

    fun selectDepartment(department: DepartmentEntity) {
        if (_uiState.value.selectedDepartment?.id == department.id) return

        _uiState.update {
            it.copy(
                selectedDepartment = department,
                selectedCourse = null,
                selectedYear = null,
                selectedGroup = null,
                courses = emptyList(),
                availableYears = emptyList(),
                groups = emptyList(),
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            repository.refreshCoursesAndGroups(department.id)
                .onSuccess {
                    repository.getCourses(department.id).collect { coursesList ->
                        _uiState.update { it.copy(courses = coursesList, isLoading = false) }
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Błąd pobierania kierunków: ${err.localizedMessage}"
                        )
                    }
                }
        }
    }

    fun selectCourse(course: StudyCourseEntity) {
        if (_uiState.value.selectedCourse?.name == course.name) return

        viewModelScope.launch {
            repository.getGroupsForCourse(course.name).collect { groupsList ->
                val years = groupsList.map { it.year }.distinct().sorted()
                _uiState.update {
                    it.copy(
                        selectedCourse = course,
                        selectedYear = if (years.size == 1) years.first() else null,
                        availableYears = years,
                        selectedGroup = null,
                        groups = groupsList
                    )
                }
            }
        }
    }

    fun selectYear(year: Int) {
        _uiState.update { it.copy(selectedYear = year, selectedGroup = null) }
    }

    fun selectGroup(group: StudyGroupEntity) {
        _uiState.update { it.copy(selectedGroup = group) }
    }

    fun searchSchedule() {
        val group = _uiState.value.selectedGroup ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.refreshSchedule(group.code)
                .onSuccess {
                    val subgroups = repository.getAvailableSubgroups(group.code)
                    if (subgroups.size > 1) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                availableSubgroups = subgroups,
                                showSubgroupDialog = true
                            )
                        }
                    } else {
                        finishOnboarding(null)
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Błąd pobierania planu: ${err.localizedMessage}"
                        )
                    }
                }
        }
    }

    fun finishOnboarding(subgroup: String?) {
        val state = _uiState.value
        val dept = state.selectedDepartment ?: return
        val course = state.selectedCourse ?: return
        val group = state.selectedGroup ?: return

        viewModelScope.launch {
            preferencesRepository.saveGroupSelection(
                departmentId = dept.id,
                courseName = course.name,
                groupCode = group.code,
                subgroup = subgroup
            )
            _uiState.update {
                it.copy(
                    showSubgroupDialog = false,
                    isOnboardingComplete = true,
                    isLoading = false
                )
            }
        }
    }
}
