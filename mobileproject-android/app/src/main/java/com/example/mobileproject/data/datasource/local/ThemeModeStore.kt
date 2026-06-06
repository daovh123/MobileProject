package com.example.mobileproject.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mobileproject.presentation.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore lưu trữ chế độ giao diện (theme mode) của ứng dụng.
 *
 * ## Cài đặt được lưu trữ
 * - **Theme mode**: giá trị chuỗi đại diện cho [ThemeMode] (ví dụ: "system", "light", "dark").
 *
 * ## Giá trị mặc định
 * Nếu chưa lưu hoặc đọc lỗi, mặc định là [ThemeMode.SYSTEM] (theo hệ thống).
 *
 * ## Flow semantics
 * [themeMode] là [Flow] tự động phát giá trị mới khi người dùng thay đổi theme.
 */
private val Context.themeModeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_mode_store",
)

@Singleton
class ThemeModeStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.themeModeDataStore

    /** Flow chế độ giao diện hiện tại. Mặc định: [ThemeMode.SYSTEM]. */
    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { preferences ->
            ThemeMode.fromValue(preferences[KEY_THEME_MODE])
        }
        .catch { emit(ThemeMode.SYSTEM) }

    /**
     * Lưu chế độ giao diện mới.
     *
     * @param themeMode chế độ giao diện cần lưu
     */
    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = themeMode.value
        }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
