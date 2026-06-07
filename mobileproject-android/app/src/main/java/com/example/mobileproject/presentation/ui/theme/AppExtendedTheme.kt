package com.example.mobileproject.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Love Wallet Extended Color Scheme
 *
 * Provides gradient, overlay, and accent colors specific to the Love Wallet design language.
 * Light mode: Warm pink gradients, soft glass, romantic accents
 * Dark mode: Warm dark tones with rose undertones (NOT cold crypto-exchange dark)
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
    val warningAmber: Color,
    val loveNoteBackground: Color,
    val anniversaryGold: Color,
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

    /** Soft warm gradient for balance cards and hero sections */
    val balanceCardGradient: Brush
        get() = Brush.linearGradient(
            listOf(
                cardGradientStart,
                cardGradientEnd,
                gradientEnd,
            )
        )

    companion object {
        fun light() = ExtendedColorScheme(
            gradientStart = Color(0xFFFF6B8A),
            gradientEnd = Color(0xFFFE8A8E),
            shimmer = Color(0xFFFFF1F3),
            cardGradientStart = Color(0xFFFF7A92),
            cardGradientEnd = Color(0xFFFE8A8E),
            overlayScrim = Color(0xFF1F1F1F).copy(alpha = 0.48f),
            glassBackground = Color(0xFFFFFFFF).copy(alpha = 0.88f),
            glassBorder = Color(0xFFFFFFFF).copy(alpha = 0.45f),
            accentWarm = Color(0xFFFFA07A),
            accentSoft = Color(0xFFFFF1F3),
            heartPulse = Color(0xFFFE8A8E),
            successGreen = Color(0xFF22C55E),
            warningAmber = Color(0xFFF59E0B),
            loveNoteBackground = Color(0xFFFFEBEF),
            anniversaryGold = Color(0xFFD4A574),
        )

        fun dark() = ExtendedColorScheme(
            gradientStart = Color(0xFFFF7A8F),
            gradientEnd = Color(0xFFCC2D4A),
            shimmer = Color(0xFF3D3533),
            cardGradientStart = Color(0xFFD4607A),
            cardGradientEnd = Color(0xFF8C1A32),
            overlayScrim = Color(0xFF0A0807).copy(alpha = 0.65f),
            glassBackground = Color(0xFF1A1614).copy(alpha = 0.90f),
            glassBorder = Color(0xFFF5EDE8).copy(alpha = 0.10f),
            accentWarm = Color(0xFFE8956E),
            accentSoft = Color(0xFF3D2A2A),
            heartPulse = Color(0xFFFF7A8F),
            successGreen = Color(0xFF86EFAC),
            warningAmber = Color(0xFFFBBF24),
            loveNoteBackground = Color(0xFF2A2220),
            anniversaryGold = Color(0xFFD4A574),
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
