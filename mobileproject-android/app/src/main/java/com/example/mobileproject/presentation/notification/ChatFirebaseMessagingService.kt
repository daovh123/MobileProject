package com.example.mobileproject.presentation.notification

import android.util.Log
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.notification.FcmTokenRequestDto
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    @Inject
    lateinit var apiService: ApiService

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        registerFcmTokenIfLoggedIn(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val type = message.data["type"].orEmpty()
        if (type != "chat_message") {
            return
        }

        val conversationKey = message.data["coupleId"].orEmpty().ifBlank { "couple_chat" }
        val senderUsername = message.data["senderUsername"].orEmpty().ifBlank { "Đối phương" }
        val conversationTitle = message.data["conversationTitle"].orEmpty().ifBlank { senderUsername }

        val text = message.data["text"]
            ?: message.data["body"]
            ?: message.notification?.body
            ?: return

        val session = authSessionStore.load()
        val myUsername = session?.username

        if (ChatNotificationGate.shouldSuppressIncomingChatNotifications()) {
            return
        }

        if (ChatNotificationGate.shouldShowInAppIncomingChatNotifications()) {
            ChatInAppNotificationBus.emit(
                InAppChatNotification(
                    conversationKey = conversationKey,
                    conversationTitle = conversationTitle,
                    senderUsername = senderUsername,
                    messageText = text,
                )
            )
            return
        }

        ChatNotifications.showIncomingMessage(
            context = this,
            conversationKey = conversationKey,
            conversationTitle = conversationTitle,
            myUsername = myUsername,
            senderUsername = senderUsername,
            messageText = text,
        )
    }

    private fun registerFcmTokenIfLoggedIn(fcmToken: String) {
        val fcm = fcmToken.trim()
        if (fcm.isBlank()) {
            return
        }

        val session = authSessionStore.load() ?: return
        val accessToken = session.token.trim()
        if (accessToken.isBlank()) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                apiService.registerFcmToken(
                    authorization = "Bearer $accessToken",
                    request = FcmTokenRequestDto(token = fcm),
                )
            }.onFailure { error ->
                Log.d("ChatFCM", "Failed to register FCM token: ${error.message}")
            }
        }
    }
}
