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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import pl.legnica.planzajec.data.preferences.AppTheme

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    appTheme: AppTheme = AppTheme.LIQUID_OBSIDIAN,
    isAmoled: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val effectiveTheme = if (isAmoled) AppTheme.PURE_AMOLED else appTheme
    val bgColor = when (effectiveTheme) {
        AppTheme.PURE_AMOLED -> AmoledBackground
        AppTheme.AURORA_PURPLE -> Color(0xFF070414)
        AppTheme.EMERALD_MATRIX -> Color(0xFF020E08)
        AppTheme.DEEP_OCEAN -> Color(0xFF030B17)
        AppTheme.LIQUID_OBSIDIAN -> ObsidianBlack
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        if (effectiveTheme != AppTheme.PURE_AMOLED) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                when (effectiveTheme) {
                    AppTheme.AURORA_PURPLE -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFA855F7).copy(alpha = 0.32f), Color(0xFFA855F7).copy(alpha = 0.08f), Color.Transparent),
                                center = Offset(width * 0.15f, height * 0.12f),
                                radius = width * 0.75f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFEC4899).copy(alpha = 0.25f), Color(0xFFEC4899).copy(alpha = 0.06f), Color.Transparent),
                                center = Offset(width * 0.85f, height * 0.25f),
                                radius = width * 0.80f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF6366F1).copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(width * 0.10f, height * 0.58f),
                                radius = width * 0.70f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFD946EF).copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(width * 0.80f, height * 0.80f),
                                radius = width * 0.85f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.14f), Color.Transparent),
                                center = Offset(width * 0.40f, height * 0.95f),
                                radius = width * 0.60f
                            )
                        )
                    }
                    AppTheme.EMERALD_MATRIX -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF10B981).copy(alpha = 0.30f), Color(0xFF10B981).copy(alpha = 0.08f), Color.Transparent),
                                center = Offset(width * 0.15f, height * 0.12f),
                                radius = width * 0.75f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(width * 0.85f, height * 0.25f),
                                radius = width * 0.80f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF34D399).copy(alpha = 0.20f), Color.Transparent),
                                center = Offset(width * 0.10f, height * 0.58f),
                                radius = width * 0.70f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF059669).copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(width * 0.80f, height * 0.80f),
                                radius = width * 0.85f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF84CC16).copy(alpha = 0.14f), Color.Transparent),
                                center = Offset(width * 0.40f, height * 0.95f),
                                radius = width * 0.60f
                            )
                        )
                    }
                    AppTheme.DEEP_OCEAN -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0284C7).copy(alpha = 0.30f), Color(0xFF0284C7).copy(alpha = 0.08f), Color.Transparent),
                                center = Offset(width * 0.15f, height * 0.12f),
                                radius = width * 0.75f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF2563EB).copy(alpha = 0.25f), Color.Transparent),
                                center = Offset(width * 0.85f, height * 0.25f),
                                radius = width * 0.80f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.20f), Color.Transparent),
                                center = Offset(width * 0.10f, height * 0.58f),
                                radius = width * 0.70f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF4338CA).copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(width * 0.80f, height * 0.80f),
                                radius = width * 0.85f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0EA5E9).copy(alpha = 0.14f), Color.Transparent),
                                center = Offset(width * 0.40f, height * 0.95f),
                                radius = width * 0.60f
                            )
                        )
                    }
                    else -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    LiquidGlowCyan.copy(alpha = 0.28f),
                                    LiquidGlowCyan.copy(alpha = 0.10f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.15f, height * 0.12f),
                                radius = width * 0.75f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    LiquidGlowPurple.copy(alpha = 0.24f),
                                    LiquidGlowPurple.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.85f, height * 0.22f),
                                radius = width * 0.80f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    LiquidGlowPink.copy(alpha = 0.18f),
                                    LiquidGlowPink.copy(alpha = 0.05f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.10f, height * 0.55f),
                                radius = width * 0.70f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    LiquidGlowIndigo.copy(alpha = 0.22f),
                                    LiquidGlowIndigo.copy(alpha = 0.07f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.80f, height * 0.78f),
                                radius = width * 0.85f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    LiquidGlowEmerald.copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.40f, height * 0.95f),
                                radius = width * 0.60f
                            )
                        )
                    }
                }
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
