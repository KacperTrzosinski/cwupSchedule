package pl.legnica.planzajec.parser.model

enum class LessonType(val displayName: String) {
    LECTURE("Wykład"),
    LABORATORY("Laboratoria"),
    EXERCISE("Ćwiczenia"),
    SEMINAR("Seminarium"),
    PROJECT("Projekt"),
    WORKSHOP("Warsztaty"),
    FOREIGN_LANGUAGE("Lektorat"),
    OTHER("Inne");

    companion object {
        fun fromRaw(raw: String): LessonType {
            val cleaned = raw.trim().lowercase().removeSurrounding("(", ")")
            return when {
                cleaned == "wyk" || cleaned.contains("wyk") -> LECTURE
                cleaned == "lab" || cleaned.contains("lab") -> LABORATORY
                cleaned == "ćw" || cleaned == "cw" || cleaned == "c" || cleaned.contains("ćw") || cleaned.contains("cw") -> EXERCISE
                cleaned == "sem" || cleaned.contains("sem") -> SEMINAR
                cleaned == "p" || cleaned == "proj" || cleaned.contains("projekt") -> PROJECT
                cleaned == "warszt" || cleaned.contains("warsztat") -> WORKSHOP
                cleaned == "lekt" || cleaned.contains("lektorat") -> FOREIGN_LANGUAGE
                else -> OTHER
            }
        }
    }
}

enum class WeekParity(val displayName: String) {
    ALL("Każdy"),
    EVEN("Parzysty"),
    ODD("Nieparzysty")
}

enum class ChangeFlag {
    NONE,
    NEW,
    CHANGED_ROOM_OR_TIME,
    CANCELLED
}
