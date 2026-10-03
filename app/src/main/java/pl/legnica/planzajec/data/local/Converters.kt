package pl.legnica.planzajec.data.local

import androidx.room.TypeConverter
import pl.legnica.planzajec.parser.model.ChangeFlag
import pl.legnica.planzajec.parser.model.LessonType
import pl.legnica.planzajec.parser.model.WeekParity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.format(dateFormatter)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, dateFormatter) }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.format(timeFormatter)

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it, timeFormatter) }

    @TypeConverter
    fun fromLessonType(value: LessonType?): String? = value?.name

    @TypeConverter
    fun toLessonType(value: String?): LessonType? = value?.let { LessonType.valueOf(it) }

    @TypeConverter
    fun fromWeekParity(value: WeekParity?): String? = value?.name

    @TypeConverter
    fun toWeekParity(value: String?): WeekParity? = value?.let { WeekParity.valueOf(it) }

    @TypeConverter
    fun fromChangeFlag(value: ChangeFlag?): String? = value?.name

    @TypeConverter
    fun toChangeFlag(value: String?): ChangeFlag? = value?.let { ChangeFlag.valueOf(it) }
}
