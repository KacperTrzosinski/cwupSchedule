package pl.legnica.planzajec.domain.notification

import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.parser.model.LessonType
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class AlarmType {
    SHOW_FIRST,
    SWITCH_BLOCK,
    DISMISS_DAY
}

data class ScheduledAlarm(
    val triggerTime: LocalDateTime,
    val type: AlarmType,
    val lessonId: Long? = null
)

data class NotificationState(
    val shouldShow: Boolean,
    val title: String = "",
    val content: String = "",
    val expandedText: String = "",
    val lessonType: LessonType = LessonType.OTHER,
    val isOnline: Boolean = false
)

data class NotificationSchedulePlan(
    val state: NotificationState,
    val alarms: List<ScheduledAlarm>
)

class NotificationSchedulerEngine {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Pure function: takes the day's lessons and current date/time,
     * returns what notification to display right now (if any) and the exact timestamps
     * at which alarms should be scheduled.
     */
    fun computePlan(
        lessons: List<LessonEntity>,
        nowDateTime: LocalDateTime
    ): NotificationSchedulePlan {
        val today = nowDateTime.toLocalDate()
        val nowTime = nowDateTime.toLocalTime()

        // Filter and sort today's non-cancelled lessons
        val todayLessons = lessons
            .filter { it.date == today && it.changeFlag != pl.legnica.planzajec.parser.model.ChangeFlag.CANCELLED }
            .sortedBy { it.startTime }

        if (todayLessons.isEmpty()) {
            return NotificationSchedulePlan(
                state = NotificationState(shouldShow = false),
                alarms = emptyList()
            )
        }

        val firstLesson = todayLessons.first()
        val lastLesson = todayLessons.last()
        val showThresholdTime = firstLesson.startTime.minusMinutes(60)

        // Generate alarm triggers
        val alarms = mutableListOf<ScheduledAlarm>()

        // 1. Alarm 60 minutes before first lesson
        if (nowTime.isBefore(showThresholdTime)) {
            alarms.add(
                ScheduledAlarm(
                    triggerTime = LocalDateTime.of(today, showThresholdTime),
                    type = AlarmType.SHOW_FIRST,
                    lessonId = firstLesson.id
                )
            )
        }

        // 2. Alarms at start of each lesson (switch to next block)
        for (lesson in todayLessons) {
            if (nowTime.isBefore(lesson.startTime)) {
                alarms.add(
                    ScheduledAlarm(
                        triggerTime = LocalDateTime.of(today, lesson.startTime),
                        type = AlarmType.SWITCH_BLOCK,
                        lessonId = lesson.id
                    )
                )
            }
        }

        // 3. Alarm at end of the last lesson (dismiss notification)
        if (nowTime.isBefore(lastLesson.endTime)) {
            alarms.add(
                ScheduledAlarm(
                    triggerTime = LocalDateTime.of(today, lastLesson.endTime),
                    type = AlarmType.DISMISS_DAY,
                    lessonId = lastLesson.id
                )
            )
        }

        // Determine current notification state
        if (nowTime.isBefore(showThresholdTime) || nowTime.isAfter(lastLesson.endTime)) {
            // Outside notification window
            return NotificationSchedulePlan(
                state = NotificationState(shouldShow = false),
                alarms = alarms
            )
        }

        // Currently inside the notification window
        // Find current ongoing lesson (if any)
        val ongoingLesson = todayLessons.find { !nowTime.isBefore(it.startTime) && nowTime.isBefore(it.endTime) }

        // Find next upcoming lesson
        val nextLesson = todayLessons.find { it.startTime.isAfter(nowTime) }

        if (nextLesson == null && ongoingLesson == null) {
            return NotificationSchedulePlan(
                state = NotificationState(shouldShow = false),
                alarms = alarms
            )
        }

        val targetForNotification = nextLesson ?: ongoingLesson!!
        val isTargetNext = nextLesson != null

        val subjectTitle = targetForNotification.subjectFull.ifBlank { targetForNotification.subjectShort }
        val title = if (isTargetNext) "Następne: $subjectTitle" else "Trwa: $subjectTitle"

        val roomOrOnline = if (targetForNotification.isOnline) {
            if (targetForNotification.onlineId != null) "Online_${targetForNotification.onlineId}" else "Online"
        } else {
            targetForNotification.room
        }

        val countdownMinutes = if (isTargetNext) {
            Duration.between(nowTime, nextLesson!!.startTime).toMinutes()
        } else 0L

        val countdownStr = if (isTargetNext) {
            if (countdownMinutes >= 60) {
                "za ${countdownMinutes / 60}h ${countdownMinutes % 60} min"
            } else {
                "za $countdownMinutes min"
            }
        } else {
            "teraz trwa"
        }

        val content = "${targetForNotification.startTime.format(timeFormatter)} · $roomOrOnline · ${targetForNotification.teacher} ($countdownStr)"

        val expanded = buildString {
            if (ongoingLesson != null) {
                val ongoingRoom = if (ongoingLesson.isOnline) "Online_${ongoingLesson.onlineId ?: ""}" else ongoingLesson.room
                append("Teraz trwa: ${ongoingLesson.subjectShort} (${ongoingRoom}, do ${ongoingLesson.endTime.format(timeFormatter)})\n")
            }
            if (nextLesson != null) {
                append("Następne: $subjectTitle ($countdownStr)\n")
                // Subsequent lesson if exists
                val laterLessons = todayLessons.filter { it.startTime.isAfter(nextLesson.startTime) }
                if (laterLessons.isNotEmpty()) {
                    val afterNext = laterLessons.first()
                    append("Potem: ${afterNext.subjectShort} (${afterNext.startTime.format(timeFormatter)})")
                }
            }
        }.trim()

        val state = NotificationState(
            shouldShow = true,
            title = title,
            content = content,
            expandedText = expanded,
            lessonType = targetForNotification.type,
            isOnline = targetForNotification.isOnline
        )

        return NotificationSchedulePlan(state, alarms)
    }
}
