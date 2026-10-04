package pl.legnica.planzajec.ui.plan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.data.preferences.ViewMode
import pl.legnica.planzajec.ui.theme.DarkSurface
import pl.legnica.planzajec.ui.theme.DarkSurfaceBorder
import pl.legnica.planzajec.ui.theme.GlassTokens
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
    var showSubgroupDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.groupCode.ifBlank { "Plan zajęć" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (state.availableSubgroups.isNotEmpty() || !state.subgroup.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                val labelText = state.subgroup ?: "Wszystkie grupy"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF00D2FF).copy(alpha = 0.15f))
                                        .border(1.dp, Color(0xFF00D2FF).copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                        .clickable { showSubgroupDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = labelText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00D2FF),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        if (state.lastUpdated.isNotBlank()) {
                            Text(
                                text = "Semestr · Aktualizacja: ${state.lastUpdated}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
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
                        ViewMode.UPCOMING -> {
                            UpcomingLessonsView(
                                lessons = state.lessons,
                                onTeacherClick = onTeacherClick,
                                onRoomClick = onRoomClick
                            )
                        }
                        ViewMode.DAY_BY_DAY -> {
                            DayByDayView(
                                lessons = state.lessons,
                                onTeacherClick = onTeacherClick,
                                onRoomClick = onRoomClick
                            )
                        }
                    }
                }
            }
        }
    }

    // Subgroup Selection Dialog
    if (showSubgroupDialog) {
        AlertDialog(
            onDismissRequest = { showSubgroupDialog = false },
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, GlassTokens.BorderBrush, RoundedCornerShape(20.dp)),
            containerColor = Color(0xFF0F172A),
            title = {
                Text(
                    text = "Wybierz podgrupę",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isAllSelected = state.subgroup == null
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isAllSelected) Color(0xFF00D2FF).copy(alpha = 0.16f) else Color(0xFF131C2E).copy(alpha = 0.70f))
                            .border(1.dp, if (isAllSelected) Color(0xFF00D2FF) else DarkSurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.selectSubgroup(null)
                                showSubgroupDialog = false
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pokaż wszystkie",
                                color = if (isAllSelected) Color(0xFF00D2FF) else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (isAllSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF00D2FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    state.availableSubgroups.forEach { sg ->
                        val isSgSelected = state.subgroup == sg
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSgSelected) Color(0xFF00D2FF).copy(alpha = 0.16f) else Color(0xFF131C2E).copy(alpha = 0.70f))
                            .border(1.dp, if (isSgSelected) Color(0xFF00D2FF) else DarkSurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                    viewModel.selectSubgroup(sg)
                                    showSubgroupDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sg,
                                    color = if (isSgSelected) Color(0xFF00D2FF) else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSgSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                if (isSgSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00D2FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
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
    val upcomingLessons = lessons.filter { !it.date.isBefore(today) }
    val groupedByDate = upcomingLessons.groupBy { it.date }.toSortedMap()

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
    val futureDates = lessons.map { it.date }.filter { !it.isBefore(today) }.distinct().sorted()
    val allDates = if (futureDates.isNotEmpty()) futureDates else lessons.map { it.date }.distinct().sorted()
    val defaultDate = allDates.find { !it.isBefore(today) } ?: allDates.firstOrNull() ?: today
    var selectedDate by remember(allDates) { mutableStateOf(defaultDate) }

    val listState = rememberLazyListState()

    // Auto-scroll the date selector when selectedDate changes
    LaunchedEffect(selectedDate, allDates) {
        val idx = allDates.indexOf(selectedDate)
        if (idx >= 0) {
            listState.animateScrollToItem(idx)
        }
    }

    var totalDragX by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Date Selector Row
        LazyRow(
            state = listState,
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
                        .background(if (isSelected) Color(0xFF00D2FF) else Color(0xFF0F172A).copy(alpha = 0.55f))
                        .border(1.dp, if (isSelected) SolidColor(Color(0xFF00D2FF)) else GlassTokens.BorderBrush, RoundedCornerShape(12.dp))
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
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Przeciągnij w lewo/prawo, aby zmienić dzień",
                fontSize = 11.sp,
                color = TextMuted
            )
            if (allDates.contains(today) && selectedDate != today) {
                TextButton(onClick = { selectedDate = today }) {
                    Text("Idź do Dziś", fontSize = 12.sp, color = Color(0xFF00D2FF), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Lessons for selected date with swipe gesture detection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedDate, allDates) {
                    detectHorizontalDragGestures(
                        onDragStart = { totalDragX = 0f },
                        onDragEnd = {
                            val currentIndex = allDates.indexOf(selectedDate)
                            if (totalDragX < -50f && currentIndex < allDates.size - 1) {
                                // Swiped Left -> Next day
                                selectedDate = allDates[currentIndex + 1]
                            } else if (totalDragX > 50f && currentIndex > 0) {
                                // Swiped Right -> Previous day
                                selectedDate = allDates[currentIndex - 1]
                            }
                            totalDragX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            totalDragX += dragAmount
                        }
                    )
                }
        ) {
            AnimatedContent(
                targetState = selectedDate,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width / 3 } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width / 3 } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width / 3 } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width / 3 } + fadeOut()
                    }
                },
                label = "dayTransition"
            ) { date ->
                val dayLessons = lessons.filter { it.date == date }.sortedBy { it.startTime }
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
    }
}

@Composable
private fun DayHeader(date: LocalDate, count: Int, isToday: Boolean) {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    val label = when (date) {
        today -> "Dziś"
        tomorrow -> "Jutro"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE", Locale("pl"))).replaceFirstChar { it.uppercase() }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isToday) Color(0xFF00D2FF) else TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = date.format(dateFormatter),
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.60f))
                .border(1.dp, GlassTokens.BorderBrush, RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "$count zajęć",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.50f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = Color(0xFF00D2FF),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
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
            text = "Odśwież plan lub zmień filtry",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
