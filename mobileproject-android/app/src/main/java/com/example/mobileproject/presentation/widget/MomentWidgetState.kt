/**
 * DataStore Preferences và keys cho widget kỷ niệm.
 *
 * Lưu trữ title, imagePath, updatedAt của moment hiện tại.
 */
package com.example.mobileproject.presentation.widget

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore Preferences lưu trữ trạng thái widget kỷ niệm.
 *
 * File DataStore: "moment_widget"
 */
val Context.momentWidgetDataStore by preferencesDataStore(name = "moment_widget")

/**
 * Các khóa (keys) cho DataStore của widget kỷ niệm.
 *
 * - [title]: Tiêu đề kỷ niệm.
 * - [imagePath]: Đường dẫn file ảnh đã cache trong cacheDir.
 * - [updatedAt]: Thời gian cập nhật lần cuối (ISO string).
 */
object MomentWidgetKeys {
    val title = stringPreferencesKey("moment_title")
    val imagePath = stringPreferencesKey("moment_image_path")
    val updatedAt = stringPreferencesKey("moment_updated_at")
}
