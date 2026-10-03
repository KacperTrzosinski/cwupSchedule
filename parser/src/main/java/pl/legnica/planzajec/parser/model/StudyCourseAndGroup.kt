package pl.legnica.planzajec.parser.model

data class StudyCourse(
    val name: String,
    val departmentId: Int,
    val isFullTime: Boolean,
    val year: Int
)

data class StudyGroup(
    val code: String,
    val name: String,
    val courseName: String,
    val departmentId: Int,
    val isFullTime: Boolean,
    val year: Int
)
