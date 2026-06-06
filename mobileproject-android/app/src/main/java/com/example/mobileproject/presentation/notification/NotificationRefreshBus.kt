/**
 * Bus sự kiện yêu cầu làm mới dữ liệu thông báo.
 *
 * Phát tín hiệu khi có thông báo FCM mới đến,
 * cho phép UI refresh danh sách thông báo.
 */
package com.example.mobileproject.presentation.notification

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bus sự kiện yêu cầu làm mới dữ liệu thông báo.
 *
 * Phát tín hiệu [Unit] mỗi khi có thông báo mới đến (từ FCM),
 * cho phép UI lắng nghe và refresh danh sách thông báo.
 *
 * Sử dụng [SharedFlow] với buffer 16 và chiến lược DROP_OLDEST.
 */
object NotificationRefreshBus {

    private val _events = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun emitRefresh() {
        _events.tryEmit(Unit)
    }
}
