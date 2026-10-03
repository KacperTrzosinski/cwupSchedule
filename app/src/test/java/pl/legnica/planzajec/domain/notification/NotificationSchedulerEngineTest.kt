package pl.legnica.planzajec.domain.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.parser.model.ChangeFlag
import pl.legnica.planzajec.parser.model.Lesson
import pl.legnica.planzajec.parser.model.LessonType
import pl.legnica.planzajec.parser.model.WeekParity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class NotificationSchedulerEngineTest {

    private val engine = NotificationSchedulerEngine()
    private val testDate = LocalDate.of(2026, 10, 5)

    private fun createLesson(
        subject: String = "Metody sztucznej inteligencji",
        short: String = "Msi",
        room: String = "E1",
        start: LocalTime = LocalTime.of(10, 0),
        end: LocalTime = LocalTime.of(11, 30),
        teacher: String = "dr inż. Piotr Nadybski",
        type: LessonType = LessonType.LECTURE,
        isOnline: Boolean = false,
        onlineId: String? = null,
        flag: ChangeFlag = ChangeFlag.NONE
    ) = LessonEntity.fromDomain(
        lesson = Lesson(
            date = testDate,
            startTime = start,
            endTime = end,
            subjectShort = short,
            subjectFull = subject,
            type = type,
            rawType = "wyk",
            room = room,
            isOnline = isOnline,
            onlineId = onlineId,
            teacher = teacher,
            subgroup = "s3PAM1(1)",
            weekParity = WeekParity.ALL,
            changeFlag = flag
        ),
        groupCode = "s3PAM",
        changeFlag = flag
    )

    @Test
    fun `day without lessons produces no notification and no alarms`() {
        val now = LocalDateTime.of(testDate, LocalTime.of(9, 0))
        val plan = engine.computePlan(emptyList(), now)

        assertFalse(plan.state.shouldShow)
        assertTrue(plan.alarms.isEmpty())
    }

    @Test
    fun `more than 60 minutes before first lesson schedules alarm but does not show notification yet`() {
        val lesson = createLesson(start = LocalTime.of(10, 0), end = LocalTime.of(11, 30))
        val now = LocalDateTime.of(testDate, LocalTime.of(8, 30)) // 90 min before

        val plan = engine.computePlan(listOf(lesson), now)

        assertFalse(plan.state.shouldShow)
        assertEquals(3, plan.alarms.size)
        // Alarm 1: 60 min before start (09:00)
        assertEquals(AlarmType.SHOW_FIRST, plan.alarms[0].type)
        assertEquals(LocalTime.of(9, 0), plan.alarms[0].triggerTime.toLocalTime())
    }

    @Test
    fun `within 60 minutes before first lesson shows upcoming notification with countdown`() {
        val lesson = createLesson(start = LocalTime.of(10, 0), end = LocalTime.of(11, 30))
        val now = LocalDateTime.of(testDate, LocalTime.of(9, 15)) // 45 min before

        val plan = engine.computePlan(listOf(lesson), now)

        assertTrue(plan.state.shouldShow)
        assertTrue(plan.state.title.contains("Następne: Metody sztucznej inteligencji"))
        assertTrue(plan.state.content.contains("za 45 min"))
        assertTrue(plan.state.content.contains("E1"))
    }

    @Test
    fun `during lesson switches to next lesson and puts current in expanded text`() {
        val l1 = createLesson(subject = "Lekcja 1", short = "L1", start = LocalTime.of(8, 15), end = LocalTime.of(9, 45))
        val l2 = createLesson(subject = "Lekcja 2", short = "L2", start = LocalTime.of(10, 0), end = LocalTime.of(11, 30))
        val now = LocalDateTime.of(testDate, LocalTime.of(8, 45)) // During l1

        val plan = engine.computePlan(listOf(l1, l2), now)

        assertTrue(plan.state.shouldShow)
        assertTrue(plan.state.title.contains("Następne: Lekcja 2"))
        assertTrue(plan.state.expandedText.contains("Teraz trwa: L1"))
    }

    @Test
    fun `during last lesson shows ongoing status`() {
        val lesson = createLesson(start = LocalTime.of(10, 0), end = LocalTime.of(11, 30))
        val now = LocalDateTime.of(testDate, LocalTime.of(10, 30)) // During lesson

        val plan = engine.computePlan(listOf(lesson), now)

        assertTrue(plan.state.shouldShow)
        assertTrue(plan.state.title.contains("Trwa: Metody sztucznej inteligencji"))
    }

    @Test
    fun `after last lesson dismisses notification`() {
        val lesson = createLesson(start = LocalTime.of(10, 0), end = LocalTime.of(11, 30))
        val now = LocalDateTime.of(testDate, LocalTime.of(11, 45)) // After lesson

        val plan = engine.computePlan(listOf(lesson), now)

        assertFalse(plan.state.shouldShow)
    }

    @Test
    fun `online lesson displays Online_3 instead of room`() {
        val onlineLesson = createLesson(
            room = "Online_3",
            isOnline = true,
            onlineId = "3",
            start = LocalTime.of(18, 45),
            end = LocalTime.of(20, 15)
        )
        val now = LocalDateTime.of(testDate, LocalTime.of(18, 0)) // 45 min before

        val plan = engine.computePlan(listOf(onlineLesson), now)

        assertTrue(plan.state.shouldShow)
        assertTrue(plan.state.content.contains("Online_3"))
        assertTrue(plan.state.isOnline)
    }

    @Test
    fun `cancelled lessons during day are ignored when computing alarms`() {
        val cancelled = createLesson(
            subject = "Odwołane",
            start = LocalTime.of(8, 15),
            end = LocalTime.of(9, 45),
            flag = ChangeFlag.CANCELLED
        )
        val active = createLesson(
            subject = "Aktywne",
            start = LocalTime.of(10, 0),
            end = LocalTime.of(11, 30)
        )
        val now = LocalDateTime.of(testDate, LocalTime.of(8, 0))

        val plan = engine.computePlan(listOf(cancelled, active), now)

        // First alarm should be 60 min before active lesson (09:00), ignoring cancelled
        val firstAlarm = plan.alarms.first()
        assertEquals(LocalTime.of(9, 0), firstAlarm.triggerTime.toLocalTime())
    }
}
