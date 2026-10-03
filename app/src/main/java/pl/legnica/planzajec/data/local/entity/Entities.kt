package pl.legnica.planzajec.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.legnica.planzajec.parser.model.ChangeFlag
import pl.legnica.planzajec.parser.model.Lesson
import pl.legnica.planzajec.parser.model.LessonType
import pl.legnica.planzajec.parser.model.WeekParity
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupCode: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val subjectShort: String,
    val subjectFull: String,
    val type: LessonType,
    val rawType: String,
    val room: String,
    val isOnline: Boolean,
    val onlineId: String?,
    val teacher: String,
    val subgroup: String,
    val weekParity: WeekParity,
    val changeFlag: ChangeFlag,
    val isVariant: Boolean,
    val variantGroupName: String?,
    val previousRoomOrTimeNote: String? = null
) {
    fun toDomain(): Lesson = Lesson(
        date = date,
        startTime = startTime,
        endTime = endTime,
        subjectShort = subjectShort,
        subjectFull = subjectFull,
        type = type,
        rawType = rawType,
        room = room,
        isOnline = isOnline,
        onlineId = onlineId,
        teacher = teacher,
        subgroup = subgroup,
        weekParity = weekParity,
        changeFlag = changeFlag,
        isVariant = isVariant,
        variantGroupName = variantGroupName
    )

    companion object {
        fun fromDomain(lesson: Lesson, groupCode: String, changeFlag: ChangeFlag = ChangeFlag.NONE, note: String? = null): LessonEntity =
            LessonEntity(
                groupCode = groupCode,
                date = lesson.date,
                startTime = lesson.startTime,
                endTime = lesson.endTime,
                subjectShort = lesson.subjectShort,
                subjectFull = lesson.subjectFull,
                type = lesson.type,
                rawType = lesson.rawType,
                room = lesson.room,
                isOnline = lesson.isOnline,
                onlineId = lesson.onlineId,
                teacher = lesson.teacher,
                subgroup = lesson.subgroup,
                weekParity = lesson.weekParity,
                changeFlag = changeFlag,
                isVariant = lesson.isVariant,
                variantGroupName = lesson.variantGroupName,
                previousRoomOrTimeNote = note
            )
    }
}

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey
    val id: Int,
    val name: String
)

@Entity(tableName = "study_courses")
data class StudyCourseEntity(
    @PrimaryKey
    val name: String,
    val departmentId: Int,
    val isFullTime: Boolean,
    val year: Int
)

@Entity(tableName = "study_groups")
data class StudyGroupEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val courseName: String,
    val departmentId: Int,
    val isFullTime: Boolean,
    val year: Int
)

@Entity(tableName = "schedule_metadata")
data class ScheduleMetadataEntity(
    @PrimaryKey
    val groupCode: String,
    val lastSyncedMillis: Long,
    val selectedWeek: String,
    val availableWeeksJson: String
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val departmentId: Int
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val building: String
)
