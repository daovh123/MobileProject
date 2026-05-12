package com.example.mobileproject.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Extended color scheme for custom colors outside of Material3's standard palette.
 * Provides gradient, overlay, and accent colors specific to the app's design language.
 */
data class ExtendedColorScheme(
    val gradientStart: Color,
    val gradientEnd: Color,
    val shimmer: Color,
    val cardGradientStart: Color,
    val cardGradientEnd: Color,
    val overlayScrim: Color,
    val glassBackground: Color,
    val glassBorder: Color,
    val accentWarm: Color,
    val accentSoft: Color,
    val heartPulse: Color,
    val successGreen: Color,
) {
    val primaryGradient: Brush
        get() = Brush.linearGradient(listOf(gradientStart, gradientEnd))

    val cardGradient: Brush
        get() = Brush.linearGradient(listOf(cardGradientStart, cardGradientEnd))

    val surfaceGradient: Brush
        get() = Brush.verticalGradient(
            listOf(
                glassBackground.copy(alpha = 0.95f),
                glassBackground.copy(alpha = 0.80f),
            )
        )

    companion object {
        fun light() = ExtendedColorScheme(
            gradientStart = Color(0xFFBA1A4B),
            gradientEnd = Color(0xFFE91E63),
            shimmer = Color(0xFFFFD9E1),
            cardGradientStart = Color(0xFFFF6B8A),
            cardGradientEnd = Color(0xFFBA1A4B),
            overlayScrim = Color(0xFF1A1113).copy(alpha = 0.52f),
            glassBackground = Color(0xFFFFFFFF).copy(alpha = 0.85f),
            glassBorder = Color(0xFFFFFFFF).copy(alpha = 0.40f),
            accentWarm = Color(0xFFFF8A80),
            accentSoft = Color(0xFFFFCDD2),
            heartPulse = Color(0xFFE91E63),
            successGreen = Color(0xFF4CAF50),
        )

        fun dark() = ExtendedColorScheme(
            gradientStart = Color(0xFFFFB2C3),
            gradientEnd = Color(0xFFFF6B8A),
            shimmer = Color(0xFF3D3234),
            cardGradientStart = Color(0xFFD0607A),
            cardGradientEnd = Color(0xFF900037),
            overlayScrim = Color(0xFF000000).copy(alpha = 0.65f),
            glassBackground = Color(0xFF1A1113).copy(alpha = 0.88f),
            glassBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f),
            accentWarm = Color(0xFFFF8A80),
            accentSoft = Color(0xFF5D1F35),
            heartPulse = Color(0xFFFF6B8A),
            successGreen = Color(0xFF81C784),
        )
    }
}

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColorScheme.light() }

/**
 * Access extended colors from composable functions.
 */
object AppTheme {
    val extendedColors: ExtendedColorScheme
        @Composable
        get() = LocalExtendedColors.current
}
