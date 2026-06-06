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

/**
 * DataStore lưu trữ cài đặt người dùng (privacy & notification preferences).
 *
 * ## Các cài đặt được lưu trữ
 * | Key | Mặc định | Mô tả |
 * |-----|----------|-------|
 * | `push_notifications` | `true` | Bật/tắt thông báo đẩy |
 * | `email_notifications` | `false` | Bật/tắt thông báo qua email |
 * | `show_activity_status` | `true` | Hiển thị trạng thái hoạt động cho đối phương |
 * | `searchable_by_email` | `true` | Cho phép người khác tìm kiếm bằng email |
 *
 * ## Flow semantics
 * Mỗi thuộc tính là một [Flow] phát giá trị hiện tại và tự động cập nhật khi thay đổi.
 * Nếu đọc lỗi (corruption), flow sẽ phát giá trị mặc định qua `catch`.
 *
 * ## Threading
 * Các hàm `set*` là `suspend`, chạy trên DataStore's internal dispatcher (không block main thread).
 */
private val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings_store",
)

@Singleton
class UserSettingsStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.userSettingsDataStore

    /** Flow bật/tắt thông báo đẩy. Mặc định: `true`. */
    val pushNotifications: Flow<Boolean> = dataStore.data
        .map { it[KEY_PUSH_NOTIFICATIONS] ?: true }
        .catch { emit(true) }

    /** Flow bật/tắt thông báo email. Mặc định: `false`. */
    val emailNotifications: Flow<Boolean> = dataStore.data
        .map { it[KEY_EMAIL_NOTIFICATIONS] ?: false }
        .catch { emit(false) }

    /** Flow hiển thị trạng thái hoạt động cho đối phương. Mặc định: `true`. */
    val showActivityStatus: Flow<Boolean> = dataStore.data
        .map { it[KEY_SHOW_ACTIVITY_STATUS] ?: true }
        .catch { emit(true) }

    /** Flow cho phép tìm kiếm bằng email. Mặc định: `true`. */
    val searchableByEmail: Flow<Boolean> = dataStore.data
        .map { it[KEY_SEARCHABLE_BY_EMAIL] ?: true }
        .catch { emit(true) }

    /** Bật/tắt thông báo đẩy. */
    suspend fun setPushNotifications(value: Boolean) {
        dataStore.edit { it[KEY_PUSH_NOTIFICATIONS] = value }
    }

    /** Bật/tắt thông báo email. */
    suspend fun setEmailNotifications(value: Boolean) {
        dataStore.edit { it[KEY_EMAIL_NOTIFICATIONS] = value }
    }

    /** Bật/tắt hiển thị trạng thái hoạt động. */
    suspend fun setShowActivityStatus(value: Boolean) {
        dataStore.edit { it[KEY_SHOW_ACTIVITY_STATUS] = value }
    }

    /** Bật/tắt cho phép tìm kiếm bằng email. */
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
