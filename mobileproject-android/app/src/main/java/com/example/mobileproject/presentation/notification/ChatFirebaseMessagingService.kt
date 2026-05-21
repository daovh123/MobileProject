package com.example.mobileproject.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.local.NotificationPreferencesStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.notification.FcmTokenRequestDto
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var notifPrefs: NotificationPreferencesStore

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        registerFcmTokenIfLoggedIn(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val type = message.data["type"].orEmpty().lowercase()
        NotificationRefreshBus.emitRefresh()

        CoroutineScope(Dispatchers.IO).launch {
            when (type) {
                "chat_message" -> handleChatMessage(message)
                "payment" -> handleGeneralNotification(
                    message, type,
                    enabled = notifPrefs.notifPayment.first(),
                    channelId = "channel_payment",
                    channelName = "Nạp tiền",
                )
                "transaction" -> handleGeneralNotification(
                    message, type,
                    enabled = notifPrefs.notifTransaction.first(),
                    channelId = "channel_transaction",
                    channelName = "Chi tiêu",
                )
                "goal_created", "goal_updated", "goal_completed", "goal" -> handleGeneralNotification(
                    message, type,
                    enabled = notifPrefs.notifGoal.first(),
                    channelId = "channel_goal",
                    channelName = "Mục tiêu",
                )
                "partner_memory", "memory" -> handleGeneralNotification(
                    message, type,
                    enabled = notifPrefs.notifMemory.first(),
                    channelId = "channel_memory",
                    channelName = "Kỷ niệm",
                )
                else -> {
                    // Unknown type: show as general if has notification payload
                    val notif = message.notification
                    if (notif != null) {
                        showSystemNotification(
                            title = notif.title ?: "Thông báo",
                            body = notif.body ?: "",
                            channelId = "channel_general",
                            channelName = "Chung",
                            notificationId = type.hashCode(),
                        )
                    }
                }
            }
        }
    }

    private suspend fun handleChatMessage(message: RemoteMessage) {
        val chatEnabled = notifPrefs.notifChat.first()
        if (!chatEnabled) return

        val conversationKey = message.data["coupleId"].orEmpty().ifBlank { "couple_chat" }
        val senderUsername = message.data["senderUsername"].orEmpty().ifBlank { "Đối phương" }
        val conversationTitle = message.data["conversationTitle"].orEmpty().ifBlank { senderUsername }
        val text = message.data["text"]
            ?: message.data["body"]
            ?: message.notification?.body
            ?: return

        val session = authSessionStore.load()
        val myUsername = session?.username

        if (ChatNotificationGate.shouldSuppressIncomingChatNotifications()) return

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

    private fun handleGeneralNotification(
        message: RemoteMessage,
        type: String,
        enabled: Boolean,
        channelId: String,
        channelName: String,
    ) {
        if (!enabled) return

        val title = message.data["title"]
            ?: message.notification?.title
            ?: "Thông báo"
        val body = message.data["body"]
            ?: message.notification?.body
            ?: return

        showSystemNotification(
            title = title,
            body = body,
            channelId = channelId,
            channelName = channelName,
            notificationId = System.currentTimeMillis().toInt(),
        )
    }

    private fun showSystemNotification(
        title: String,
        body: String,
        channelId: String,
        channelName: String,
        notificationId: Int,
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = channelName
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open app on tap
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = if (intent != null) {
            PendingIntent.getActivity(
                this, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        } else null

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notifications_24)
            .setAutoCancel(true)
            .apply { if (pendingIntent != null) setContentIntent(pendingIntent) }
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun registerFcmTokenIfLoggedIn(fcmToken: String) {
        val fcm = fcmToken.trim()
        if (fcm.isBlank()) return

        val session = authSessionStore.load() ?: return
        val accessToken = session.token.trim()
        if (accessToken.isBlank()) return

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
