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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import pl.legnica.planzajec.data.preferences.AppTheme

data class AuraNode(
    val xRatio: Float,
    val yRatio: Float,
    val radiusRatio: Float,
    val color: Color,
    val alpha: Float = 0.38f
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
            Color(0xFF040209),
            AuraMeshPalette(
                baseColor = Color(0xFF040209),
                nodes = listOf(
                    AuraNode(xRatio = 0.15f, yRatio = 0.20f, radiusRatio = 0.85f, color = Color(0xFF00F5D4), alpha = 0.35f),
                    AuraNode(xRatio = 0.40f, yRatio = 0.45f, radiusRatio = 0.70f, color = Color(0xFF10B981), alpha = 0.26f),
                    AuraNode(xRatio = 0.85f, yRatio = 0.75f, radiusRatio = 0.90f, color = Color(0xFFA855F7), alpha = 0.40f),
                    AuraNode(xRatio = 0.70f, yRatio = 0.95f, radiusRatio = 0.75f, color = Color(0xFFEC4899), alpha = 0.25f)
                )
            )
        )
        AppTheme.EMERALD_MATRIX -> Pair(
            Color(0xFF010A05),
            AuraMeshPalette(
                baseColor = Color(0xFF010A05),
                nodes = listOf(
                    AuraNode(xRatio = 0.85f, yRatio = 0.85f, radiusRatio = 0.90f, color = Color(0xFF34D399), alpha = 0.40f),
                    AuraNode(xRatio = 0.30f, yRatio = 0.50f, radiusRatio = 0.75f, color = Color(0xFF10B981), alpha = 0.28f),
                    AuraNode(xRatio = 0.15f, yRatio = 0.15f, radiusRatio = 0.70f, color = Color(0xFF059669), alpha = 0.22f)
                )
            )
        )
        AppTheme.DEEP_OCEAN -> Pair(
            Color(0xFF020712),
            AuraMeshPalette(
                baseColor = Color(0xFF020712),
                nodes = listOf(
                    AuraNode(xRatio = 0.50f, yRatio = 0.95f, radiusRatio = 0.90f, color = Color(0xFF00E5FF), alpha = 0.38f),
                    AuraNode(xRatio = 0.25f, yRatio = 0.35f, radiusRatio = 0.85f, color = Color(0xFF1D4ED8), alpha = 0.34f),
                    AuraNode(xRatio = 0.80f, yRatio = 0.60f, radiusRatio = 0.75f, color = Color(0xFF0284C7), alpha = 0.28f)
                )
            )
        )
        AppTheme.LIQUID_OBSIDIAN -> Pair(
            Color(0xFF030305),
            AuraMeshPalette(
                baseColor = Color(0xFF030305),
                nodes = listOf(
                    AuraNode(xRatio = 0.20f, yRatio = 0.25f, radiusRatio = 0.80f, color = Color(0xFF475569), alpha = 0.28f),
                    AuraNode(xRatio = 0.80f, yRatio = 0.75f, radiusRatio = 0.85f, color = Color(0xFF334155), alpha = 0.28f),
                    AuraNode(xRatio = 0.50f, yRatio = 0.50f, radiusRatio = 0.90f, color = Color(0xFF1E293B), alpha = 0.22f)
                )
            )
        )
        AppTheme.CYBERPUNK_NEON -> Pair(
            Color(0xFF040209),
            AuraMeshPalette(
                baseColor = Color(0xFF040209),
                nodes = listOf(
                    AuraNode(xRatio = 0.15f, yRatio = 0.20f, radiusRatio = 0.85f, color = Color(0xFFFF007F), alpha = 0.38f),
                    AuraNode(xRatio = 0.85f, yRatio = 0.80f, radiusRatio = 0.85f, color = Color(0xFF00F0FF), alpha = 0.36f),
                    AuraNode(xRatio = 0.55f, yRatio = 0.50f, radiusRatio = 0.70f, color = Color(0xFF8B5CF6), alpha = 0.22f)
                )
            )
        )
        AppTheme.CRIMSON_NIGHT -> Pair(
            Color(0xFF060103),
            AuraMeshPalette(
                baseColor = Color(0xFF060103),
                nodes = listOf(
                    AuraNode(xRatio = 0.50f, yRatio = 0.85f, radiusRatio = 0.90f, color = Color(0xFFE11D48), alpha = 0.40f),
                    AuraNode(xRatio = 0.80f, yRatio = 0.40f, radiusRatio = 0.75f, color = Color(0xFFFB7185), alpha = 0.30f),
                    AuraNode(xRatio = 0.20f, yRatio = 0.20f, radiusRatio = 0.75f, color = Color(0xFF881337), alpha = 0.25f)
                )
            )
        )
        AppTheme.MIDNIGHT_AMBER -> Pair(
            Color(0xFF070502),
            AuraMeshPalette(
                baseColor = Color(0xFF070502),
                nodes = listOf(
                    AuraNode(xRatio = 0.60f, yRatio = 0.28f, radiusRatio = 0.85f, color = Color(0xFFF59E0B), alpha = 0.40f),
                    AuraNode(xRatio = 0.35f, yRatio = 0.60f, radiusRatio = 0.80f, color = Color(0xFFEA580C), alpha = 0.35f),
                    AuraNode(xRatio = 0.75f, yRatio = 0.90f, radiusRatio = 0.75f, color = Color(0xFFD97706), alpha = 0.30f)
                )
            )
        )
        AppTheme.SYNTHWAVE_SUNSET -> Pair(
            Color(0xFF05020D),
            AuraMeshPalette(
                baseColor = Color(0xFF05020D),
                nodes = listOf(
                    AuraNode(xRatio = 0.20f, yRatio = 0.25f, radiusRatio = 0.85f, color = Color(0xFFF97316), alpha = 0.38f),
                    AuraNode(xRatio = 0.85f, yRatio = 0.70f, radiusRatio = 0.85f, color = Color(0xFFEC4899), alpha = 0.38f),
                    AuraNode(xRatio = 0.45f, yRatio = 0.95f, radiusRatio = 0.80f, color = Color(0xFF8B5CF6), alpha = 0.30f)
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

                // 1. Solid deep base color
                drawRect(palette.baseColor)

                // 2. Diffused Aura / Mesh Nodes with multi-stop radial gradient
                val maxDim = maxOf(width, height)
                palette.nodes.forEach { node ->
                    val center = Offset(width * node.xRatio, height * node.yRatio)
                    val radius = maxDim * node.radiusRatio
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to node.color.copy(alpha = node.alpha),
                                0.35f to node.color.copy(alpha = node.alpha * 0.65f),
                                0.65f to node.color.copy(alpha = node.alpha * 0.20f),
                                1.0f to Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        center = center,
                        radius = radius
                    )
                }

                // 3. Tactile film grain / micro-dither texture overlay
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
