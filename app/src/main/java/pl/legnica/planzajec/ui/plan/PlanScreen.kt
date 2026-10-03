package pl.legnica.planzajec.ui.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.data.preferences.ViewMode
import pl.legnica.planzajec.ui.theme.DarkBackground
import pl.legnica.planzajec.ui.theme.DarkSurface
import pl.legnica.planzajec.ui.theme.DarkSurfaceBorder
import pl.legnica.planzajec.ui.theme.DarkSurfaceElevated
import pl.legnica.planzajec.ui.theme.TextMuted
import pl.legnica.planzajec.ui.theme.TextPrimary
import pl.legnica.planzajec.ui.theme.TextSecondary
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    viewModel: PlanViewModel,
    onTeacherClick: (String) -> Unit = {},
    onRoomClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showWeekMenu by remember { mutableStateOf(false) }
    var showSubgroupDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.groupCode.ifBlank { "Plan zajęć" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (state.subgroup != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF06B6D4).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = state.subgroup.orEmpty(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF06B6D4)
                                    )
                                }
                            }
                        }
                        if (state.lastUpdated.isNotBlank()) {
                            Text(
                                text = "Aktualizacja: ${state.lastUpdated}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Week selector button
                    if (state.availableWeeks.isNotEmpty()) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showWeekMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Tydzień",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = state.availableWeeks.find { it.isSelected }?.label?.take(15) ?: "Tydzień",
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showWeekMenu,
                                onDismissRequest = { showWeekMenu = false }
                            ) {
                                state.availableWeeks.forEach { week ->
                                    DropdownMenuItem(
                                        text = { Text(week.label, fontSize = 13.sp) },
                                        onClick = {
                                            viewModel.selectWeek(week.value)
                                            showWeekMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // View Mode Toggle (Upcoming vs Day by day)
                    IconButton(onClick = {
                        val newMode = if (state.viewMode == ViewMode.UPCOMING) ViewMode.DAY_BY_DAY else ViewMode.UPCOMING
                        viewModel.setViewMode(newMode)
                    }) {
                        Icon(
                            imageVector = if (state.viewMode == ViewMode.UPCOMING) Icons.Default.CalendarToday else Icons.Default.ViewAgenda,
                            contentDescription = "Zmień widok",
                            tint = Color(0xFF00D2FF)
                        )
                    }

                    // Subgroup filter button
                    if (state.availableSubgroups.size > 1) {
                        IconButton(onClick = { showSubgroupDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtruj podgrupę",
                                tint = TextSecondary
                            )
                        }
                    }

                    // Refresh Button
                    IconButton(onClick = { viewModel.refresh() }, enabled = !state.isRefreshing) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF00D2FF)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Odśwież",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Offline banner if error occurred
                AnimatedVisibility(visible = state.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = state.errorMessage.orEmpty(),
                            color = Color(0xFFF59E0B),
                            fontSize = 12.sp
                        )
                    }
                }

                if (state.lessons.isEmpty() && !state.isRefreshing) {
                    EmptyStateView()
                } else {
                    when (state.viewMode) {
                        ViewMode.UPCOMING -> UpcomingLessonsView(
                            lessons = state.lessons,
                            onTeacherClick = onTeacherClick,
                            onRoomClick = onRoomClick
                        )
                        ViewMode.DAY_BY_DAY -> DayByDayView(
                            lessons = state.lessons,
                            onTeacherClick = onTeacherClick,
                            onRoomClick = onRoomClick
                        )
                    }
                }
            }
        }
    }

    // Subgroup Filter Dialog
    if (showSubgroupDialog) {
        AlertDialog(
            onDismissRequest = { showSubgroupDialog = false },
            containerColor = DarkSurfaceElevated,
            title = { Text("Wybierz podgrupę", color = TextPrimary) },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (state.subgroup == null) Color(0xFF06B6D4).copy(alpha = 0.2f) else DarkSurface)
                            .clickable {
                                viewModel.selectSubgroup(null)
                                showSubgroupDialog = false
                            }
                            .padding(12.dp)
                    ) {
                        Text("Pokaż wszystkie", color = TextPrimary, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    state.availableSubgroups.forEach { sg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (state.subgroup == sg) Color(0xFF06B6D4).copy(alpha = 0.2f) else DarkSurface)
                                .clickable {
                                    viewModel.selectSubgroup(sg)
                                    showSubgroupDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Text(sg, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun UpcomingLessonsView(
    lessons: List<LessonEntity>,
    onTeacherClick: (String) -> Unit,
    onRoomClick: (String) -> Unit
) {
    val today = LocalDate.now()
    val groupedByDate = lessons.groupBy { it.date }.toSortedMap()

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        groupedByDate.forEach { (date, dayLessons) ->
            // Sticky Day Header
            item(key = "header_${date}") {
                DayHeader(date = date, count = dayLessons.size, isToday = date == today)
            }

            // Lessons in day
            for (i in dayLessons.indices) {
                val current = dayLessons[i]

                item(key = "lesson_${current.id}_${current.date}_${current.startTime}") {
                    LessonCard(
                        lesson = current,
                        onTeacherClick = onTeacherClick,
                        onRoomClick = onRoomClick
                    )
                }

                // Break ("Okienko") indicator between consecutive lessons
                if (i < dayLessons.size - 1) {
                    val next = dayLessons[i + 1]
                    val breakMinutes = Duration.between(current.endTime, next.startTime).toMinutes()
                    if (breakMinutes >= 30) {
                        item(key = "break_${current.id}_${next.id}") {
                            BreakIndicator(minutes = breakMinutes)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayByDayView(
    lessons: List<LessonEntity>,
    onTeacherClick: (String) -> Unit,
    onRoomClick: (String) -> Unit
) {
    val today = LocalDate.now()
    val allDates = lessons.map { it.date }.distinct().sorted()
    val defaultDate = allDates.find { !it.isBefore(today) } ?: allDates.firstOrNull() ?: today
    var selectedDate by remember(allDates) { mutableStateOf(defaultDate) }

    val dayLessons = lessons.filter { it.date == selectedDate }.sortedBy { it.startTime }

    Column(modifier = Modifier.fillMaxSize()) {
        // Date Selector Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allDates) { date ->
                val isSelected = date == selectedDate
                val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale("pl"))
                val dateNumFormatter = DateTimeFormatter.ofPattern("d.MM")

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF00D2FF) else DarkSurface)
                        .border(1.dp, if (isSelected) Color(0xFF00D2FF) else DarkSurfaceBorder, RoundedCornerShape(12.dp))
                        .clickable { selectedDate = date }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = date.format(dayFormatter).replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Color.Black else TextSecondary
                        )
                        Text(
                            text = date.format(dateNumFormatter),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }
            }
        }

        // Quick Jump to Today
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (allDates.contains(today) && selectedDate != today) {
                TextButton(onClick = { selectedDate = today }) {
                    Text("Idź do Dziś", fontSize = 12.sp, color = Color(0xFF00D2FF))
                }
            }
        }

        // Lessons for selected date
        if (dayLessons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Brak zajęć w wybranym dniu", color = TextSecondary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dayLessons, key = { "${it.id}_${it.startTime}" }) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        onTeacherClick = onTeacherClick,
                        onRoomClick = onRoomClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate, count: Int, isToday: Boolean) {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    val label = when (date) {
        today -> "Dziś"
        tomorrow -> "Jutro"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))
            date.format(formatter).replaceFirstChar { it.uppercase() }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isToday) Color(0xFF00D2FF) else TextMuted)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isToday) Color(0xFF00D2FF) else TextPrimary
            )
        }

        Text(
            text = "$count zajęć",
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun BreakIndicator(minutes: Long) {
    val hours = minutes / 60
    val remMinutes = minutes % 60
    val text = if (hours > 0 && remMinutes > 0) {
        "Okienko: ${hours}h ${remMinutes}m"
    } else if (hours > 0) {
        "Okienko: ${hours}h"
    } else {
        "Okienko: ${remMinutes}m"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Brak zajęć w tym okresie",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Sprawdź inny tydzień lub odśwież plan",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
