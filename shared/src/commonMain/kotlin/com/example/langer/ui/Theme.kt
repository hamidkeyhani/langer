package com.example.langer.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Premium Dark Theme Palette
val DarkBackground = Color(0xFF0F1115) // Deep luxury charcoal
val DarkSurface = Color(0xFF181B22)    // Slightly lighter card slate
val DarkSurfaceVariant = Color(0xFF222631)
val DarkPrimary = Color(0xFFEC4899)    // Modern vibrant rose-pink
val DarkSecondary = Color(0xFF06B6D4)  // Electric cyan accent
val DarkTertiary = Color(0xFF8B5CF6)   // Mystic violet
val DarkOnBackground = Color(0xFFF3F4F6)
val DarkOnSurface = Color(0xFFE5E7EB)
val DarkOutline = Color(0xFF374151)

// Rating Button Colors (SRS)
object SrsColors {
    val Again = Color(0xFFEF4444)  // Vibrant Red
    val Hard = Color(0xFFF59E0B)   // Warm Amber/Orange
    val Good = Color(0xFF10B981)   // Emerald Green
    val Easy = Color(0xFF3B82F6)   // Sky Blue
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD946EF), // Fuchsia
    secondary = Color(0xFF0EA5E9), // Sky Blue
    tertiary = Color(0xFF6366F1), // Indigo
    background = Color(0xFFFAF9F6), // Warm white
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF1E293B),
    outline = Color(0xFFE2E8F0)
)

val LangerTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun LangerTheme(
    darkTheme: Boolean = isSystemInDarkTheme() || true, // Default to dark theme for premium aesthetics
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LangerTypography,
        content = content
    )
}
