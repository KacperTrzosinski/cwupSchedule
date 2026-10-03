package pl.legnica.planzajec.ui.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.legnica.planzajec.ui.theme.DarkSurface
import pl.legnica.planzajec.ui.theme.DarkSurfaceBorder
import pl.legnica.planzajec.ui.theme.DarkSurfaceElevated
import pl.legnica.planzajec.ui.theme.LessonGradients
import pl.legnica.planzajec.ui.theme.TextMuted
import pl.legnica.planzajec.ui.theme.TextPrimary
import pl.legnica.planzajec.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    if (state.isOnboardingComplete) {
        onFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = Color(0xFF00D2FF),
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Wybierz swój plan zajęć",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = "Collegium Witelona w Legnicy",
            fontSize = 15.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        // Dropdown 1: Wydział
        GlassDropdownField(
            label = "1. Wydział",
            selectedValue = state.selectedDepartment?.name,
            placeholder = "Wybierz wydział...",
            items = state.departments.map { it.name },
            isEnabled = state.departments.isNotEmpty(),
            onItemSelected = { name ->
                val dept = state.departments.firstOrNull { it.name == name }
                if (dept != null) viewModel.selectDepartment(dept)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown 2: Kierunek
        GlassDropdownField(
            label = "2. Kierunek",
            selectedValue = state.selectedCourse?.name,
            placeholder = if (state.selectedDepartment == null) "Najpierw wybierz wydział" else "Wybierz kierunek...",
            items = state.courses.map { it.name },
            isEnabled = state.courses.isNotEmpty(),
            onItemSelected = { name ->
                val course = state.courses.firstOrNull { it.name == name }
                if (course != null) viewModel.selectCourse(course)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown 3: Rok / Tryb
        val yearItems = state.availableYears.map { year ->
            val mode = if (state.selectedCourse?.isFullTime == false) "niestacjonarne" else "stacjonarne"
            "$year rok · $mode"
        }
        val currentYearLabel = state.selectedYear?.let { year ->
            val mode = if (state.selectedCourse?.isFullTime == false) "niestacjonarne" else "stacjonarne"
            "$year rok · $mode"
        }

        GlassDropdownField(
            label = "3. Rok i tryb studiów",
            selectedValue = currentYearLabel,
            placeholder = if (state.selectedCourse == null) "Najpierw wybierz kierunek" else "Wybierz rok...",
            items = yearItems,
            isEnabled = state.availableYears.isNotEmpty(),
            onItemSelected = { label ->
                val yearInt = label.substringBefore(" rok").trim().toIntOrNull()
                if (yearInt != null) viewModel.selectYear(yearInt)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown 4: Grupa (Specjalność)
        val filteredGroups = state.groups.filter {
            state.selectedYear == null || it.year == state.selectedYear
        }
        GlassDropdownField(
            label = "4. Grupa",
            selectedValue = state.selectedGroup?.let { "${it.code} (${it.name})" },
            placeholder = if (state.selectedYear == null) "Najpierw wybierz rok" else "Wybierz grupę...",
            items = filteredGroups.map { "${it.code} (${it.name})" },
            isEnabled = filteredGroups.isNotEmpty(),
            onItemSelected = { label ->
                val code = label.substringBefore(" (").trim()
                val group = filteredGroups.firstOrNull { it.code == code }
                if (group != null) viewModel.selectGroup(group)
            }
        )

        // Error message if any
        AnimatedVisibility(visible = state.errorMessage != null) {
            Text(
                text = state.errorMessage.orEmpty(),
                color = Color(0xFFEF4444),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Search Button with Gradient
        val canSearch = state.selectedGroup != null && !state.isLoading
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (canSearch) LessonGradients.ActionButtonBrush
                    else Brush.horizontalGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder))
                )
                .clickable(enabled = canSearch) {
                    // Trigger notification permission request on Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    viewModel.searchSchedule()
                },
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (canSearch) Color.White else TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wyszukaj plan",
                        color = if (canSearch) Color.White else TextMuted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Subgroup Selection Dialog (if group has multiple subgroups like s3PAM1(1), s3PAM2(1)u)
    if (state.showSubgroupDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.finishOnboarding(null) },
            containerColor = DarkSurfaceElevated,
            title = {
                Text(
                    text = "Wybierz swoją podgrupę",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Wykryto kilka podgrup dla tej specjalności. Możesz wybrać swoją lub wyświetlać wszystkie:",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(modifier = Modifier.height(180.dp)) {
                        item {
                            SubgroupChoiceItem(
                                label = "Pokaż wszystkie podgrupy",
                                onClick = { viewModel.finishOnboarding(null) }
                            )
                        }
                        items(state.availableSubgroups) { subgroup ->
                            SubgroupChoiceItem(
                                label = subgroup,
                                onClick = { viewModel.finishOnboarding(subgroup) }
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun GlassDropdownField(
    label: String,
    selectedValue: String?,
    placeholder: String,
    items: List<String>,
    isEnabled: Boolean,
    onItemSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf("") }

    val borderColor = if (!isEnabled) DarkSurfaceBorder
    else if (selectedValue != null) Color(0xFF06B6D4)
    else DarkSurfaceBorder

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isEnabled) TextSecondary else TextMuted,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isEnabled) DarkSurface else DarkSurface.copy(alpha = 0.5f))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .clickable(enabled = isEnabled) { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedValue ?: placeholder,
                    color = if (selectedValue != null) TextPrimary else TextMuted,
                    fontSize = 15.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "arrowRotation")
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isEnabled) TextSecondary else TextMuted,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }

        // Expanded Selection Dialog for long lists with search
        if (isExpanded && isEnabled) {
            AlertDialog(
                onDismissRequest = { isExpanded = false },
                containerColor = DarkSurfaceElevated,
                title = { Text(text = label, color = TextPrimary) },
                text = {
                    Column {
                        if (items.size > 5) {
                            OutlinedTextField(
                                value = filterText,
                                onValueChange = { filterText = it },
                                placeholder = { Text("Filtruj listę...", color = TextMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00D2FF),
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        val filtered = items.filter { it.contains(filterText, ignoreCase = true) }
                        LazyColumn(modifier = Modifier.height(240.dp)) {
                            items(filtered) { itemText ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onItemSelected(itemText)
                                            isExpanded = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = itemText,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (itemText == selectedValue) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF06B6D4),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { isExpanded = false }) {
                        Text("Anuluj", color = Color(0xFF00D2FF))
                    }
                }
            )
        }
    }
}

@Composable
private fun SubgroupChoiceItem(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(text = label, color = TextPrimary, fontSize = 14.sp)
    }
}
