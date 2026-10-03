package pl.legnica.planzajec.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.legnica.planzajec.data.local.AppDatabase
import pl.legnica.planzajec.data.local.entity.DepartmentEntity
import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.data.local.entity.RoomEntity
import pl.legnica.planzajec.data.local.entity.ScheduleMetadataEntity
import pl.legnica.planzajec.data.local.entity.StudyCourseEntity
import pl.legnica.planzajec.data.local.entity.StudyGroupEntity
import pl.legnica.planzajec.data.local.entity.TeacherEntity
import pl.legnica.planzajec.data.remote.ScheduleNetworkClient
import pl.legnica.planzajec.parser.CourseGroupParser
import pl.legnica.planzajec.parser.DepartmentParser
import pl.legnica.planzajec.parser.RoomParser
import pl.legnica.planzajec.parser.ScheduleParser
import pl.legnica.planzajec.parser.TeacherParser
import pl.legnica.planzajec.parser.model.ScheduleResult
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val database: AppDatabase,
    private val networkClient: ScheduleNetworkClient,
    private val departmentParser: DepartmentParser,
    private val courseGroupParser: CourseGroupParser,
    private val scheduleParser: ScheduleParser,
    private val teacherParser: TeacherParser,
    private val roomParser: RoomParser,
    private val changeDetector: ChangeDetector
) {
    private val lessonDao = database.lessonDao()
    private val structureDao = database.structureDao()
    private val metadataDao = database.metadataDao()

    // --- DEPARTMENTS & STRUCTURE ---

    fun getDepartments(): Flow<List<DepartmentEntity>> = structureDao.getAllDepartments()

    suspend fun refreshDepartments(): Result<List<DepartmentEntity>> = runCatching {
        val html = networkClient.fetchDepartmentsHtml()
        val departments = departmentParser.parse(html)
        val entities = departments.map { DepartmentEntity(it.id, it.name) }
        structureDao.insertDepartments(entities)
        entities
    }

    fun getCourses(departmentId: Int): Flow<List<StudyCourseEntity>> =
        structureDao.getCoursesForDepartment(departmentId)

    fun getGroupsForCourse(courseName: String): Flow<List<StudyGroupEntity>> =
        structureDao.getGroupsForCourse(courseName)

    fun getAllGroups(): Flow<List<StudyGroupEntity>> = structureDao.getAllGroups()

    suspend fun refreshCoursesAndGroups(departmentId: Int): Result<Unit> = runCatching {
        val html = networkClient.fetchCoursesHtml(departmentId)
        val result = courseGroupParser.parse(html, departmentId)

        val courseEntities = result.courses.map {
            StudyCourseEntity(
                name = it.name,
                departmentId = it.departmentId,
                isFullTime = it.isFullTime,
                year = it.year
            )
        }
        val groupEntities = result.groups.map {
            StudyGroupEntity(
                code = it.code,
                name = it.name,
                courseName = it.courseName,
                departmentId = it.departmentId,
                isFullTime = it.isFullTime,
                year = it.year
            )
        }

        structureDao.insertCourses(courseEntities)
        structureDao.insertGroups(groupEntities)
    }

    // --- SCHEDULE & LESSONS ---

    fun getLessons(groupCode: String, subgroup: String? = null): Flow<List<LessonEntity>> {
        return lessonDao.getLessonsForGroup(groupCode).map { list ->
            filterBySubgroup(list, subgroup)
        }
    }

    fun getUpcomingLessons(groupCode: String, fromDate: LocalDate, subgroup: String? = null): Flow<List<LessonEntity>> {
        return lessonDao.getUpcomingLessons(groupCode, fromDate).map { list ->
            filterBySubgroup(list, subgroup)
        }
    }

    fun getLessonsForDate(groupCode: String, date: LocalDate, subgroup: String? = null): Flow<List<LessonEntity>> {
        return lessonDao.getLessonsForGroupAndDate(groupCode, date).map { list ->
            filterBySubgroup(list, subgroup)
        }
    }

    fun getMetadata(groupCode: String): Flow<ScheduleMetadataEntity?> =
        metadataDao.getMetadata(groupCode)

    suspend fun refreshSchedule(groupCode: String, weekDate: String? = null): Result<Int> = runCatching {
        val html = networkClient.fetchGroupScheduleHtml(groupCode, weekDate)
        val scheduleResult = scheduleParser.parse(html, groupCode)

        // Compare with existing cache to detect changes
        val existingLessons = lessonDao.getLessonsListForGroup(groupCode)
        val diffResult = changeDetector.detectChanges(scheduleResult.lessons, existingLessons, groupCode)

        // Update database
        lessonDao.replaceLessonsForGroup(groupCode, diffResult.updatedEntities)

        val selectedWeek = scheduleResult.weeks.find { it.isSelected }?.value ?: weekDate.orEmpty()
        val weeksJson = scheduleResult.weeks.joinToString(";") { "${it.value}:${it.label}" }

        metadataDao.insertMetadata(
            ScheduleMetadataEntity(
                groupCode = groupCode,
                lastSyncedMillis = System.currentTimeMillis(),
                selectedWeek = selectedWeek,
                availableWeeksJson = weeksJson
            )
        )

        diffResult.changeCount
    }

    suspend fun getAvailableSubgroups(groupCode: String): List<String> {
        return lessonDao.getSubgroupsForGroup(groupCode)
    }

    // --- SEARCH: TEACHERS & ROOMS ---

    fun getAllTeachers(): Flow<List<TeacherEntity>> = structureDao.getAllTeachers()
    fun getAllRooms(): Flow<List<RoomEntity>> = structureDao.getAllRooms()

    suspend fun refreshTeachersAndRooms(): Result<Unit> = runCatching {
        val teachersHtml = networkClient.fetchTeachersHtml()
        val teachers = teacherParser.parse(teachersHtml).map {
            TeacherEntity(id = it.id, name = it.name, departmentId = it.departmentId)
        }
        structureDao.insertTeachers(teachers)

        val roomsHtml = networkClient.fetchRoomsHtml()
        val rooms = roomParser.parse(roomsHtml).map {
            RoomEntity(id = it.id, name = it.name, building = it.building)
        }
        structureDao.insertRooms(rooms)
    }

    suspend fun fetchTeacherSchedule(teacherId: String, departmentId: Int): Result<ScheduleResult> = runCatching {
        val html = networkClient.fetchTeacherScheduleHtml(teacherId, departmentId)
        scheduleParser.parse(html, "teacher_$teacherId")
    }

    suspend fun fetchRoomSchedule(roomId: String): Result<ScheduleResult> = runCatching {
        val html = networkClient.fetchRoomScheduleHtml(roomId)
        scheduleParser.parse(html, "room_$roomId")
    }

    private fun filterBySubgroup(list: List<LessonEntity>, subgroup: String?): List<LessonEntity> {
        if (subgroup.isNullOrBlank()) return list
        return list.filter { it.subgroup == subgroup || it.subgroup.isBlank() }
    }
}
