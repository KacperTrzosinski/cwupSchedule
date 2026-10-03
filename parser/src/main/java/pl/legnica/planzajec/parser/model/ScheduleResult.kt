package pl.legnica.planzajec.parser.model

data class WeekOption(
    val value: String, // e.g. "28-09-2026"
    val label: String, // e.g. "28-09-2026 - 04-10-2026 (40 tydzien roku)"
    val isSelected: Boolean
)

data class Teacher(
    val id: String,
    val name: String,
    val departmentId: Int
)

data class Room(
    val id: String,
    val name: String,
    val building: String
)

data class VariantOption(
    val groupName: String, // e.g. "Legenda1"
    val subjectShort: String,
    val teacher: String,
    val room: String
)

data class ScheduleResult(
    val groupCode: String,
    val lessons: List<Lesson>,
    val weeks: List<WeekOption>,
    val legend: Map<String, String>,
    val subgroups: List<String>,
    val variants: List<VariantOption> = emptyList()
)
