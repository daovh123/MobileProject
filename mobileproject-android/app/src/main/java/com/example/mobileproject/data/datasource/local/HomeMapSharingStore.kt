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
 * DataStore lưu trữ cài đặt chia sẻ vị trí trên bản đồ trang chủ (Home Map).
 *
 * ## Cài đặt được lưu trữ
 * - **Share location enabled**: bật/tắt chia sẻ vị trí real-time cho đối phương xem trên bản đồ.
 *
 * ## Giá trị mặc định
 * Mặc định là `false` (tắt chia sẻ vị trí) để bảo vệ quyền riêng tư.
 *
 * ## Flow semantics
 * [shareLocationEnabled] là [Flow] tự động phát giá trị mới khi thay đổi cài đặt.
 */
private val Context.homeMapSharingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "home_map_sharing_store",
)

@Singleton
class HomeMapSharingStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.homeMapSharingDataStore

    /** Flow trạng thái bật/tắt chia sẻ vị trí. Mặc định: `false`. */
    val shareLocationEnabled: Flow<Boolean> = dataStore.data
        .map { it[KEY_SHARE_LOCATION] ?: false }
        .catch { emit(false) }

    /**
     * Bật/tắt chia sẻ vị trí trên bản đồ trang chủ.
     *
     * @param enabled `true` để bật chia sẻ, `false` để tắt
     */
    suspend fun setShareLocationEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SHARE_LOCATION] = enabled }
    }

    private companion object {
        val KEY_SHARE_LOCATION = booleanPreferencesKey("share_location_enabled")
    }
}
