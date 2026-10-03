package pl.legnica.planzajec.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import pl.legnica.planzajec.parser.model.LessonType

// Base Dark Colors
val DarkBackground = Color(0xFF030712)
val AmoledBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF111827).copy(alpha = 0.65f)
val DarkSurfaceElevated = Color(0xFF1F2937).copy(alpha = 0.75f)
val DarkSurfaceBorder = Color(0xFF38BDF8).copy(alpha = 0.20f)

// Liquid Glass Design Tokens
val ObsidianBlack = Color(0xFF030712)
val GlassSurfaceColor = Color(0xFF0F172A).copy(alpha = 0.58f)
val GlassSurfaceElevatedColor = Color(0xFF1E293B).copy(alpha = 0.68f)

val LiquidGlowCyan = Color(0xFF00D2FF)
val LiquidGlowPurple = Color(0xFF8B5CF6)
val LiquidGlowPink = Color(0xFFEC4899)
val LiquidGlowIndigo = Color(0xFF4F46E5)
val LiquidGlowEmerald = Color(0xFF10B981)

object GlassTokens {
    val BorderBrush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.35f),
            Color(0xFF00D2FF).copy(alpha = 0.30f),
            Color(0xFF8B5CF6).copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.08f)
        )
    )

    val SpecularHighlight = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.14f),
            Color.Transparent
        )
    )
}

// Text Colors (High Contrast >= 4.5:1)
val TextPrimary = Color(0xFFF0F6FC)
val TextSecondary = Color(0xFF8B949E)
val TextMuted = Color(0xFF6E7681)

// Gradient Accent Colors
val AccentIndigo = Color(0xFF4F46E5)
val AccentCyan = Color(0xFF06B6D4)
val AccentEmerald = Color(0xFF10B981)
val AccentLime = Color(0xFF84CC16)
val AccentOrange = Color(0xFFF97316)
val AccentPink = Color(0xFFEC4899)
val AccentPurple = Color(0xFF8B5CF6)
val AccentMagenta = Color(0xFFD946EF)
val AccentTeal = Color(0xFF14B8A6)
val AccentBlue = Color(0xFF3B82F6)
val AccentGrayStart = Color(0xFF4B5563)
val AccentGrayEnd = Color(0xFF9CA3AF)

// Online Gradient
val OnlineGradientStart = Color(0xFF00D2FF)
val OnlineGradientEnd = Color(0xFF7928CA)

// Change indicator colors
val ChangeColorNew = Color(0xFF10B981)
val ChangeColorModified = Color(0xFFF59E0B)
val ChangeColorCancelled = Color(0xFFEF4444)

object LessonGradients {
    fun getBrushForType(type: LessonType): Brush {
        return when (type) {
            LessonType.LECTURE -> Brush.horizontalGradient(listOf(AccentIndigo, AccentCyan))
            LessonType.EXERCISE -> Brush.horizontalGradient(listOf(AccentEmerald, AccentLime))
            LessonType.LABORATORY -> Brush.horizontalGradient(listOf(AccentOrange, AccentPink))
            LessonType.SEMINAR -> Brush.horizontalGradient(listOf(AccentPurple, AccentMagenta))
            LessonType.PROJECT -> Brush.horizontalGradient(listOf(AccentTeal, AccentBlue))
            LessonType.WORKSHOP -> Brush.horizontalGradient(listOf(AccentCyan, AccentEmerald))
            LessonType.FOREIGN_LANGUAGE -> Brush.horizontalGradient(listOf(AccentPurple, AccentBlue))
            LessonType.OTHER -> Brush.horizontalGradient(listOf(AccentGrayStart, AccentGrayEnd))
        }
    }

    val OnlineBrush: Brush = Brush.horizontalGradient(listOf(OnlineGradientStart, OnlineGradientEnd))
    val ActionButtonBrush: Brush = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED)))
}
