package pl.legnica.planzajec.parser

import org.jsoup.Jsoup
import pl.legnica.planzajec.parser.model.StudyCourse
import pl.legnica.planzajec.parser.model.StudyGroup

class CourseGroupParser {

    data class CourseAndGroups(
        val courses: List<StudyCourse>,
        val groups: List<StudyGroup>
    )

    /**
     * Parses the department view HTML (schedule_view.php?site=show_kierunek.php&id=X)
     * extracting study courses and their associated student groups.
     */
    fun parse(html: String, departmentId: Int): CourseAndGroups {
        val doc = Jsoup.parse(html)
        val courses = mutableListOf<StudyCourse>()
        val groups = mutableListOf<StudyGroup>()

        // Look for accordion items containing courses
        val accordionListItems = doc.select("ul.accordion > li")
        for (li in accordionListItems) {
            val titleAnchor = li.selectFirst("> a") ?: continue
            val fullCourseTitle = titleAnchor.text().trim()
            if (fullCourseTitle.isBlank() || isNavigationLink(fullCourseTitle)) {
                continue
            }

            val isFullTime = !fullCourseTitle.contains("niestacjonarne", ignoreCase = true)
            val year = extractYear(fullCourseTitle)

            val course = StudyCourse(
                name = fullCourseTitle,
                departmentId = departmentId,
                isFullTime = isFullTime,
                year = year
            )
            courses.add(course)

            // Extract groups inside the content container (div)
            val contentDiv = li.selectFirst("> div") ?: continue
            val groupLinks = contentDiv.select("a[href*='checkSpecjalnosc.php']")

            for (link in groupLinks) {
                val href = link.attr("href")
                val codeMatch = Regex("""specjalnosc=([^&"'\s>]+)""").find(href)
                val groupCode = codeMatch?.groupValues?.get(1)?.trim() ?: continue

                // Check text immediately before the link or surrounding text
                val previousSibling = link.previousSibling()?.toString()?.trim().orEmpty()
                val groupLabel = if (previousSibling.isNotBlank()) {
                    previousSibling.replace("""<[^>]*>""".toRegex(), "").trim()
                } else {
                    groupCode
                }

                val studyGroup = StudyGroup(
                    code = groupCode,
                    name = if (groupLabel.isNotBlank()) groupLabel else groupCode,
                    courseName = fullCourseTitle,
                    departmentId = departmentId,
                    isFullTime = isFullTime,
                    year = year
                )
                groups.add(studyGroup)
            }
        }

        return CourseAndGroups(
            courses = courses.distinctBy { it.name },
            groups = groups.distinctBy { it.code }
        )
    }

    private fun isNavigationLink(title: String): Boolean {
        val lower = title.lowercase()
        return lower.contains("wydział") ||
            lower.contains("szukaj") ||
            lower.contains("sala") ||
            lower.contains("harmonogram")
    }

    private fun extractYear(title: String): Int {
        // e.g. "Informatyka 3- studia stacjonarne", "(s3INF)"
        val regex = Regex("""(?:\s+(\d)\s*-|\(([sn])(\d))""", RegexOption.IGNORE_CASE)
        val match = regex.find(title)
        if (match != null) {
            match.groupValues[1].toIntOrNull()?.let { return it }
            match.groupValues[3].toIntOrNull()?.let { return it }
        }
        return 1
    }
}
