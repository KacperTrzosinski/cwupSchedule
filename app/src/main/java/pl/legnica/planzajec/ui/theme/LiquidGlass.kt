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

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    isAmoled: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isAmoled) AmoledBackground else ObsidianBlack)
    ) {
        // Multi-point glowing mesh gradient background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val alphaMultiplier = if (isAmoled) 0.5f else 1.0f

            // Top-left: vibrant cyan orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LiquidGlowCyan.copy(alpha = 0.28f * alphaMultiplier),
                        LiquidGlowCyan.copy(alpha = 0.10f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.12f),
                    radius = width * 0.75f
                )
            )

            // Top-right: electric purple/violet orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LiquidGlowPurple.copy(alpha = 0.24f * alphaMultiplier),
                        LiquidGlowPurple.copy(alpha = 0.08f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.22f),
                    radius = width * 0.80f
                )
            )

            // Center-left: magenta / pink liquid glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LiquidGlowPink.copy(alpha = 0.18f * alphaMultiplier),
                        LiquidGlowPink.copy(alpha = 0.05f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.10f, height * 0.55f),
                    radius = width * 0.70f
                )
            )

            // Bottom-right: deep indigo orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LiquidGlowIndigo.copy(alpha = 0.22f * alphaMultiplier),
                        LiquidGlowIndigo.copy(alpha = 0.07f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.80f, height * 0.78f),
                    radius = width * 0.85f
                )
            )

            // Bottom-center: emerald glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LiquidGlowEmerald.copy(alpha = 0.12f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.40f, height * 0.95f),
                    radius = width * 0.60f
                )
            )
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
