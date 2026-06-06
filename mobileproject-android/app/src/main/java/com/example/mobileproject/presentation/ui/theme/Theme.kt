/**
 * Theme chính của ứng dụng "Love Wallet".
 *
 * Cấu hình MaterialTheme với typography (Plus Jakarta Sans),
 * shapes (bo góc Airbnb-inspired), color scheme (light/dark/dynamic),
 * và extended colors (gradient, glass, accent).
 */
package com.example.mobileproject.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font as GoogleFontEntry
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.R

/**
 * Font provider Google Fonts cho ứng dụng.
 * Sử dụng Plus Jakarta Sans — font sans-serif hiện đại,
 * gần với Inter (font được Love Wallet khuyến nghị).
 */
private val AppFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

/**
 * Font family chính của ứng dụng — Plus Jakarta Sans.
 * Load từ Google Fonts với certificate verification.
 */
private val AppFontFamily = FontFamily(
    GoogleFontEntry(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = AppFontProvider,
    ),
)

/**
 * Hệ thống typography cho "Love Wallet Design System".
 *
 * Dựa trên Plus Jakarta Sans (gần với Inter — Love Wallet khuyến nghị).
 * - Display tiers sử dụng SemiBold (phong cách Airbnb — ưu tiên nội dung hình ảnh hơn typography).
 * - Financial labels sử dụng letter-spacing chặt cho hiển thị số tiền sạch (lấy cảm hứng từ Stripe).
 * - Negative letter-spacing cho display/headline tạo cảm giác gọn gàng, hiện đại.
 * - Label sử dụng letterSpacing 0.1-0.4.sp cho readability tốt hơn ở kích thước nhỏ.
 */
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 38.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.0).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.6).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)

/**
 * Hệ thống hình dạng (shape) cho "Love Wallet Design System".
 *
 * Bo góc mềm mại, hào phóng — lấy cảm hứng từ Airbnb.
 * - extraSmall: 10dp (chip, tag nhỏ).
 * - small: 14dp (input field, metric chip).
 * - medium: 16dp (button, card nhỏ).
 * - large: 24dp (card tiêu chuẩn).
 * - extraLarge: 28dp (dialog, card lớn).
 */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Theme chính của ứng dụng "Love Wallet".
 *
 * Cấu hình MaterialTheme với:
 * - Color scheme: Light/Dark tùy theo system theme hoặc dynamic color (Android 12+).
 * - Typography: Plus Jakarta Sans với các tier SemiBold/Normal/Medium.
 * - Shapes: Bo góc mềm mại Airbnb-inspired.
 * - Extended colors: Gradient, glass, accent colors qua [LocalExtendedColors].
 *
 * @param darkTheme true nếu sử dụng dark mode. Mặc định theo system.
 * @param dynamicColor true nếu sử dụng Material You dynamic color (Android 12+). Mặc định false.
 * @param content Nội dung composable được bọc trong theme.
 */
@Composable
fun MobileProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> MobileProjectDarkColorScheme
        else -> MobileProjectLightColorScheme
    }

    val extendedColors = if (darkTheme) {
        ExtendedColorScheme.dark()
    } else {
        ExtendedColorScheme.light()
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
