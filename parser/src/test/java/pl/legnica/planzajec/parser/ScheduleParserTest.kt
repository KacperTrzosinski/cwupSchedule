package pl.legnica.planzajec.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pl.legnica.planzajec.parser.model.LessonType
import pl.legnica.planzajec.parser.model.WeekParity
import java.time.LocalTime

class ScheduleParserTest {

    private val parser = ScheduleParser()

    @Test
    fun `parse plan_s3PAM extracts weeks, subgroups, legend and lessons`() {
        val html = FixtureLoader.load("plan_s3PAM.html")
        val result = parser.parse(html, "s3PAM")

        assertEquals("s3PAM", result.groupCode)
        assertTrue(result.weeks.isNotEmpty(), "Weeks should be parsed")
        assertTrue(result.weeks.any { it.isSelected }, "Current week should be marked as selected")

        assertTrue(result.subgroups.isNotEmpty(), "Subgroups should be parsed")
        assertTrue(result.subgroups.contains("s3PAM1(1)"))

        assertTrue(result.legend.isNotEmpty(), "Legend should be parsed")
        // Check Polish character decoding in legend
        assertTrue(
            result.legend.values.any { it.contains("interfejs", ignoreCase = true) || it.contains("sztuczn", ignoreCase = true) },
            "Legend should decode Polish characters correctly"
        )
    }

    @Test
    fun `parse semestr_s3PAM handles online rooms, variants and parity`() {
        val html = FixtureLoader.load("semestr_s3PAM.html")
        val result = parser.parse(html, "s3PAM")

        assertTrue(result.lessons.isNotEmpty(), "Semester lessons should not be empty")

        // Verify Online rooms detection
        val onlineLessons = result.lessons.filter { it.isOnline }
        assertTrue(onlineLessons.isNotEmpty(), "Should detect online lessons")
        val onlineSample = onlineLessons.first()
        assertNotNull(onlineSample.onlineId)
        assertTrue(onlineSample.room.contains("Online", ignoreCase = true))

        // Verify variants detection
        val variantLessons = result.lessons.filter { it.isVariant }
        assertTrue(variantLessons.isNotEmpty(), "Should detect variant/Rozne lessons")

        // Verify variants table
        assertTrue(result.variants.isNotEmpty(), "Should extract variant options from Legenda tables")
    }

    @Test
    fun `parse schedules from multiple departments without errors`() {
        val fixtures = listOf(
            "plan_s1INF.html" to "s1INF",
            "plan_n1BW.html" to "n1BW",
            "plan_s1P.html" to "s1P",
            "plan_s1RM.html" to "s1RM",
            "plan_s1INT.html" to "s1INT",
            "plan_s1WF.html" to "s1WF"
        )

        for ((fixture, code) in fixtures) {
            val html = FixtureLoader.load(fixture)
            val result = parser.parse(html, code)
            assertNotNull(result)
            assertEquals(code, result.groupCode)
            assertTrue(result.subgroups.isNotEmpty(), "Subgroups should not be empty for $fixture")
        }
    }

    @Test
    fun `45 minute block calculation properly splits time interval`() {
        val htmlFirstHalf = """
            <table class="TabPlan">
                <tr><td></td><td class="nazwaSpecjalnosci" colspan="3">s1TEST</td></tr>
                <tr><td class="nazwaDnia" colspan="4">Poniedziałek 05.10.2026</td></tr>
                <tr>
                    <td class="godzina">08:15-09:45</td>
                    <td class="test">TestSubj (lab) 1h [x/-]</td>
                    <td class="test">dr Testowy</td>
                    <td class="test2">A101</td>
                </tr>
            </table>
        """.trimIndent()

        val result1 = parser.parse(htmlFirstHalf, "s1TEST")
        assertEquals(1, result1.lessons.size)
        val l1 = result1.lessons.first()
        assertEquals(LocalTime.of(8, 15), l1.startTime)
        assertEquals(LocalTime.of(9, 0), l1.endTime)
        assertEquals("TestSubj", l1.subjectShort)
        assertEquals(LessonType.LABORATORY, l1.type)

        val htmlSecondHalf = """
            <table class="TabPlan">
                <tr><td></td><td class="nazwaSpecjalnosci" colspan="3">s1TEST</td></tr>
                <tr><td class="nazwaDnia" colspan="4">Poniedziałek 05.10.2026</td></tr>
                <tr>
                    <td class="godzina">08:15-09:45</td>
                    <td class="test">TestSubj2 (wyk) 1h [-/x]</td>
                    <td class="test">dr Testowy</td>
                    <td class="test2">A101</td>
                </tr>
            </table>
        """.trimIndent()

        val result2 = parser.parse(htmlSecondHalf, "s1TEST")
        assertEquals(1, result2.lessons.size)
        val l2 = result2.lessons.first()
        assertEquals(LocalTime.of(9, 0), l2.startTime)
        assertEquals(LocalTime.of(9, 45), l2.endTime)
        assertEquals("TestSubj2", l2.subjectShort)
        assertEquals(LessonType.LECTURE, l2.type)
    }

    @Test
    fun `two items separated by HR are parsed with even and odd week parity`() {
        val htmlParity = """
            <table class="TabPlan">
                <tr><td></td><td class="nazwaSpecjalnosci" colspan="3">s1TEST</td></tr>
                <tr><td class="nazwaDnia" colspan="4">Poniedziałek 05.10.2026</td></tr>
                <tr>
                    <td class="godzina">10:00-11:30</td>
                    <td class="test">Parzyste (wyk)<hr>Nieparzyste (lab)</td>
                    <td class="test">dr Prowadzący 1<hr>mgr Prowadzący 2</td>
                    <td class="test2">C101<hr>C102</td>
                </tr>
            </table>
        """.trimIndent()

        val result = parser.parse(htmlParity, "s1TEST")
        assertEquals(2, result.lessons.size)

        val even = result.lessons[0]
        assertEquals("Parzyste", even.subjectShort)
        assertEquals(WeekParity.EVEN, even.weekParity)
        assertEquals("C101", even.room)
        assertEquals("dr Prowadzący 1", even.teacher)

        val odd = result.lessons[1]
        assertEquals("Nieparzyste", odd.subjectShort)
        assertEquals(WeekParity.ODD, odd.weekParity)
        assertEquals("C102", odd.room)
        assertEquals("mgr Prowadzący 2", odd.teacher)
    }
}
