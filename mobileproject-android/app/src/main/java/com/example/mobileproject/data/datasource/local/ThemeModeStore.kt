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

private val Context.themeModeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_mode_store",
)

@Singleton
class ThemeModeStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.themeModeDataStore

    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { preferences ->
            ThemeMode.fromValue(preferences[KEY_THEME_MODE])
        }
        .catch { emit(ThemeMode.SYSTEM) }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = themeMode.value
        }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
