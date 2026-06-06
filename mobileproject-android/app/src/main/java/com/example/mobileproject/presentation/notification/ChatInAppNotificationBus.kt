/**
 * Bus sự kiện thông báo chat in-app.
 *
 * Phát sự kiện [InAppChatNotification] qua [SharedFlow] khi
 * ứng dụng đang mở nhưng màn hình chat không active.
 */
package com.example.mobileproject.presentation.notification

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Dữ liệu thông báo chat in-app.
 *
 * @param conversationKey Khóa định danh cuộc trò chuyện.
 * @param conversationTitle Tiêu đề cuộc trò chuyện (tên đối phương).
 * @param senderUsername Tên người gửi tin nhắn.
 * @param messageText Nội dung tin nhắn.
 */
data class InAppChatNotification(
    val conversationKey: String,
    val conversationTitle: String,
    val senderUsername: String,
    val messageText: String,
)

/**
 * Bus sự kiện thông báo chat in-app.
 *
 * Sử dụng [SharedFlow] với buffer 16 và chiến lược DROP_OLDEST
 * để phát sự kiện thông báo chat khi ứng dụng đang mở nhưng
 * màn hình chat không active.
 *
 * Các composable collect [events] để hiển thị snackbar hoặc banner
 * thông báo tin nhắn đến trong ứng dụng.
 */
object ChatInAppNotificationBus {

    private val _events = MutableSharedFlow<InAppChatNotification>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events: SharedFlow<InAppChatNotification> = _events.asSharedFlow()

    fun emit(notification: InAppChatNotification) {
        _events.tryEmit(notification)
    }
}
