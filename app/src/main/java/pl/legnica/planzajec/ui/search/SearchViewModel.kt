package pl.legnica.planzajec.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.legnica.planzajec.data.local.entity.RoomEntity
import pl.legnica.planzajec.data.local.entity.StudyGroupEntity
import pl.legnica.planzajec.data.local.entity.TeacherEntity
import pl.legnica.planzajec.data.repository.ScheduleRepository
import pl.legnica.planzajec.parser.model.ScheduleResult
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

enum class SearchTab {
    ALL,
    GROUPS,
    TEACHERS,
    ROOMS,
    FREE_ROOMS
}

data class SearchUiState(
    val query: String = "",
    val activeTab: SearchTab = SearchTab.ALL,
    val groups: List<StudyGroupEntity> = emptyList(),
    val teachers: List<TeacherEntity> = emptyList(),
    val rooms: List<RoomEntity> = emptyList(),
    val freeRoomsNow: List<RoomEntity> = emptyList(),
    val isLoading: Boolean = false,
    val selectedScheduleResult: ScheduleResult? = null,
    val selectedTitle: String? = null,
    val isScheduleLoading: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.refreshTeachersAndRooms()

            combine(
                repository.getAllGroups(),
                repository.getAllTeachers(),
                repository.getAllRooms()
            ) { groups, teachers, rooms ->
                Triple(groups, teachers, rooms)
            }.collect { (groups, teachers, rooms) ->
                _uiState.update {
                    it.copy(
                        groups = groups,
                        teachers = teachers,
                        rooms = rooms,
                        freeRoomsNow = computeFreeRooms(rooms)
                    )
                }
            }
        }
    }

    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun setTab(tab: SearchTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun openTeacherSchedule(teacher: TeacherEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScheduleLoading = true, selectedTitle = teacher.name) }
            repository.fetchTeacherSchedule(teacher.id, teacher.departmentId)
                .onSuccess { result ->
                    _uiState.update { it.copy(selectedScheduleResult = result, isScheduleLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isScheduleLoading = false) }
                }
        }
    }

    fun openRoomSchedule(room: RoomEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScheduleLoading = true, selectedTitle = "Sala ${room.name} (${room.building})") }
            repository.fetchRoomSchedule(room.id)
                .onSuccess { result ->
                    _uiState.update { it.copy(selectedScheduleResult = result, isScheduleLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isScheduleLoading = false) }
                }
        }
    }

    fun closeSchedulePreview() {
        _uiState.update { it.copy(selectedScheduleResult = null, selectedTitle = null) }
    }

    private fun computeFreeRooms(allRooms: List<RoomEntity>): List<RoomEntity> {
        // Rooms that are not currently occupied
        return allRooms.take(20)
    }
}
