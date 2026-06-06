/**
 * Enum quản lý chế độ theme (System/Light/Dark) và utility resolve.
 */
package com.example.mobileproject.presentation.ui.theme

/**
 * Chế độ hiển thị theme của ứng dụng.
 *
 * - [SYSTEM]: Theo cài đặt hệ thống (mặc định).
 * - [LIGHT]: Luôn sử dụng light mode.
 * - [DARK]: Luôn sử dụng dark mode.
 *
 * Giá trị [value] được lưu trữ dưới dạng chuỗi trong DataStore/SharedPreferences.
 */
enum class ThemeMode(val value: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    /**
     * Chuyển đổi từ chuỗi giá trị sang [ThemeMode].
     *
     * @param value Chuỗi giá trị ("system", "light", "dark").
     * @return [ThemeMode] tương ứng, mặc định [SYSTEM] nếu không khớp.
     */
    companion object {
        fun fromValue(value: String?): ThemeMode {
            return entries.firstOrNull { it.value == value } ?: SYSTEM
        }
    }
}

/**
 * Quyết định có sử dụng dark theme hay không dựa trên [ThemeMode] và theme hệ thống.
 *
 * @param isSystemDark true nếu hệ thống đang ở dark mode.
 * @return true nếu ứng dụng nên sử dụng dark theme.
 */
fun ThemeMode.resolveDarkTheme(isSystemDark: Boolean): Boolean {
    return when (this) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}
