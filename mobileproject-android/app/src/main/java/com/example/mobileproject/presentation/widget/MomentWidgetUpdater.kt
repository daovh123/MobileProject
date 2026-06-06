/**
 * Cập nhật dữ liệu cho MomentWidget.
 *
 * Tải ảnh, cache vào storage, lưu DataStore,
 * và gọi updateAll để refresh widget instances.
 */
package com.example.mobileproject.presentation.widget

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.glance.appwidget.updateAll
import com.example.mobileproject.R
import com.example.mobileproject.data.model.moment.MomentDto
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * Cập nhật dữ liệu cho [MomentWidget].
 *
 * Chịu trách nhiệm:
 * 1. Tải ảnh từ URL (hỗ trợ HTTP URL và data URI Base64).
 * 2. Cache ảnh vào `cacheDir/moment_widget_latest.jpg`.
 * 3. Lưu tiêu đề và đường dẫn ảnh vào [momentWidgetDataStore].
 * 4. Gọi [MomentWidget.updateAll] để refresh tất cả widget instances.
 *
 * Nếu moment là null, xóa toàn bộ dữ liệu widget.
 */
object MomentWidgetUpdater {

    private val client = OkHttpClient()

    /**
     * Cập nhật widget với kỷ niệm mới nhất.
     *
     * @param context Context.
     * @param moment [MomentDto] cần hiển thị, hoặc null để xóa widget.
     */
    suspend fun updateLatest(context: Context, moment: MomentDto?) {
        if (moment == null) {
            clearState(context)
            MomentWidget().updateAll(context)
            return
        }

        val imageBytes = loadImageBytes(moment.imageUrl)
        val imagePath = if (imageBytes != null) {
            val file = File(context.cacheDir, "moment_widget_latest.jpg")
            file.writeBytes(imageBytes)
            file.absolutePath
        } else {
            null
        }

        val title = moment.title.ifBlank { context.getString(R.string.memories_locket_title_fallback) }
        val updatedAt = moment.createdAt.orEmpty()

        context.momentWidgetDataStore.edit { prefs ->
            prefs[MomentWidgetKeys.title] = title
            if (imagePath == null) {
                prefs.remove(MomentWidgetKeys.imagePath)
            } else {
                prefs[MomentWidgetKeys.imagePath] = imagePath
            }
            prefs[MomentWidgetKeys.updatedAt] = updatedAt
        }

        MomentWidget().updateAll(context)
    }

    private suspend fun clearState(context: Context) {
        context.momentWidgetDataStore.edit { prefs ->
            prefs.remove(MomentWidgetKeys.title)
            prefs.remove(MomentWidgetKeys.imagePath)
            prefs.remove(MomentWidgetKeys.updatedAt)
        }
    }

    /**
     * Tải ảnh từ URL hoặc data URI.
     *
     * @param imageUrl URL ảnh (http/https) hoặc data URI (data:image/...;base64,...).
     * @return ByteArray của ảnh, hoặc null nếu tải thất bại.
     */
    private fun loadImageBytes(imageUrl: String): ByteArray? {
        if (imageUrl.isBlank()) return null
        if (imageUrl.startsWith("data:", ignoreCase = true)) {
            return decodeDataUri(imageUrl)
        }

        val request = Request.Builder().url(imageUrl).get().build()
        return runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                response.body?.bytes()
            }
        }.getOrNull()
    }

    /**
     * Giải mã data URI (data:image/...;base64,...) thành ByteArray.
     *
     * @param dataUri Chuỗi data URI.
     * @return ByteArray đã giải mã, hoặc null nếu format không hợp lệ.
     */
    private fun decodeDataUri(dataUri: String): ByteArray? {
        val parts = dataUri.split(",", limit = 2)
        if (parts.size < 2) return null
        return runCatching { Base64.decode(parts[1], Base64.DEFAULT) }.getOrNull()
    }
}
