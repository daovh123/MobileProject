package com.example.mobileproject.presentation.notification

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class InAppChatNotification(
    val conversationKey: String,
    val conversationTitle: String,
    val senderUsername: String,
    val messageText: String,
)

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
