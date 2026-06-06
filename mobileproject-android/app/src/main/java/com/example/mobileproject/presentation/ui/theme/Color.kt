/**
 * Định nghĩa bảng màu Light và Dark cho "Love Wallet Design System".
 *
 * Cung cấp [MobileProjectLightColorScheme] và [MobileProjectDarkColorScheme]
 * với thiết kế ấm áp, lãng mạn — lấy cảm hứng từ Airbnb + Stripe + Love Wallet.
 */
package com.example.mobileproject.presentation.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Bảng màu Light Mode cho "Love Wallet Design System".
 *
 * Thiết kế lấy cảm hứng từ sự ấm áp của Airbnb, tính rõ ràng của Stripe
 * và phong cách lãng mạn của Love Wallet.
 *
 * - Primary: #FF385C (Airbnb Rausch — ấm, thân thiện, lãng mạn).
 * - Surfaces: Tông kem-trắng (#FFFBFA, #FFF7F8, #FFF1F3).
 * - Error: #EF4444 (đỏ rõ ràng cho cảnh báo).
 * - Tertiary: #FF6B81 (hồng nhạt bổ sung cho primary).
 *
 * Các surface container sử dụng thang hồng nhạt dần từ trắng → hồng,
 * tạo chiều sâu mà vẫn giữ cảm giác ấm áp.
 */
internal val MobileProjectLightColorScheme = lightColorScheme(
    primary = Color(0xFFFF385C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFF1F3),
    onPrimaryContainer = Color(0xFF3F0015),
    secondary = Color(0xFF6B7B8D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0E8F5),
    onSecondaryContainer = Color(0xFF22272E),
    tertiary = Color(0xFFFF6B81),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE0E6),
    onTertiaryContainer = Color(0xFF3F0015),
    background = Color(0xFFFFFBFA),
    onBackground = Color(0xFF1F1F1F),
    surface = Color(0xFFFFFBFA),
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFF5E8EB),
    onSurfaceVariant = Color(0xFF707070),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF7F8),
    surfaceContainer = Color(0xFFFFF1F3),
    surfaceContainerHigh = Color(0xFFFFE8EC),
    surfaceContainerHighest = Color(0xFFF5D3D9),
    inverseSurface = Color(0xFF362F30),
    inverseOnSurface = Color(0xFFFBEEEE),
    inversePrimary = Color(0xFFFFB2C3),
    outline = Color(0xFFBFB0B3),
    outlineVariant = Color(0xFFEFEFEF),
    scrim = Color(0xFF000000),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

/**
 * Bảng màu Dark Mode ấm ("Warm Dark") cho "Love Wallet Design System".
 *
 * Triết lý: "Warm dark mode" — nền tối với tông nâu/hồng ấm,
 * KHÔNG phải dark mode lạnh kiểu crypto-exchange. Ứng dụng vẫn
 * cảm giác lãng mạn và ấm cúng vào ban đêm.
 *
 * - Backgrounds: Nâu ấm (#1A1614, #221E1C) thay vì xám lạnh.
 * - Text: Trắng ấm (#F5EDE8) thay vì trắng tinh (#FFFFFF).
 * - Primary: Hồng ấm (#FF7A8F) thay vì hồng neon.
 * - Error: #FCA5A5 (đỏ nhạt dễ đọc trên nền tối).
 *
 * Surface container sử dụng thang nâu ấm từ tối → sáng.
 */
internal val MobileProjectDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF7A8F),
    onPrimary = Color(0xFF4A0012),
    primaryContainer = Color(0xFF6B1A2E),
    onPrimaryContainer = Color(0xFFFFE0E6),
    secondary = Color(0xFFC4B8CC),
    onSecondary = Color(0xFF2E2635),
    secondaryContainer = Color(0xFF443A4D),
    onSecondaryContainer = Color(0xFFE8DFF0),
    tertiary = Color(0xFFDC8A9A),
    onTertiary = Color(0xFF3F0015),
    tertiaryContainer = Color(0xFF5D2535),
    onTertiaryContainer = Color(0xFFFFE0E6),
    background = Color(0xFF1A1614),
    onBackground = Color(0xFFF5EDE8),
    surface = Color(0xFF1A1614),
    onSurface = Color(0xFFF5EDE8),
    surfaceVariant = Color(0xFF3D3533),
    onSurfaceVariant = Color(0xFFD4C4BF),
    surfaceContainerLowest = Color(0xFF14110F),
    surfaceContainerLow = Color(0xFF221E1C),
    surfaceContainer = Color(0xFF2A2523),
    surfaceContainerHigh = Color(0xFF352F2D),
    surfaceContainerHighest = Color(0xFF413A37),
    inverseSurface = Color(0xFFF5EDE8),
    inverseOnSurface = Color(0xFF362F2D),
    inversePrimary = Color(0xFFD42B4A),
    outline = Color(0xFF8C7F7B),
    outlineVariant = Color(0xFF3D3533),
    scrim = Color(0xFF000000),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
)
