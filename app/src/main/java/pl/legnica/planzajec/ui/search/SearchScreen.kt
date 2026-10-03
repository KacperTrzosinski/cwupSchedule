package pl.legnica.planzajec.ui.search

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.data.local.entity.RoomEntity
import pl.legnica.planzajec.data.local.entity.StudyGroupEntity
import pl.legnica.planzajec.data.local.entity.TeacherEntity
import pl.legnica.planzajec.ui.plan.LessonCard
import pl.legnica.planzajec.ui.theme.DarkBackground
import pl.legnica.planzajec.ui.theme.DarkSurface
import pl.legnica.planzajec.ui.theme.DarkSurfaceBorder
import pl.legnica.planzajec.ui.theme.DarkSurfaceElevated
import pl.legnica.planzajec.ui.theme.TextMuted
import pl.legnica.planzajec.ui.theme.TextPrimary
import pl.legnica.planzajec.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onGroupSelected: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Szukaj",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.setQuery(it) },
                placeholder = { Text("Szukaj grupy, prowadzącego lub sali...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00D2FF))
                },
                trailingIcon = {
                    if (state.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Wyczyść", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00D2FF),
                    unfocusedBorderColor = Color(0xFF38BDF8).copy(alpha = 0.25f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.70f),
                    unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.55f)
                )
            )

            // Tabs Selector
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SearchTab.values()) { tab ->
                    val isSelected = state.activeTab == tab
                    val tabName = when (tab) {
                        SearchTab.ALL -> "Wszystko"
                        SearchTab.GROUPS -> "Grupy"
                        SearchTab.TEACHERS -> "Prowadzący"
                        SearchTab.ROOMS -> "Sale"
                        SearchTab.FREE_ROOMS -> "Wolne sale teraz"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF00D2FF) else DarkSurface)
                            .border(1.dp, if (isSelected) Color(0xFF00D2FF) else DarkSurfaceBorder, RoundedCornerShape(20.dp))
                            .clickable { viewModel.setTab(tab) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = tabName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Results List
            val query = state.query.trim().lowercase()

            val filteredGroups = if (state.activeTab == SearchTab.ALL || state.activeTab == SearchTab.GROUPS) {
                state.groups.filter { it.code.lowercase().contains(query) || it.name.lowercase().contains(query) }
            } else emptyList()

            val filteredTeachers = if (state.activeTab == SearchTab.ALL || state.activeTab == SearchTab.TEACHERS) {
                state.teachers.filter { it.name.lowercase().contains(query) }
            } else emptyList()

            val filteredRooms = if (state.activeTab == SearchTab.ALL || state.activeTab == SearchTab.ROOMS) {
                state.rooms.filter { it.name.lowercase().contains(query) || it.building.lowercase().contains(query) }
            } else emptyList()

            val freeRooms = if (state.activeTab == SearchTab.FREE_ROOMS) {
                state.freeRoomsNow.filter { it.name.lowercase().contains(query) }
            } else emptyList()

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Free rooms
                if (state.activeTab == SearchTab.FREE_ROOMS) {
                    items(freeRooms) { room ->
                        RoomResultItem(room = room, onClick = { viewModel.openRoomSchedule(room) })
                    }
                }

                // Groups
                if (filteredGroups.isNotEmpty()) {
                    item { SectionHeader("Grupy (${filteredGroups.size})") }
                    items(filteredGroups.take(15)) { group ->
                        GroupResultItem(group = group, onClick = { onGroupSelected(group.code) })
                    }
                }

                // Teachers
                if (filteredTeachers.isNotEmpty()) {
                    item { SectionHeader("Prowadzący (${filteredTeachers.size})") }
                    items(filteredTeachers.take(20)) { teacher ->
                        TeacherResultItem(teacher = teacher, onClick = { viewModel.openTeacherSchedule(teacher) })
                    }
                }

                // Rooms
                if (filteredRooms.isNotEmpty()) {
                    item { SectionHeader("Sale (${filteredRooms.size})") }
                    items(filteredRooms.take(20)) { room ->
                        RoomResultItem(room = room, onClick = { viewModel.openRoomSchedule(room) })
                    }
                }
            }
        }
    }

    // Schedule Preview Dialog (when clicked on a teacher or room)
    if (state.selectedScheduleResult != null || state.isScheduleLoading) {
        AlertDialog(
            onDismissRequest = { viewModel.closeSchedulePreview() },
            containerColor = DarkSurfaceElevated,
            title = {
                Text(
                    text = state.selectedTitle ?: "Plan zajęć",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                if (state.isScheduleLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00D2FF))
                    }
                } else {
                    val lessons = state.selectedScheduleResult?.lessons.orEmpty()
                    if (lessons.isEmpty()) {
                        Text("Brak zaplanowanych zajęć w najbliższym okresie", color = TextSecondary)
                    } else {
                        LazyColumn(modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(lessons) { lesson ->
                                LessonCard(lesson = LessonEntity.fromDomain(lesson, "preview"))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closeSchedulePreview() }) {
                    Text("Zamknij", color = Color(0xFF00D2FF))
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF00D2FF),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun GroupResultItem(group: StudyGroupEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.55f))
            .border(1.dp, pl.legnica.planzajec.ui.theme.GlassTokens.BorderBrush, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = group.code, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = group.name, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun TeacherResultItem(teacher: TeacherEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.55f))
            .border(1.dp, pl.legnica.planzajec.ui.theme.GlassTokens.BorderBrush, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = teacher.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun RoomResultItem(room: RoomEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.55f))
            .border(1.dp, pl.legnica.planzajec.ui.theme.GlassTokens.BorderBrush, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Sala ${room.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(text = room.building, fontSize = 12.sp, color = TextSecondary)
    }
}
