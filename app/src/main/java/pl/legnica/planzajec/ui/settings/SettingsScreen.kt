package pl.legnica.planzajec.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.legnica.planzajec.data.preferences.FilterMode
import pl.legnica.planzajec.data.preferences.UserPreferencesRepository
import pl.legnica.planzajec.ui.theme.DarkBackground
import pl.legnica.planzajec.ui.theme.DarkSurface
import pl.legnica.planzajec.ui.theme.DarkSurfaceBorder
import pl.legnica.planzajec.ui.theme.DarkSurfaceElevated
import pl.legnica.planzajec.ui.theme.TextMuted
import pl.legnica.planzajec.ui.theme.TextPrimary
import pl.legnica.planzajec.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    onChangeGroupClick: () -> Unit
) {
    val prefs by preferencesRepository.userPreferencesFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var showBatteryInstructions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ustawienia",
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Section: Twoja grupa
            SettingsSectionHeader("Wybrany plan")
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onChangeGroupClick)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF00D2FF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = prefs?.selectedGroupCode ?: "Brak wybranej grupy",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = prefs?.selectedCourseName ?: "Kliknij, aby zmienić",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Text(text = "Zmień", fontSize = 13.sp, color = Color(0xFF00D2FF), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Powiadomienia
            SettingsSectionHeader("Powiadomienia i działanie")
            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = "Stałe powiadomienie",
                    subtitle = "Pokazuje najbliższe zajęcia 60 min przed i aktualizuje się na bieżąco",
                    checked = prefs?.notificationsEnabled ?: true,
                    onCheckedChange = { scope.launch { preferencesRepository.setNotificationsEnabled(it) } }
                )

                SettingsDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBatteryInstructions = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Optymalizacja baterii (OEM)", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(text = "Instrukcje dla Xiaomi, Samsung, Huawei, OnePlus", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Wygląd i filtry
            SettingsSectionHeader("Wygląd i filtry")
            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Default.Brightness4,
                    title = "Głęboka czerń (AMOLED)",
                    subtitle = "Maksymalna oszczędność baterii na ekranach OLED",
                    checked = prefs?.isAmoledTheme ?: false,
                    onCheckedChange = { scope.launch { preferencesRepository.setAmoledTheme(it) } }
                )

                SettingsDivider()

                SettingsSwitchRow(
                    icon = Icons.AutoMirrored.Filled.MergeType,
                    title = "Scalaj kolejne bloki",
                    subtitle = "Łącz wielokrotne bloki tego samego przedmiotu w jedną kartę",
                    checked = prefs?.mergeConsecutiveBlocks ?: false,
                    onCheckedChange = { scope.launch { preferencesRepository.setMergeConsecutiveBlocks(it) } }
                )

                SettingsDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Tryb zajęć", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text(
                                text = when (prefs?.filterMode) {
                                    FilterMode.CAMPUS_ONLY -> "Tylko stacjonarne"
                                    FilterMode.ONLINE_ONLY -> "Tylko online"
                                    else -> "Wszystkie zajęcia"
                                },
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row {
                        FilterButton("Wszystkie", isSelected = prefs?.filterMode == FilterMode.ALL) {
                            scope.launch { preferencesRepository.setFilterMode(FilterMode.ALL) }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterButton("Sale", isSelected = prefs?.filterMode == FilterMode.CAMPUS_ONLY) {
                            scope.launch { preferencesRepository.setFilterMode(FilterMode.CAMPUS_ONLY) }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterButton("Online", isSelected = prefs?.filterMode == FilterMode.ONLINE_ONLY) {
                            scope.launch { preferencesRepository.setFilterMode(FilterMode.ONLINE_ONLY) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: O aplikacji
            SettingsSectionHeader("O aplikacji")
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "100% Prywatności i Open Source", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text(
                        text = "Aplikacja nie gromadzi żadnych danych osobowych, nie zawiera reklam ani zewnętrznych trackerów analitycznych. Komunikacja odbywa się bezpośrednio z serwerem uczelni.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Wersja 1.0.0 · Collegium Witelona Legnica", fontSize = 11.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Battery Instructions Modal
    if (showBatteryInstructions) {
        AlertDialog(
            onDismissRequest = { showBatteryInstructions = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Text("Optymalizacja baterii", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Aby powiadomienia o najbliższych zajęciach pojawiały się niezawodnie na telefonach niektórych marek, wyłącz optymalizację baterii dla tej aplikacji:\n\n" +
                            "• Xiaomi / Redmi (HyperOS / MIUI):\nUstawienia → Aplikacje → Zarządzanie aplikacjami → Plan Zajęć → Autostart: Włączony, Oszczędzanie energii: Bez ograniczeń.\n\n" +
                            "• Samsung (One UI):\nUstawienia → Bateria → Limity użycia w tle → Nigdy nieusypiane aplikacje → Dodaj aplikację.\n\n" +
                            "• Huawei (EMUI):\nUstawienia → Bateria → Uruchamianie aplikacji → Wyłącz automatyczne, włącz działanie w tle.\n\n" +
                            "• OnePlus / Realme:\nUstawienia → Bateria → Aktywność w tle → Zezwalaj.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showBatteryInstructions = false }) {
                    Text("Rozumiem", color = Color(0xFF00D2FF))
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF00D2FF),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.58f))
            .border(1.dp, pl.legnica.planzajec.ui.theme.GlassTokens.BorderBrush, RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(pl.legnica.planzajec.ui.theme.GlassTokens.SpecularHighlight)
        )
        Column { content() }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF00D2FF), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(text = subtitle, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00D2FF)
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkSurfaceBorder))
}

@Composable
private fun FilterButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF00D2FF) else DarkSurfaceElevated)
            .border(1.dp, if (isSelected) Color(0xFF00D2FF) else DarkSurfaceBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.Black else TextSecondary
        )
    }
}
