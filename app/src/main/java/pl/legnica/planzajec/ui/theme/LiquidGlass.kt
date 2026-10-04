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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import pl.legnica.planzajec.data.preferences.AppTheme
import pl.legnica.planzajec.data.preferences.UiStyle

val LocalUiStyle = compositionLocalOf { UiStyle.GLASSMORPHISM }

object GlassmorphismTokens {
    val SurfaceColor = Color(0xFF0B0F19).copy(alpha = 0.72f)
    val SurfaceElevatedColor = Color(0xFF131A29).copy(alpha = 0.80f)
    val BorderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.14f),
            Color.White.copy(alpha = 0.04f)
        )
    )
}

data class AuraNode(
    val xRatio: Float,
    val yRatio: Float,
    val radiusRatio: Float,
    val color: Color,
    val alpha: Float = 0.35f
)

private data class AuraMeshPalette(
    val baseColor: Color,
    val nodes: List<AuraNode>
)

private object NoiseTextureHolder {
    private var noiseShader: android.graphics.BitmapShader? = null

    fun getShader(): android.graphics.BitmapShader {
        return noiseShader ?: synchronized(this) {
            noiseShader ?: run {
                val size = 128
                val pixels = IntArray(size * size)
                val random = java.util.Random(1337)
                for (i in pixels.indices) {
                    val alpha = random.nextInt(15) // Subtle 0 to 14 alpha (~0.055 max)
                    pixels[i] = android.graphics.Color.argb(alpha, 255, 255, 255)
                }
                val bitmap = android.graphics.Bitmap.createBitmap(pixels, size, size, android.graphics.Bitmap.Config.ARGB_8888)
                val shader = android.graphics.BitmapShader(
                    bitmap,
                    android.graphics.Shader.TileMode.REPEAT,
                    android.graphics.Shader.TileMode.REPEAT
                )
                noiseShader = shader
                shader
            }
        }
    }
}

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
            Color(0xFF020106),
            AuraMeshPalette(
                baseColor = Color(0xFF020106),
                nodes = listOf(
                    AuraNode(xRatio = 0.10f, yRatio = 0.15f, radiusRatio = 0.55f, color = Color(0xFF00F5D4), alpha = 0.34f),
                    AuraNode(xRatio = 0.90f, yRatio = 0.85f, radiusRatio = 0.60f, color = Color(0xFFA855F7), alpha = 0.38f),
                    AuraNode(xRatio = 0.30f, yRatio = 0.50f, radiusRatio = 0.45f, color = Color(0xFF10B981), alpha = 0.20f)
                )
            )
        )
        AppTheme.EMERALD_MATRIX -> Pair(
            Color(0xFF010502),
            AuraMeshPalette(
                baseColor = Color(0xFF010502),
                nodes = listOf(
                    AuraNode(xRatio = 0.88f, yRatio = 0.90f, radiusRatio = 0.60f, color = Color(0xFF34D399), alpha = 0.38f),
                    AuraNode(xRatio = 0.12f, yRatio = 0.12f, radiusRatio = 0.45f, color = Color(0xFF059669), alpha = 0.22f)
                )
            )
        )
        AppTheme.DEEP_OCEAN -> Pair(
            Color(0xFF01040A),
            AuraMeshPalette(
                baseColor = Color(0xFF01040A),
                nodes = listOf(
                    AuraNode(xRatio = 0.50f, yRatio = 1.0f, radiusRatio = 0.62f, color = Color(0xFF00E5FF), alpha = 0.38f),
                    AuraNode(xRatio = 0.15f, yRatio = 0.40f, radiusRatio = 0.50f, color = Color(0xFF1D4ED8), alpha = 0.30f),
                    AuraNode(xRatio = 0.85f, yRatio = 0.65f, radiusRatio = 0.45f, color = Color(0xFF0284C7), alpha = 0.24f)
                )
            )
        )
        AppTheme.LIQUID_OBSIDIAN -> Pair(
            Color(0xFF020204),
            AuraMeshPalette(
                baseColor = Color(0xFF020204),
                nodes = listOf(
                    AuraNode(xRatio = 0.82f, yRatio = 0.80f, radiusRatio = 0.55f, color = Color(0xFF475569), alpha = 0.30f),
                    AuraNode(xRatio = 0.18f, yRatio = 0.20f, radiusRatio = 0.50f, color = Color(0xFF334155), alpha = 0.25f)
                )
            )
        )
        AppTheme.CYBERPUNK_NEON -> Pair(
            Color(0xFF020105),
            AuraMeshPalette(
                baseColor = Color(0xFF020105),
                nodes = listOf(
                    AuraNode(xRatio = 0.12f, yRatio = 0.18f, radiusRatio = 0.55f, color = Color(0xFFFF007F), alpha = 0.36f),
                    AuraNode(xRatio = 0.88f, yRatio = 0.82f, radiusRatio = 0.55f, color = Color(0xFF00F0FF), alpha = 0.34f)
                )
            )
        )
        AppTheme.CRIMSON_NIGHT -> Pair(
            Color(0xFF030001),
            AuraMeshPalette(
                baseColor = Color(0xFF030001),
                nodes = listOf(
                    AuraNode(xRatio = 0.50f, yRatio = 0.95f, radiusRatio = 0.60f, color = Color(0xFFE11D48), alpha = 0.38f),
                    AuraNode(xRatio = 0.15f, yRatio = 0.18f, radiusRatio = 0.45f, color = Color(0xFF881337), alpha = 0.24f)
                )
            )
        )
        AppTheme.MIDNIGHT_AMBER -> Pair(
            Color(0xFF030201),
            AuraMeshPalette(
                baseColor = Color(0xFF030201),
                nodes = listOf(
                    AuraNode(xRatio = 0.65f, yRatio = 0.35f, radiusRatio = 0.55f, color = Color(0xFFF59E0B), alpha = 0.38f),
                    AuraNode(xRatio = 0.30f, yRatio = 0.85f, radiusRatio = 0.50f, color = Color(0xFFEA580C), alpha = 0.30f)
                )
            )
        )
        AppTheme.SYNTHWAVE_SUNSET -> Pair(
            Color(0xFF020108),
            AuraMeshPalette(
                baseColor = Color(0xFF020108),
                nodes = listOf(
                    AuraNode(xRatio = 0.15f, yRatio = 0.30f, radiusRatio = 0.55f, color = Color(0xFFF97316), alpha = 0.36f),
                    AuraNode(xRatio = 0.85f, yRatio = 0.75f, radiusRatio = 0.55f, color = Color(0xFFEC4899), alpha = 0.36f)
                )
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

                // 1. Deep carbon black void base
                drawRect(palette.baseColor)

                // 2. Focused point lights emerging from behind dark glass
                val maxDim = maxOf(width, height)
                palette.nodes.forEach { node ->
                    val center = Offset(width * node.xRatio, height * node.yRatio)
                    val radius = maxDim * node.radiusRatio
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to node.color.copy(alpha = node.alpha),
                                0.22f to node.color.copy(alpha = node.alpha * 0.65f),
                                0.45f to node.color.copy(alpha = node.alpha * 0.20f),
                                0.70f to node.color.copy(alpha = node.alpha * 0.04f),
                                1.0f to Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        center = center,
                        radius = radius
                    )
                }

                // 3. Dark smoked glass vignette (absorbs stray light into deep black)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0xFF010103).copy(alpha = 0.40f)),
                        radius = maxDim * 0.85f,
                        center = Offset(width * 0.5f, height * 0.5f)
                    )
                )

                // 4. Tactile film grain / micro-dither texture overlay
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        shader = NoiseTextureHolder.getShader()
                    }
                    canvas.nativeCanvas.drawRect(0f, 0f, width, height, paint)
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
    borderBrush: Brush? = null,
    backgroundColor: Color? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val uiStyle = LocalUiStyle.current
    val effectiveBg = backgroundColor ?: if (uiStyle == UiStyle.GLASSMORPHISM) {
        GlassmorphismTokens.SurfaceColor
    } else {
        GlassSurfaceColor
    }
    val effectiveBorder = borderBrush ?: if (uiStyle == UiStyle.GLASSMORPHISM) {
        GlassmorphismTokens.BorderBrush
    } else {
        GlassTokens.BorderBrush
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, effectiveBorder, shape)
            .background(effectiveBg)
            .then(clickableModifier)
    ) {
        if (uiStyle == UiStyle.LIQUID_GLASS) {
            // Specular top highlight simulating liquid glass reflection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(GlassTokens.SpecularHighlight)
            )
        }

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

