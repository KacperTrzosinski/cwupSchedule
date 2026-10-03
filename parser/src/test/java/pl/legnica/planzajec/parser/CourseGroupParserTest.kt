package pl.legnica.planzajec.parser

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CourseGroupParserTest {

    private val parser = CourseGroupParser()

    @Test
    fun `parse wydzial_1 returns technical courses and groups including s3PAM and s3GK`() {
        val html = FixtureLoader.load("wydzial_1.html")
        val result = parser.parse(html, departmentId = 1)

        assertTrue(result.courses.isNotEmpty(), "Courses list should not be empty")
        assertTrue(result.groups.isNotEmpty(), "Groups list should not be empty")

        val groupCodes = result.groups.map { it.code }
        assertTrue(groupCodes.contains("s3PAM"), "Should contain s3PAM")
        assertTrue(groupCodes.contains("s3GK"), "Should contain s3GK")
        assertTrue(groupCodes.contains("s1INF"), "Should contain s1INF")
        assertTrue(groupCodes.contains("n1E"), "Should contain n1E")

        val pam = result.groups.first { it.code == "s3PAM" }
        assertTrue(pam.isFullTime)
        assertTrue(pam.year >= 3)
    }

    @Test
    fun `parse wydzial_2 returns social science courses and groups`() {
        val html = FixtureLoader.load("wydzial_2.html")
        val result = parser.parse(html, departmentId = 2)

        assertTrue(result.courses.isNotEmpty())
        assertTrue(result.groups.isNotEmpty())

        val groupCodes = result.groups.map { it.code }
        assertTrue(groupCodes.contains("n1BW") || groupCodes.contains("s1BW") || groupCodes.contains("s1A"))
    }

    @Test
    fun `parse wydzial_7 returns health science courses and groups`() {
        val html = FixtureLoader.load("wydzial_7.html")
        val result = parser.parse(html, departmentId = 7)

        assertTrue(result.courses.isNotEmpty())
        assertTrue(result.groups.isNotEmpty())

        val groupCodes = result.groups.map { it.code }
        assertTrue(groupCodes.contains("s1P") || groupCodes.contains("s1RM"))
    }
}
