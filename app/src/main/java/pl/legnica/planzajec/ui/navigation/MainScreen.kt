package pl.legnica.planzajec.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.legnica.planzajec.data.preferences.UserPreferencesRepository
import pl.legnica.planzajec.ui.calendar.CalendarScreen
import pl.legnica.planzajec.ui.plan.PlanScreen
import pl.legnica.planzajec.ui.plan.PlanViewModel
import pl.legnica.planzajec.ui.search.SearchScreen
import pl.legnica.planzajec.ui.search.SearchTab
import pl.legnica.planzajec.ui.search.SearchViewModel
import pl.legnica.planzajec.ui.settings.SettingsScreen
import pl.legnica.planzajec.ui.theme.DarkBackground
import pl.legnica.planzajec.ui.theme.DarkSurface
import pl.legnica.planzajec.ui.theme.DarkSurfaceBorder
import pl.legnica.planzajec.ui.theme.TextMuted
import pl.legnica.planzajec.ui.theme.TextPrimary

enum class NavigationDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    PLAN("Plan", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    CALENDAR("Kalendarz", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    SEARCH("Szukaj", Icons.Filled.Search, Icons.Outlined.Search),
    SETTINGS("Ustawienia", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    preferencesRepository: UserPreferencesRepository,
    onChangeGroupClick: () -> Unit,
    modifier: Modifier = Modifier,
    planViewModel: PlanViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    var currentDestination by rememberSaveable { mutableStateOf(NavigationDestination.PLAN) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.32f),
                                Color(0xFF00D2FF).copy(alpha = 0.28f),
                                Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .background(Color(0xFF090D16).copy(alpha = 0.78f))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(68.dp)
                ) {
                    NavigationDestination.entries.forEach { destination ->
                        val isSelected = currentDestination == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00D2FF),
                                selectedTextColor = Color(0xFF00D2FF),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Color(0xFF00D2FF).copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentDestination) {
                NavigationDestination.PLAN -> {
                    PlanScreen(
                        viewModel = planViewModel,
                        onTeacherClick = { teacherName ->
                            searchViewModel.setQuery(teacherName)
                            searchViewModel.setTab(SearchTab.TEACHERS)
                            currentDestination = NavigationDestination.SEARCH
                        },
                        onRoomClick = { roomName ->
                            searchViewModel.setQuery(roomName)
                            searchViewModel.setTab(SearchTab.ROOMS)
                            currentDestination = NavigationDestination.SEARCH
                        }
                    )
                }
                NavigationDestination.CALENDAR -> {
                    CalendarScreen(
                        viewModel = planViewModel,
                        onTeacherClick = { teacherName ->
                            searchViewModel.setQuery(teacherName)
                            searchViewModel.setTab(SearchTab.TEACHERS)
                            currentDestination = NavigationDestination.SEARCH
                        },
                        onRoomClick = { roomName ->
                            searchViewModel.setQuery(roomName)
                            searchViewModel.setTab(SearchTab.ROOMS)
                            currentDestination = NavigationDestination.SEARCH
                        }
                    )
                }
                NavigationDestination.SEARCH -> {
                    SearchScreen(
                        viewModel = searchViewModel,
                        onGroupSelected = {
                            // Optionally switch back to Plan or show preview
                            currentDestination = NavigationDestination.PLAN
                        }
                    )
                }
                NavigationDestination.SETTINGS -> {
                    SettingsScreen(
                        preferencesRepository = preferencesRepository,
                        onChangeGroupClick = onChangeGroupClick
                    )
                }
            }
        }
    }
}
