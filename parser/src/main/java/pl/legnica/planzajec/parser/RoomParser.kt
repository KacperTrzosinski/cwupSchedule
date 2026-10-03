package pl.legnica.planzajec.parser

import org.jsoup.Jsoup
import pl.legnica.planzajec.parser.model.Room

class RoomParser {

    /**
     * Parses the room list page (schedule_view.php?site=show_sala.php&id=11)
     */
    fun parse(html: String): List<Room> {
        val doc = Jsoup.parse(html)
        val rooms = mutableListOf<Room>()

        val listItems = doc.select("ul.accordion > li")
        for (li in listItems) {
            val buildingTitle = li.selectFirst("> a")?.text()?.trim().orEmpty()
            val contentDiv = li.selectFirst("> div") ?: continue

            val roomLinks = contentDiv.select("a[href*='checkSala.php']")
            for (link in roomLinks) {
                val href = link.attr("href")
                val salaMatch = Regex("""sala=(\d+)""").find(href)
                val id = salaMatch?.groupValues?.get(1) ?: continue
                val name = link.text().trim()

                if (name.isNotBlank()) {
                    rooms.add(
                        Room(
                            id = id,
                            name = name,
                            building = buildingTitle
                        )
                    )
                }
            }
        }

        return rooms.distinctBy { it.id }
    }
}
