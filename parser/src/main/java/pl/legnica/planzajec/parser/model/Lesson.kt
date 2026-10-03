package pl.legnica.planzajec.parser.model

import java.time.LocalDate
import java.time.LocalTime

data class Lesson(
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
    val weekParity: WeekParity = WeekParity.ALL,
    val changeFlag: ChangeFlag = ChangeFlag.NONE,
    val isVariant: Boolean = false,
    val variantGroupName: String? = null
)
