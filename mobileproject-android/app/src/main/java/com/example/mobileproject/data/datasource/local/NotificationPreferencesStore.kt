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
 *
 * ## Các nhóm thông báo
 * | Key | Mặc định | Mô tả |
 * |-----|----------|-------|
 * | `notif_chat` | `true` | Thông báo tin nhắn chat |
 * | `notif_payment` | `true` | Thông báo thanh toán |
 * | `notif_transaction` | `true` | Thông báo giao dịch |
 * | `notif_goal` | `true` | Thông báo mục tiêu (goal) |
 * | `notif_memory` | `true` | Thông báo kỷ niệm (memory) |
 *
 * ## Flow semantics
 * Mỗi thuộc tính là [Flow] phát giá trị boolean hiện tại, tự động cập nhật khi thay đổi.
 * Nếu đọc lỗi (DataStore corruption), flow sẽ emit `true` (bật) qua `catch`.
 *
 * ## Threading
 * Các hàm `set*` là `suspend`, chạy trên DataStore's internal dispatcher.
 */
@Singleton
class NotificationPreferencesStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.notificationPrefsDataStore

    /** Flow bật/tắt thông báo tin nhắn chat. Mặc định: `true`. */
    val notifChat: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIF_CHAT] ?: true }
        .catch { emit(true) }

    /** Flow bật/tắt thông báo thanh toán. Mặc định: `true`. */
    val notifPayment: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIF_PAYMENT] ?: true }
        .catch { emit(true) }

    /** Flow bật/tắt thông báo giao dịch. Mặc định: `true`. */
    val notifTransaction: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIF_TRANSACTION] ?: true }
        .catch { emit(true) }

    /** Flow bật/tắt thông báo mục tiêu. Mặc định: `true`. */
    val notifGoal: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIF_GOAL] ?: true }
        .catch { emit(true) }

    /** Flow bật/tắt thông báo kỷ niệm. Mặc định: `true`. */
    val notifMemory: Flow<Boolean> = dataStore.data
        .map { it[KEY_NOTIF_MEMORY] ?: true }
        .catch { emit(true) }

    /** Bật/tắt thông báo chat. */
    suspend fun setNotifChat(value: Boolean) {
        dataStore.edit { it[KEY_NOTIF_CHAT] = value }
    }

    /** Bật/tắt thông báo thanh toán. */
    suspend fun setNotifPayment(value: Boolean) {
        dataStore.edit { it[KEY_NOTIF_PAYMENT] = value }
    }

    /** Bật/tắt thông báo giao dịch. */
    suspend fun setNotifTransaction(value: Boolean) {
        dataStore.edit { it[KEY_NOTIF_TRANSACTION] = value }
    }

    /** Bật/tắt thông báo mục tiêu. */
    suspend fun setNotifGoal(value: Boolean) {
        dataStore.edit { it[KEY_NOTIF_GOAL] = value }
    }

    /** Bật/tắt thông báo kỷ niệm. */
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
