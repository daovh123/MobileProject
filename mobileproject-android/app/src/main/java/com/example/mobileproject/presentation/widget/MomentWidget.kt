package com.example.mobileproject.presentation.widget

import android.content.Context
import android.graphics.BitmapFactory
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.action.actionStartActivity
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.screen.home.HomeHelper
import kotlinx.coroutines.flow.first
import java.io.File

class MomentWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.momentWidgetDataStore.data.first()
        val title = prefs[MomentWidgetKeys.title]
            ?: context.getString(R.string.memories_locket_widget_empty)
        val imagePath = prefs[MomentWidgetKeys.imagePath]

        val bitmap = imagePath?.let {
            val file = File(it)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        }

        provideContent {
            val openApp = actionStartActivity<HomeHelper>()
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color(0xFFF7F1EA)))
                    .clickable(openApp)
            ) {
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.fillMaxSize(),
                    )
                } else {
                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(16),
                    ) {
                        Text(
                            text = context.getString(R.string.memories_locket_widget_title),
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF3C2F2B)),
                                fontWeight = FontWeight.Bold,
                            )
                        )
                        Text(
                            text = title,
                            style = TextStyle(color = ColorProvider(Color(0xFF6B5E57))),
                            modifier = GlanceModifier.padding(top = 6),
                        )
                        Image(
                            provider = ImageProvider(R.drawable.ic_memories_24),
                            contentDescription = null,
                            modifier = GlanceModifier.padding(top = 12).size(28),
                        )
                    }
                }
            }
        }
    }
}
