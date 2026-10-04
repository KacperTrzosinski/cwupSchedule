package pl.legnica.planzajec.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import pl.legnica.planzajec.MainActivity
import pl.legnica.planzajec.data.local.dao.LessonDao
import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.data.preferences.UserPreferencesRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun lessonDao(): LessonDao
    fun preferencesRepository(): UserPreferencesRepository
}

class ScheduleGlanceWidget : GlanceAppWidget() {

    companion object {
        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(220.dp, 100.dp)
        private val BIG_SQUARE = DpSize(220.dp, 220.dp)

        private val BG_COLOR = Color(0xFF0B0E14)
        private val CARD_COLOR = Color(0xFF161B22)
        private val CYAN_ACCENT = Color(0xFF00D2FF)
        private val TEXT_PRIMARY = Color(0xFFF0F6FC)
        private val TEXT_MUTED = Color(0xFF8B949E)
        private val ONLINE_COLOR = Color(0xFF38BDF8)

        private fun color(c: Color): ColorProvider = ColorProvider(c)

        suspend fun updateWidget(context: Context) {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(ScheduleGlanceWidget::class.java)
                glanceIds.forEach { id ->
                    ScheduleGlanceWidget().update(context, id)
                }
            } catch (_: Exception) {
                // Ignore widget update errors during background execution
            }
        }
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, BIG_SQUARE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val prefs = entryPoint.preferencesRepository().userPreferencesFlow.first()
        val groupCode = prefs.selectedGroupCode.orEmpty()
        val subgroup = prefs.selectedSubgroup
        val today = LocalDate.now()
        val nowTime = LocalTime.now()

        val allUpcoming = if (groupCode.isNotBlank()) {
            entryPoint.lessonDao().getUpcomingLessons(groupCode, today).first()
                .filter { subgroup.isNullOrBlank() || it.subgroup.isNullOrBlank() || it.subgroup == subgroup }
                .filter { lesson ->
                    lesson.date.isAfter(today) || (lesson.date == today && nowTime.isBefore(lesson.endTime))
                }
        } else {
            emptyList()
        }

        provideContent {
            val size = LocalSize.current

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(color(BG_COLOR))
                        .cornerRadius(16.dp)
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.TopStart
                ) {
                    when {
                        size.width >= BIG_SQUARE.width && size.height >= BIG_SQUARE.height -> {
                            LargeWidgetContent(groupCode = groupCode, lessons = allUpcoming)
                        }
                        size.width >= HORIZONTAL_RECTANGLE.width -> {
                            MediumWidgetContent(groupCode = groupCode, lessons = allUpcoming)
                        }
                        else -> {
                            SmallWidgetContent(groupCode = groupCode, nextLesson = allUpcoming.firstOrNull())
                        }
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun SmallWidgetContent(groupCode: String, nextLesson: LessonEntity?) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = groupCode.ifBlank { "Plan" },
                    style = TextStyle(
                        color = color(CYAN_ACCENT),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            if (nextLesson != null) {
                Text(
                    text = "${nextLesson.startTime} - ${nextLesson.endTime}",
                    style = TextStyle(
                        color = color(TEXT_PRIMARY),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = nextLesson.subjectFull.ifBlank { nextLesson.subjectShort },
                    maxLines = 2,
                    style = TextStyle(
                        color = color(TEXT_PRIMARY),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                val locationText = if (nextLesson.isOnline) "ONLINE" else "Sala ${nextLesson.room}"
                val locationColor = if (nextLesson.isOnline) ONLINE_COLOR else CYAN_ACCENT
                Text(
                    text = locationText,
                    style = TextStyle(
                        color = color(locationColor),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            } else {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (groupCode.isBlank()) "Wybierz grupę" else "Brak zajęć",
                        style = TextStyle(
                            color = color(TEXT_MUTED),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun MediumWidgetContent(groupCode: String, lessons: List<LessonEntity>) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = groupCode.ifBlank { "Plan zajęć" },
                    style = TextStyle(
                        color = color(CYAN_ACCENT),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                val todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM", Locale("pl")))
                Text(
                    text = todayDate,
                    style = TextStyle(
                        color = color(TEXT_MUTED),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            val displayLessons = lessons.take(2)
            if (displayLessons.isNotEmpty()) {
                var lastDate: LocalDate? = null
                displayLessons.forEachIndexed { index, lesson ->
                    if (lastDate == null || lesson.date != lastDate) {
                        WidgetDayDivider(date = lesson.date, isFirst = index == 0)
                        lastDate = lesson.date
                    } else if (index > 0) {
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }
                    WidgetLessonRow(lesson = lesson)
                }
            } else {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (groupCode.isBlank()) "Wybierz grupę w aplikacji" else "Brak zaplanowanych zajęć",
                        style = TextStyle(
                            color = color(TEXT_MUTED),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun LargeWidgetContent(groupCode: String, lessons: List<LessonEntity>) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = groupCode.ifBlank { "Plan zajęć" },
                    style = TextStyle(
                        color = color(CYAN_ACCENT),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                val todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl")))
                Text(
                    text = todayDate,
                    style = TextStyle(
                        color = color(TEXT_MUTED),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            val displayLessons = lessons.take(5)
            if (displayLessons.isNotEmpty()) {
                var lastDate: LocalDate? = null
                displayLessons.forEachIndexed { index, lesson ->
                    if (lastDate == null || lesson.date != lastDate) {
                        WidgetDayDivider(date = lesson.date, isFirst = index == 0)
                        lastDate = lesson.date
                    } else if (index > 0) {
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }
                    WidgetLessonRow(lesson = lesson)
                }
            } else {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (groupCode.isBlank()) "Wybierz grupę w aplikacji" else "Brak zaplanowanych zajęć",
                        style = TextStyle(
                            color = color(TEXT_MUTED),
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetDayDivider(date: LocalDate, isFirst: Boolean) {
        val today = LocalDate.now()
        val dayLabel = when (date) {
            today -> "DZISIAJ"
            today.plusDays(1) -> "JUTRO"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, d.MM", Locale("pl"))).replaceFirstChar { it.uppercase() }
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(top = if (isFirst) 0.dp else 4.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayLabel,
                style = TextStyle(
                    color = color(CYAN_ACCENT),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(1.dp)
                    .background(color(Color(0xFF334155)))
            ) {}
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetLessonRow(lesson: LessonEntity) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(color(CARD_COLOR))
                .cornerRadius(8.dp)
                .padding(8.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.width(52.dp)) {
                    Text(
                        text = lesson.startTime.toString(),
                        style = TextStyle(
                            color = color(TEXT_PRIMARY),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = lesson.endTime.toString(),
                        style = TextStyle(
                            color = color(TEXT_MUTED),
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = lesson.subjectFull.ifBlank { lesson.subjectShort },
                        maxLines = 1,
                        style = TextStyle(
                            color = color(TEXT_PRIMARY),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = lesson.teacher.ifBlank { lesson.rawType },
                        maxLines = 1,
                        style = TextStyle(
                            color = color(TEXT_MUTED),
                            fontSize = 9.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(6.dp))

                val roomLabel = if (lesson.isOnline) "ONLINE" else lesson.room.ifBlank { "—" }
                val roomColor = if (lesson.isOnline) ONLINE_COLOR else CYAN_ACCENT
                Text(
                    text = roomLabel,
                    style = TextStyle(
                        color = color(roomColor),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

class ScheduleGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleGlanceWidget()
}
