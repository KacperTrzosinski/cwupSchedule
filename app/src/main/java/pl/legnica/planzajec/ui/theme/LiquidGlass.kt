package pl.legnica.planzajec.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import pl.legnica.planzajec.data.preferences.AppTheme

private data class AuroraPalette(
    val bgStart: Color,
    val bgEnd: Color,
    val ribbon1: List<Color>,
    val ribbon2: List<Color>,
    val ribbon3: List<Color>,
    val rays: List<Color>
)

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    appTheme: AppTheme = AppTheme.LIQUID_OBSIDIAN,
    isAmoled: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val effectiveTheme = if (isAmoled) AppTheme.PURE_AMOLED else appTheme
    val (bgColor, palette) = when (effectiveTheme) {
        AppTheme.PURE_AMOLED -> Pair(AmoledBackground, null)
        AppTheme.AURORA_PURPLE -> Pair(
            Color(0xFF070314),
            AuroraPalette(
                bgStart = Color(0xFF09041A),
                bgEnd = Color(0xFF03010A),
                ribbon1 = listOf(Color(0xFF00F0FF).copy(alpha = 0.40f), Color(0xFFA855F7).copy(alpha = 0.50f), Color(0xFFEC4899).copy(alpha = 0.45f), Color.Transparent),
                ribbon2 = listOf(Color(0xFF818CF8).copy(alpha = 0.35f), Color(0xFFC084FC).copy(alpha = 0.45f), Color(0xFF2DD4BF).copy(alpha = 0.35f), Color.Transparent),
                ribbon3 = listOf(Color(0xFFD946EF).copy(alpha = 0.30f), Color(0xFF38BDF8).copy(alpha = 0.35f), Color(0xFF6366F1).copy(alpha = 0.25f), Color.Transparent),
                rays = listOf(Color(0xFFA855F7).copy(alpha = 0.22f), Color(0xFF38BDF8).copy(alpha = 0.18f), Color(0xFFEC4899).copy(alpha = 0.12f), Color.Transparent)
            )
        )
        AppTheme.EMERALD_MATRIX -> Pair(
            Color(0xFF010E07),
            AuroraPalette(
                bgStart = Color(0xFF011409),
                bgEnd = Color(0xFF010604),
                ribbon1 = listOf(Color(0xFF00FF87).copy(alpha = 0.40f), Color(0xFF10B981).copy(alpha = 0.50f), Color(0xFF06B6D4).copy(alpha = 0.40f), Color.Transparent),
                ribbon2 = listOf(Color(0xFF34D399).copy(alpha = 0.35f), Color(0xFF84CC16).copy(alpha = 0.40f), Color(0xFF059669).copy(alpha = 0.45f), Color.Transparent),
                ribbon3 = listOf(Color(0xFF14B8A6).copy(alpha = 0.30f), Color(0xFFA3E635).copy(alpha = 0.30f), Color(0xFF047857).copy(alpha = 0.35f), Color.Transparent),
                rays = listOf(Color(0xFF10B981).copy(alpha = 0.22f), Color(0xFF34D399).copy(alpha = 0.18f), Color(0xFF06B6D4).copy(alpha = 0.12f), Color.Transparent)
            )
        )
        AppTheme.DEEP_OCEAN -> Pair(
            Color(0xFF020B18),
            AuroraPalette(
                bgStart = Color(0xFF021024),
                bgEnd = Color(0xFF01060F),
                ribbon1 = listOf(Color(0xFF00E5FF).copy(alpha = 0.42f), Color(0xFF0284C7).copy(alpha = 0.50f), Color(0xFF2563EB).copy(alpha = 0.40f), Color.Transparent),
                ribbon2 = listOf(Color(0xFF38BDF8).copy(alpha = 0.35f), Color(0xFF4F46E5).copy(alpha = 0.42f), Color(0xFF06B6D4).copy(alpha = 0.35f), Color.Transparent),
                ribbon3 = listOf(Color(0xFF1D4ED8).copy(alpha = 0.30f), Color(0xFF00F5D4).copy(alpha = 0.30f), Color(0xFF3B82F6).copy(alpha = 0.35f), Color.Transparent),
                rays = listOf(Color(0xFF0284C7).copy(alpha = 0.22f), Color(0xFF00E5FF).copy(alpha = 0.18f), Color(0xFF2563EB).copy(alpha = 0.12f), Color.Transparent)
            )
        )
        AppTheme.LIQUID_OBSIDIAN -> Pair(
            ObsidianBlack,
            AuroraPalette(
                bgStart = Color(0xFF07040E),
                bgEnd = Color(0xFF020204),
                ribbon1 = listOf(Color(0xFF00D2FF).copy(alpha = 0.42f), Color(0xFF8B5CF6).copy(alpha = 0.50f), Color(0xFFF43F5E).copy(alpha = 0.38f), Color.Transparent),
                ribbon2 = listOf(Color(0xFFA855F7).copy(alpha = 0.35f), Color(0xFFEC4899).copy(alpha = 0.38f), Color(0xFF00F0FF).copy(alpha = 0.35f), Color.Transparent),
                ribbon3 = listOf(Color(0xFF4F46E5).copy(alpha = 0.30f), Color(0xFFE11D48).copy(alpha = 0.28f), Color(0xFF06B6D4).copy(alpha = 0.32f), Color.Transparent),
                rays = listOf(Color(0xFF8B5CF6).copy(alpha = 0.22f), Color(0xFF00D2FF).copy(alpha = 0.18f), Color(0xFFF43F5E).copy(alpha = 0.12f), Color.Transparent)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        if (palette != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // 1. Diagonal atmospheric background gradient
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(palette.bgStart, palette.bgEnd),
                        start = Offset(0f, 0f),
                        end = Offset(width, height)
                    )
                )

                // 2. Diffuse curtain light rays (northern lights shimmering columns)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = palette.rays,
                        startY = 0f,
                        endY = height * 0.85f
                    )
                )

                // 3. Ribbon 1: Sweeping Upper Aurora Wave
                val path1 = Path().apply {
                    moveTo(-width * 0.15f, height * 0.04f)
                    cubicTo(
                        width * 0.35f, height * 0.20f,
                        width * 0.65f, height * 0.02f,
                        width * 1.15f, height * 0.16f
                    )
                    lineTo(width * 1.15f, height * 0.40f)
                    cubicTo(
                        width * 0.70f, height * 0.26f,
                        width * 0.30f, height * 0.44f,
                        -width * 0.15f, height * 0.24f
                    )
                    close()
                }
                drawPath(
                    path = path1,
                    brush = Brush.linearGradient(
                        colors = palette.ribbon1,
                        start = Offset(0f, 0f),
                        end = Offset(width, height * 0.4f)
                    )
                )

                // 4. Ribbon 2: Flowing Mid-screen Undulating Wave
                val path2 = Path().apply {
                    moveTo(-width * 0.20f, height * 0.38f)
                    cubicTo(
                        width * 0.25f, height * 0.28f,
                        width * 0.70f, height * 0.55f,
                        width * 1.20f, height * 0.44f
                    )
                    lineTo(width * 1.20f, height * 0.68f)
                    cubicTo(
                        width * 0.65f, height * 0.76f,
                        width * 0.20f, height * 0.52f,
                        -width * 0.20f, height * 0.60f
                    )
                    close()
                }
                drawPath(
                    path = path2,
                    brush = Brush.linearGradient(
                        colors = palette.ribbon2,
                        start = Offset(0f, height * 0.3f),
                        end = Offset(width, height * 0.7f)
                    )
                )

                // 5. Ribbon 3: Luminous Lower Horizon Wave
                val path3 = Path().apply {
                    moveTo(-width * 0.15f, height * 0.68f)
                    cubicTo(
                        width * 0.35f, height * 0.58f,
                        width * 0.70f, height * 0.88f,
                        width * 1.15f, height * 0.78f
                    )
                    lineTo(width * 1.15f, height * 1.05f)
                    lineTo(-width * 0.15f, height * 1.05f)
                    close()
                }
                drawPath(
                    path = path3,
                    brush = Brush.linearGradient(
                        colors = palette.ribbon3,
                        start = Offset(0f, height * 0.6f),
                        end = Offset(width, height)
                    )
                )
            }
        }

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderBrush: Brush = GlassTokens.BorderBrush,
    backgroundColor: Color = GlassSurfaceColor,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, borderBrush, shape)
            .background(backgroundColor)
            .then(clickableModifier)
    ) {
        // Specular top highlight simulating liquid glass reflection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(GlassTokens.SpecularHighlight)
        )

        Column {
            content()
        }
    }
}

fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    borderBrush: Brush = GlassTokens.BorderBrush,
    backgroundColor: Color = GlassSurfaceColor,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .border(borderWidth, borderBrush, shape)
    .background(backgroundColor)
