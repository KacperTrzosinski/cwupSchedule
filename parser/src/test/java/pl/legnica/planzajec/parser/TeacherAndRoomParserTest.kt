package pl.legnica.planzajec.parser

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TeacherAndRoomParserTest {

    @Test
    fun `parse teachers from nauczyciel_11 extracts teachers with non-empty names and ids`() {
        val parser = TeacherParser()
        val html = FixtureLoader.load("nauczyciel_11.html")
        val teachers = parser.parse(html)

        assertTrue(teachers.isNotEmpty(), "Teacher list should not be empty")
        assertTrue(teachers.size > 50, "Should find numerous teachers")

        val sample = teachers.first()
        assertTrue(sample.name.isNotBlank())
        assertTrue(sample.id.isNotBlank())
    }

    @Test
    fun `parse rooms from sala_11 extracts rooms grouped with buildings`() {
        val parser = RoomParser()
        val html = FixtureLoader.load("sala_11.html")
        val rooms = parser.parse(html)

        assertTrue(rooms.isNotEmpty(), "Room list should not be empty")
        assertTrue(rooms.size > 30, "Should find numerous rooms")

        val sample = rooms.first()
        assertTrue(sample.name.isNotBlank())
        assertTrue(sample.id.isNotBlank())
        assertTrue(sample.building.isNotBlank())
    }
}
