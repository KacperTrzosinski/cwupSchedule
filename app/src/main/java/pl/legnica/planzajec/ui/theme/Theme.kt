package pl.legnica.planzajec.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    surface = Color(0xFF0F1217),
    surfaceVariant = Color(0xFF171B22)
)

@Composable
fun CwupScheduleTheme(
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isAmoled) AmoledColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
