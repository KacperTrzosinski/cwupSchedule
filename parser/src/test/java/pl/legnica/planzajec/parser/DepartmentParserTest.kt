package pl.legnica.planzajec.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DepartmentParserTest {

    private val parser = DepartmentParser()

    @Test
    fun `parse departments from index html extracts all 5 main faculties`() {
        val html = FixtureLoader.load("index.html")
        val departments = parser.parse(html)

        assertEquals(5, departments.size)

        val deptIds = departments.map { it.id }.toSet()
        assertTrue(deptIds.containsAll(listOf(1, 2, 7, 10, 11)))

        val wnte = departments.first { it.id == 1 }
        assertTrue(wnte.name.contains("Technicznych", ignoreCase = true))

        val wnoz = departments.first { it.id == 7 }
        assertTrue(wnoz.name.contains("Zdrowiu", ignoreCase = true))
    }
}
