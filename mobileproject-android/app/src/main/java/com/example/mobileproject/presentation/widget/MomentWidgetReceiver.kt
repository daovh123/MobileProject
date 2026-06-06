/**
 * GlanceAppWidgetReceiver liên kết [MomentWidget] với hệ thống Android.
 */
package com.example.mobileproject.presentation.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * GlanceAppWidgetReceiver cho [MomentWidget].
 *
 * Liên kết widget với hệ thống Android, cho phép hệ thống
 * khởi tạo và cập nhật widget trên màn hình chính.
 * Được đăng ký trong AndroidManifest.xml.
 */
class MomentWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MomentWidget()
}
