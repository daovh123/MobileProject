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

object MomentWidgetUpdater {

    private val client = OkHttpClient()

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

    private fun decodeDataUri(dataUri: String): ByteArray? {
        val parts = dataUri.split(",", limit = 2)
        if (parts.size < 2) return null
        return runCatching { Base64.decode(parts[1], Base64.DEFAULT) }.getOrNull()
    }
}
