package pl.legnica.planzajec.parser

import org.jsoup.Jsoup
import pl.legnica.planzajec.parser.model.Teacher

class TeacherParser {

    /**
     * Parses the teacher list page (schedule_view.php?site=show_nauczyciel.php&id=11)
     */
    fun parse(html: String): List<Teacher> {
        val doc = Jsoup.parse(html)
        val teachers = mutableListOf<Teacher>()

        val listItems = doc.select("ul.accordion > li")
        for (li in listItems) {
            val contentDiv = li.selectFirst("> div") ?: continue
            val links = contentDiv.select("a[href*='checkNauczycielAll.php']")

            for (link in links) {
                val href = link.attr("href")
                val pracownikMatch = Regex("""pracownik=(\d+)""").find(href)
                val wydzialMatch = Regex("""wydzial=(\d+)""").find(href)

                val id = pracownikMatch?.groupValues?.get(1) ?: continue
                val deptId = wydzialMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1

                val prevSiblingText = link.previousSibling()?.toString()?.trim().orEmpty()
                val cleanName = prevSiblingText
                    .replace(Regex("""<[^>]*>"""), "")
                    .replace("&nbsp;", " ")
                    .trim()

                if (cleanName.isNotBlank()) {
                    teachers.add(
                        Teacher(
                            id = id,
                            name = cleanName,
                            departmentId = deptId
                        )
                    )
                }
            }
        }

        return teachers.distinctBy { it.id }
    }
}
