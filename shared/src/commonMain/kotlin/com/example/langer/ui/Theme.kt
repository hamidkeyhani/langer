package com.example.langer.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// BrainBob-inspired color palette
val BrainBobIndigo = Color(0xFF5D53C1)      // Signature Indigo
val BrainBobLightBg = Color(0xFFF5F6FA)     // Warm off-white
val BrainBobLightSurface = Color(0xFFFFFFFF)
val BrainBobLightSurfaceVariant = Color(0xFFECEEF5)

val BrainBobDarkBg = Color(0xFF12131A)      // Deep dark navy-purple
val BrainBobDarkSurface = Color(0xFF1C1D2A) // Dark slate
val BrainBobDarkSurfaceVariant = Color(0xFF252739)
val BrainBobDarkPrimary = Color(0xFF7B73E6) // Soft vibrant indigo for dark mode

// Action colors matching BrainBob list styling
object ActionColors {
    val Red = Color(0xFFEF4444)
    val Blue = Color(0xFF3B82F6)
    val Orange = Color(0xFFF97316)
    val Green = Color(0xFF10B981)
}

// Spaced Repetition rating colors (adapted to BrainBob theme)
object SrsColors {
    val Again = Color(0xFFEF4444)  // Red
    val Hard = Color(0xFF64748B)   // Slate Gray
    val Good = Color(0xFF10B981)   // Green
    val Easy = Color(0xFF3B82F6)   // Blue
}

private val DarkColorScheme = darkColorScheme(
    primary = BrainBobDarkPrimary,
    secondary = Color(0xFF38BDF8), // Light blue
    tertiary = Color(0xFFF472B6),  // Pink
    background = BrainBobDarkBg,
    surface = BrainBobDarkSurface,
    surfaceVariant = BrainBobDarkSurfaceVariant,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFE2E8F0),
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = BrainBobIndigo,
    secondary = Color(0xFF0284C7), // Blue
    tertiary = Color(0xFFDB2777),  // Fuchsia
    background = BrainBobLightBg,
    surface = BrainBobLightSurface,
    surfaceVariant = BrainBobLightSurfaceVariant,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF1E293B),
    outline = Color(0xFFCBD5E1)
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
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
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
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun LangerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LangerTypography,
        content = content
    )
}
