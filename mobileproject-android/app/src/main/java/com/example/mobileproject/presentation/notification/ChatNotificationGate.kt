/**
 * Cổng kiểm soát hiển thị thông báo chat.
 *
 * Quản lý trạng thái foreground/background của ứng dụng và
 * màn hình chat để quyết định hiển thị thông báo hệ thống,
 * in-app notification, hoặc ẩn hoàn toàn.
 */
package com.example.mobileproject.presentation.notification

/**
 * Cổng kiểm soát hiển thị thông báo chat dựa trên trạng thái ứng dụng.
 *
 * Quản lý 2 trạng thái:
 * - [appInForeground]: Ứng dụng có đang hiển thị trên màn hình không.
 * - [chatRouteActive]: Màn hình chat có đang mở không.
 *
 * Logic quyết định:
 * - Ứng dụng foreground + chat đang mở → Ẩn thông báo hệ thống.
 * - Ứng dụng foreground + chat không mở → Hiển thị in-app notification.
 * - Ứng dụng background → Hiển thị thông báo hệ thống bình thường.
 */
object ChatNotificationGate {

    @Volatile
    private var appInForeground: Boolean = false

    @Volatile
    private var chatRouteActive: Boolean = false

    /**
     * Thiết lập trạng thái resumed của HomeActivity.
     * Được gọi từ lifecycle observer của Activity.
     */
    fun setHomeActivityResumed(resumed: Boolean) {
        setAppInForeground(resumed)
    }

    fun setAppInForeground(inForeground: Boolean) {
        appInForeground = inForeground
    }

    fun setChatRouteActive(active: Boolean) {
        chatRouteActive = active
    }

    fun isAppInForeground(): Boolean {
        return appInForeground
    }

    fun isChatRouteActive(): Boolean {
        return chatRouteActive
    }

    /**
     * Kiểm tra có nên ẩn thông báo chat hệ thống không.
     * true khi ứng dụng đang mở VÀ màn hình chat đang active.
     */
    fun shouldSuppressIncomingChatNotifications(): Boolean {
        return appInForeground && chatRouteActive
    }

    /**
     * Kiểm tra có nên hiển thị in-app notification không.
     * true khi ứng dụng đang mở NHƯNG màn hình chat không active.
     */
    fun shouldShowInAppIncomingChatNotifications(): Boolean {
        return appInForeground && !chatRouteActive
    }
}
