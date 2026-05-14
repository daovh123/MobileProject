package com.example.mobileproject.presentation.widget

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.momentWidgetDataStore by preferencesDataStore(name = "moment_widget")

object MomentWidgetKeys {
    val title = stringPreferencesKey("moment_title")
    val imagePath = stringPreferencesKey("moment_image_path")
    val updatedAt = stringPreferencesKey("moment_updated_at")
}
