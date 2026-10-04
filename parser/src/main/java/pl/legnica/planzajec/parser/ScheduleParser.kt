package pl.legnica.planzajec.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import pl.legnica.planzajec.parser.model.ChangeFlag
import pl.legnica.planzajec.parser.model.Lesson
import pl.legnica.planzajec.parser.model.LessonType
import pl.legnica.planzajec.parser.model.ScheduleResult
import pl.legnica.planzajec.parser.model.VariantOption
import pl.legnica.planzajec.parser.model.WeekOption
import pl.legnica.planzajec.parser.model.WeekParity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScheduleParser {

    /**
     * Parses the group schedule HTML (checkSpecjalnosc.php?specjalnosc=X or checkSpecjalnoscStac.php).
     */
    fun parse(html: String, groupCode: String): ScheduleResult {
        // Strip <div> and </div> tags that the legacy PHP code erroneously injects between <tr> rows,
        // which causes HTML5 tree builders (like Jsoup) to foster-parent rows outside the table.
        val cleanedHtml = html.replace(Regex("""<\/?div[^>]*>""", RegexOption.IGNORE_CASE), "")
        val doc = Jsoup.parse(cleanedHtml)

        val weeks = parseWeekSelector(doc)
        val legend = parseLegend(doc)
        val variants = parseVariantTables(doc)

        val lessons = mutableListOf<Lesson>()
        val subgroups = mutableListOf<String>()

        val scheduleTable = doc.selectFirst("table.TabPlan")
        if (scheduleTable != null) {
            val rows = scheduleTable.select("tr")
            if (rows.isNotEmpty()) {
                // Header row containing subgroup names
                val headerRow = rows[0]
                val headerCols = headerRow.select("td")
                for (col in headerCols) {
                    val text = col.text().trim()
                    if (text.isNotBlank() && col.hasClass("nazwaSpecjalnosci")) {
                        subgroups.add(text)
                    }
                }

                var currentDate: LocalDate? = null

                for (i in 1 until rows.size) {
                    val row = rows[i]

                    // Check if day header row
                    val dayHeader = row.selectFirst("td.nazwaDnia")
                    if (dayHeader != null) {
                        currentDate = parseDate(dayHeader.text().trim())
                        continue
                    }

                    // Check if time row
                    val timeCell = row.selectFirst("td.godzina")
                    if (timeCell != null && currentDate != null) {
                        val timeRange = parseTimeRange(timeCell.text().trim())
                        if (timeRange != null) {
                            val (defaultStartTime, defaultEndTime) = timeRange
                            val dataCells = row.select("td").filter { !it.hasClass("godzina") }

                            for (subgroupIndex in subgroups.indices) {
                                val cellBaseIndex = subgroupIndex * 3
                                if (cellBaseIndex + 2 < dataCells.size) {
                                    val subjectCell = dataCells[cellBaseIndex]
                                    val teacherCell = dataCells[cellBaseIndex + 1]
                                    val roomCell = dataCells[cellBaseIndex + 2]

                                    val parsedLessons = parseLessonCells(
                                        subjectCell = subjectCell,
                                        teacherCell = teacherCell,
                                        roomCell = roomCell,
                                        date = currentDate,
                                        defaultStartTime = defaultStartTime,
                                        defaultEndTime = defaultEndTime,
                                        subgroup = subgroups[subgroupIndex],
                                        legend = legend
                                    )
                                    lessons.addAll(parsedLessons)
                                }
                            }
                        }
                    }
                }
            }
        }

        return ScheduleResult(
            groupCode = groupCode,
            lessons = lessons,
            weeks = weeks,
            legend = legend,
            subgroups = subgroups,
            variants = variants
        )
    }

    private fun parseWeekSelector(doc: Element): List<WeekOption> {
        val options = doc.select("select[name='dzien'] option")
        return options.map { opt ->
            WeekOption(
                value = opt.attr("value").trim(),
                label = opt.text().trim(),
                isSelected = opt.hasAttr("selected")
            )
        }
    }

    private fun normalizeCode(str: String): String =
        str.replace('\u00A0', ' ')
            .replace("&nbsp;", " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

    private fun parseLegend(doc: Element): Map<String, String> {
        val legendMap = mutableMapOf<String, String>()
        val allTables = doc.select("table")
        for (table in allTables) {
            val isLegendTable = table.hasClass("TabLegenda") ||
                table.parents().any { it.id().equals("prtleg", ignoreCase = true) || it.attr("name").equals("prnt", ignoreCase = true) } ||
                table.select("th, td").any { it.text().contains("skr", ignoreCase = true) }
            if (isLegendTable) {
                val rows = table.select("tr")
                for (row in rows) {
                    val cells = row.select("td")
                    if (cells.size >= 2) {
                        val shortCode = normalizeCode(cells[0].text())
                        val fullName = normalizeCode(cells[1].text())
                        if (shortCode.isNotBlank() && fullName.isNotBlank() &&
                            !shortCode.startsWith("Skr", ignoreCase = true) &&
                            shortCode != "P" && shortCode != "N" &&
                            !shortCode.equals("Przedmiot", ignoreCase = true)
                        ) {
                            legendMap[shortCode] = fullName
                        }
                    }
                }
            }
        }
        return legendMap
    }

    private fun parseVariantTables(doc: Element): List<VariantOption> {
        val variants = mutableListOf<VariantOption>()
        val allTables = doc.select("table")
        for (table in allTables) {
            val th = table.selectFirst("th")
            val title = th?.text()?.trim().orEmpty()
            if (title.contains("Legenda", ignoreCase = true)) {
                val groupName = Regex("""(Legenda\d+)""", RegexOption.IGNORE_CASE).find(title)?.value ?: title
                val rows = table.select("tr")
                for (row in rows) {
                    val cells = row.select("td")
                    if (cells.size >= 3) {
                        val subj = cells[0].text().trim()
                        val teacher = cells[1].text().trim()
                        val room = cells[2].text().trim()
                        if (subj != "Przedmiot" && subj.isNotBlank()) {
                            variants.add(
                                VariantOption(
                                    groupName = groupName,
                                    subjectShort = subj,
                                    teacher = teacher,
                                    room = room
                                )
                            )
                        }
                    }
                }
            }
        }
        return variants
    }

    private fun parseLessonCells(
        subjectCell: Element,
        teacherCell: Element,
        roomCell: Element,
        date: LocalDate,
        defaultStartTime: LocalTime,
        defaultEndTime: LocalTime,
        subgroup: String,
        legend: Map<String, String>
    ): List<Lesson> {
        val subjectHtml = subjectCell.html()
        val teacherHtml = teacherCell.html()
        val roomHtml = roomCell.html()

        // Check if block has multiple parity items separated by <hr>
        if (subjectHtml.contains("<hr", ignoreCase = true)) {
            val subjectParts = subjectHtml.split(Regex("""<hr[^>]*>""", RegexOption.IGNORE_CASE))
            val teacherParts = teacherHtml.split(Regex("""<hr[^>]*>""", RegexOption.IGNORE_CASE))
            val roomParts = roomHtml.split(Regex("""<hr[^>]*>""", RegexOption.IGNORE_CASE))

            val result = mutableListOf<Lesson>()
            for (idx in subjectParts.indices) {
                val rawSubj = cleanHtml(subjectParts[idx])
                val rawTeacher = if (idx < teacherParts.size) cleanHtml(teacherParts[idx]) else ""
                val rawRoom = if (idx < roomParts.size) cleanHtml(roomParts[idx]) else ""
                val parity = if (idx == 0) WeekParity.EVEN else WeekParity.ODD

                parseSingleLesson(
                    rawSubject = rawSubj,
                    rawTeacher = rawTeacher,
                    rawRoom = rawRoom,
                    date = date,
                    defaultStartTime = defaultStartTime,
                    defaultEndTime = defaultEndTime,
                    subgroup = subgroup,
                    parity = parity,
                    legend = legend
                )?.let { result.add(it) }
            }
            return result
        } else {
            val rawSubj = cleanHtml(subjectHtml)
            val rawTeacher = cleanHtml(teacherHtml)
            val rawRoom = cleanHtml(roomHtml)

            val lesson = parseSingleLesson(
                rawSubject = rawSubj,
                rawTeacher = rawTeacher,
                rawRoom = rawRoom,
                date = date,
                defaultStartTime = defaultStartTime,
                defaultEndTime = defaultEndTime,
                subgroup = subgroup,
                parity = WeekParity.ALL,
                legend = legend
            )
            return if (lesson != null) listOf(lesson) else emptyList()
        }
    }

    private fun parseSingleLesson(
        rawSubject: String,
        rawTeacher: String,
        rawRoom: String,
        date: LocalDate,
        defaultStartTime: LocalTime,
        defaultEndTime: LocalTime,
        subgroup: String,
        parity: WeekParity,
        legend: Map<String, String>
    ): Lesson? {
        val subjTrimmed = rawSubject.trim()
        val teacherTrimmed = rawTeacher.trim()
        val roomTrimmed = rawRoom.trim()

        if (subjTrimmed.isEmpty() || subjTrimmed == "-" || (subjTrimmed == "" && roomTrimmed.isEmpty())) {
            return null
        }

        // Check 45-minute blocks: 1h [x/-] or 1h [-/x]
        var (startTime, endTime) = Pair(defaultStartTime, defaultEndTime)
        var cleanSubject = subjTrimmed

        if (subjTrimmed.contains("[x/-]", ignoreCase = true) || subjTrimmed.contains("[x/ -]", ignoreCase = true)) {
            // First 45 minutes
            endTime = startTime.plusMinutes(45)
            cleanSubject = cleanSubject.replace(Regex("""1h\s*\[[xX]/\s*-\]|\b\[[xX]/\s*-\]"""), "").trim()
        } else if (subjTrimmed.contains("[-/x]", ignoreCase = true) || subjTrimmed.contains("[- /x]", ignoreCase = true)) {
            // Second 45 minutes
            startTime = endTime.minusMinutes(45)
            cleanSubject = cleanSubject.replace(Regex("""1h\s*\[\s*-/[xX]\]|\b\[\s*-/[xX]\]"""), "").trim()
        }

        // Extract raw type in parentheses, e.g. "Pig (wyk)" -> "Pig", "wyk"
        val typeMatch = Regex("""\(([^)]+)\)""").find(cleanSubject)
        val rawType = typeMatch?.groupValues?.get(1)?.trim().orEmpty()
        val subjectShort = cleanSubject.replace(Regex("""\([^)]+\)"""), "").trim()
        val lessonType = LessonType.fromRaw(rawType)
        val normSubject = normalizeCode(subjectShort)
        val subjectFull = legend.entries.firstOrNull {
            normalizeCode(it.key).equals(normSubject, ignoreCase = true)
        }?.value ?: legend[normSubject] ?: legend[subjectShort] ?: subjectShort
        val onlineMatch = Regex("""online[_\s]*(\d+)?""", RegexOption.IGNORE_CASE).find(roomTrimmed)
        val isOnline = onlineMatch != null
        val onlineId = onlineMatch?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }

        // Variant check: "Różne" or "LegendaX"
        val isVariant = roomTrimmed.contains("różne", ignoreCase = true) ||
            roomTrimmed.contains("r?ne", ignoreCase = true) ||
            teacherTrimmed.contains("legenda", ignoreCase = true)
        val variantGroupName = if (teacherTrimmed.contains("legenda", ignoreCase = true)) {
            teacherTrimmed
        } else null

        return Lesson(
            date = date,
            startTime = startTime,
            endTime = endTime,
            subjectShort = subjectShort,
            subjectFull = subjectFull,
            type = lessonType,
            rawType = rawType,
            room = roomTrimmed,
            isOnline = isOnline,
            onlineId = onlineId,
            teacher = teacherTrimmed,
            subgroup = subgroup,
            weekParity = parity,
            changeFlag = ChangeFlag.NONE,
            isVariant = isVariant,
            variantGroupName = variantGroupName
        )
    }

    private fun cleanHtml(html: String): String {
        return html
            .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""<[^>]*>"""), "")
            .replace("&nbsp;", " ")
            .trim()
    }

    private fun parseDate(dateStr: String): LocalDate? {
        // Format YYYY-MM-DD (np. w widoku semestralnym "Czwartek 2026-10-01")
        val ymd = Regex("""\b(\d{4})-(\d{2})-(\d{2})\b""").find(dateStr)
        if (ymd != null) {
            val (year, month, day) = ymd.destructured
            return LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        }

        // Format DD.MM.YYYY lub DD-MM-YYYY (np. w widoku tygodniowym "Czwartek 01.10.2026")
        val dmy = Regex("""\b(\d{2})[.-](\d{2})[.-](\d{4})\b""").find(dateStr)
        if (dmy != null) {
            val (day, month, year) = dmy.destructured
            return LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        }

        return null
    }

    private fun parseTimeRange(timeStr: String): Pair<LocalTime, LocalTime>? {
        val match = Regex("""(\d{1,2}):(\d{2})\s*-\s*(\d{1,2}):(\d{2})""").find(timeStr) ?: return null
        val (h1, m1, h2, m2) = match.destructured
        return Pair(
            LocalTime.of(h1.toInt(), m1.toInt()),
            LocalTime.of(h2.toInt(), m2.toInt())
        )
    }
}
