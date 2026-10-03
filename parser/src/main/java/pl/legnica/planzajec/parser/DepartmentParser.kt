package pl.legnica.planzajec.parser

import org.jsoup.Jsoup
import pl.legnica.planzajec.parser.model.Department

class DepartmentParser {

    /**
     * Parses the main page HTML to extract the list of university departments.
     */
    fun parse(html: String): List<Department> {
        val doc = Jsoup.parse(html)
        val departments = mutableListOf<Department>()

        val links = doc.select("a[href*='show_kierunek.php']")
        for (link in links) {
            val href = link.attr("href")
            val idMatch = Regex("""id=(\d+)""").find(href)
            val id = idMatch?.groupValues?.get(1)?.toIntOrNull() ?: continue
            val name = link.text().trim()
            if (name.isNotBlank()) {
                departments.add(Department(id = id, name = name))
            }
        }
        return departments.distinctBy { it.id }
    }
}
