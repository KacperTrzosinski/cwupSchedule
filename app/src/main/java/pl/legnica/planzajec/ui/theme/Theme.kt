package pl.legnica.planzajec.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import pl.legnica.planzajec.data.preferences.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color.Black,
    primaryContainer = AccentIndigo,
    onPrimaryContainer = Color.White,
    secondary = AccentPurple,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder
)

private val AmoledColorScheme = DarkColorScheme.copy(
    background = AmoledBackground,
    surface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFF141414)
)

@Composable
fun CwupScheduleTheme(
    appTheme: AppTheme = AppTheme.LIQUID_OBSIDIAN,
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val actualAmoled = isAmoled || appTheme == AppTheme.PURE_AMOLED
    val baseScheme = if (actualAmoled) AmoledColorScheme else DarkColorScheme
    val colorScheme = when (appTheme) {
        AppTheme.AURORA_PURPLE -> baseScheme.copy(
            primary = Color(0xFFA855F7),
            primaryContainer = Color(0xFF6B21A8),
            secondary = Color(0xFFEC4899)
        )
        AppTheme.EMERALD_MATRIX -> baseScheme.copy(
            primary = Color(0xFF10B981),
            primaryContainer = Color(0xFF065F46),
            secondary = Color(0xFF34D399)
        )
        AppTheme.DEEP_OCEAN -> baseScheme.copy(
            primary = Color(0xFF38BDF8),
            primaryContainer = Color(0xFF1E40AF),
            secondary = Color(0xFF60A5FA)
        )
        AppTheme.CYBERPUNK_NEON -> baseScheme.copy(
            primary = Color(0xFFFF007F),
            primaryContainer = Color(0xFF830051),
            secondary = Color(0xFF00F0FF)
        )
        AppTheme.CRIMSON_NIGHT -> baseScheme.copy(
            primary = Color(0xFFE11D48),
            primaryContainer = Color(0xFF881337),
            secondary = Color(0xFFFB7185)
        )
        AppTheme.MIDNIGHT_AMBER -> baseScheme.copy(
            primary = Color(0xFFF59E0B),
            primaryContainer = Color(0xFF78350F),
            secondary = Color(0xFFFBBF24)
        )
        AppTheme.SYNTHWAVE_SUNSET -> baseScheme.copy(
            primary = Color(0xFFF97316),
            primaryContainer = Color(0xFF7C3AED),
            secondary = Color(0xFFEC4899)
        )
        AppTheme.PURE_AMOLED -> AmoledColorScheme
        AppTheme.LIQUID_OBSIDIAN -> baseScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
