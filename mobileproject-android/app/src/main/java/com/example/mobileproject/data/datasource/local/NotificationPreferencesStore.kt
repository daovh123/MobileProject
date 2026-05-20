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

private val Context.notificationPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "notification_preferences",
)

/**
 * DataStore lưu trữ cài đặt bật/tắt từng nhóm thông báo.
 * Mặc định tất cả đều bật.
 */
@Singleton
class NotificationPreferencesStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.notificationPrefsDataStore

    val notifChat: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIF_CHAT] ?: true }
        .catch { emit(true) }

    val notifPayment: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIF_PAYMENT] ?: true }
        .catch { emit(true) }

    val notifTransaction: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIF_TRANSACTION] ?: true }
        .catch { emit(true) }

    val notifGoal: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIF_GOAL] ?: true }
        .catch { emit(true) }

    val notifMemory: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIF_MEMORY] ?: true }
        .catch { emit(true) }

    suspend fun setNotifChat(value: Boolean) {
        dataStore.edit { it[KEY_NOTIF_CHAT] = value }
    }

    suspend fun setNotifPayment(value: Boolean) {
        dataStore.edit { it[KEY_NOTIF_PAYMENT] = value }
    }

    suspend fun setNotifTransaction(value: Boolean) {
        dataStore.edit { it[KEY_NOTIF_TRANSACTION] = value }
    }

    suspend fun setNotifGoal(value: Boolean) {
        dataStore.edit { it[KEY_NOTIF_GOAL] = value }
    }

    suspend fun setNotifMemory(value: Boolean) {
        dataStore.edit { it[KEY_NOTIF_MEMORY] = value }
    }

    private companion object {
        val KEY_NOTIF_CHAT = booleanPreferencesKey("notif_chat")
        val KEY_NOTIF_PAYMENT = booleanPreferencesKey("notif_payment")
        val KEY_NOTIF_TRANSACTION = booleanPreferencesKey("notif_transaction")
        val KEY_NOTIF_GOAL = booleanPreferencesKey("notif_goal")
        val KEY_NOTIF_MEMORY = booleanPreferencesKey("notif_memory")
    }
}
