package pl.legnica.planzajec.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.parser.model.ChangeFlag
import pl.legnica.planzajec.parser.model.Lesson
import pl.legnica.planzajec.parser.model.LessonType
import pl.legnica.planzajec.parser.model.WeekParity
import java.time.LocalDate
import java.time.LocalTime

class ChangeDetectorTest {

    private val detector = ChangeDetector()
    private val testDate = LocalDate.of(2026, 10, 5)

    private fun createLesson(
        subject: String = "TestSubj",
        room: String = "C212",
        startTime: LocalTime = LocalTime.of(8, 15),
        endTime: LocalTime = LocalTime.of(9, 45),
        subgroup: String = "s3PAM1(1)"
    ) = Lesson(
        date = testDate,
        startTime = startTime,
        endTime = endTime,
        subjectShort = subject,
        subjectFull = "Test Subject Full",
        type = LessonType.LECTURE,
        rawType = "wyk",
        room = room,
        isOnline = false,
        onlineId = null,
        teacher = "dr Testowy",
        subgroup = subgroup,
        weekParity = WeekParity.ALL,
        changeFlag = ChangeFlag.NONE
    )

    @Test
    fun `first sync returns all lessons as NONE changes`() {
        val newLessons = listOf(createLesson())
        val result = detector.detectChanges(newLessons, emptyList(), "s3PAM")

        assertEquals(1, result.updatedEntities.size)
        assertEquals(0, result.changeCount)
        assertEquals(ChangeFlag.NONE, result.updatedEntities.first().changeFlag)
    }

    @Test
    fun `detects room change when same subject and date has new room`() {
        val oldLesson = LessonEntity.fromDomain(createLesson(room = "C212"), "s3PAM")
        val newLesson = createLesson(room = "A101")

        val result = detector.detectChanges(listOf(newLesson), listOf(oldLesson), "s3PAM")

        assertEquals(1, result.updatedEntities.size)
        assertEquals(1, result.changeCount)
        val entity = result.updatedEntities.first()
        assertEquals(ChangeFlag.CHANGED_ROOM_OR_TIME, entity.changeFlag)
        assertEquals("A101", entity.room)
        assertTrue(entity.previousRoomOrTimeNote?.contains("C212") == true)
    }

    @Test
    fun `detects new lesson when added to schedule`() {
        val existing = listOf(LessonEntity.fromDomain(createLesson(subject = "SubjA"), "s3PAM"))
        val newLessons = listOf(
            createLesson(subject = "SubjA"),
            createLesson(subject = "SubjB", startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 30))
        )

        val result = detector.detectChanges(newLessons, existing, "s3PAM")

        assertEquals(2, result.updatedEntities.size)
        assertEquals(1, result.changeCount)
        val subjB = result.updatedEntities.first { it.subjectShort == "SubjB" }
        assertEquals(ChangeFlag.NEW, subjB.changeFlag)
    }

    @Test
    fun `detects cancelled lesson when absent from new schedule for that date`() {
        val oldA = LessonEntity.fromDomain(createLesson(subject = "SubjA"), "s3PAM")
        val oldB = LessonEntity.fromDomain(
            createLesson(subject = "SubjB", startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 30)),
            "s3PAM"
        )
        // SubjB was cancelled/removed in the new download
        val newLessons = listOf(createLesson(subject = "SubjA"))

        val result = detector.detectChanges(newLessons, listOf(oldA, oldB), "s3PAM")

        assertEquals(2, result.updatedEntities.size)
        assertEquals(1, result.changeCount)
        val cancelled = result.updatedEntities.first { it.subjectShort == "SubjB" }
        assertEquals(ChangeFlag.CANCELLED, cancelled.changeFlag)
    }
}
