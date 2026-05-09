package com.example.mobileproject.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings_store",
)

@Singleton
class UserSettingsStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.userSettingsDataStore

    val pushNotifications: Flow<Boolean> = dataStore.data
        .map { it[KEY_PUSH_NOTIFICATIONS] ?: true }
        .catch { emit(true) }

    val emailNotifications: Flow<Boolean> = dataStore.data
        .map { it[KEY_EMAIL_NOTIFICATIONS] ?: false }
        .catch { emit(false) }

    val showActivityStatus: Flow<Boolean> = dataStore.data
        .map { it[KEY_SHOW_ACTIVITY_STATUS] ?: true }
        .catch { emit(true) }

    val searchableByEmail: Flow<Boolean> = dataStore.data
        .map { it[KEY_SEARCHABLE_BY_EMAIL] ?: true }
        .catch { emit(true) }

    suspend fun setPushNotifications(value: Boolean) {
        dataStore.edit { it[KEY_PUSH_NOTIFICATIONS] = value }
    }

    suspend fun setEmailNotifications(value: Boolean) {
        dataStore.edit { it[KEY_EMAIL_NOTIFICATIONS] = value }
    }

    suspend fun setShowActivityStatus(value: Boolean) {
        dataStore.edit { it[KEY_SHOW_ACTIVITY_STATUS] = value }
    }

    suspend fun setSearchableByEmail(value: Boolean) {
        dataStore.edit { it[KEY_SEARCHABLE_BY_EMAIL] = value }
    }

    private companion object {
        val KEY_PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
        val KEY_EMAIL_NOTIFICATIONS = booleanPreferencesKey("email_notifications")
        val KEY_SHOW_ACTIVITY_STATUS = booleanPreferencesKey("show_activity_status")
        val KEY_SEARCHABLE_BY_EMAIL = booleanPreferencesKey("searchable_by_email")
    }
}
