/**
 * BroadcastReceiver xử lý trả lời nhanh chat từ notification.
 *
 * Nhận inline reply, hiển thị optimistic UI, gửi qua WebSocket,
 * và cập nhật trạng thái thất bại nếu cần.
 */
package com.example.mobileproject.presentation.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * BroadcastReceiver xử lý trả lời nhanh từ notification chat.
 *
 * Khi người dùng nhập tin nhắn qua inline reply trên notification:
 * 1. Trích xuất văn bản trả lời từ [RemoteInput].
 * 2. Hiển thị tin nhắn outgoing trên notification (optimistic UI).
 * 3. Gửi tin nhắn qua WebSocket sử dụng [ChatQuickReplySender].
 * 4. Nếu gửi thất bại, cập nhật notification hiển thị "Gửi thất bại".
 *
 * Sử dụng [goAsync] để giữ receiver sống trong tối đa 8 giây
 * chờ WebSocket hoàn thành.
 *
 * Action: [ChatNotifications.ACTION_REPLY]
 */
@AndroidEntryPoint
class ChatReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ChatNotifications.ACTION_REPLY) {
            return
        }

        val input = RemoteInput.getResultsFromIntent(intent)
        val replyText = input
            ?.getCharSequence(ChatNotifications.KEY_TEXT_REPLY)
            ?.toString()
            ?.trim()
            .orEmpty()

        if (replyText.isBlank()) {
            return
        }

        val conversationKey = intent.getStringExtra(ChatNotifications.EXTRA_CONVERSATION_KEY)
            .orEmpty()
            .ifBlank { "couple_chat" }

        val conversationTitle = intent.getStringExtra(ChatNotifications.EXTRA_CONVERSATION_TITLE)
            .orEmpty()
            .ifBlank { context.getString(R.string.chat_title) }

        val session = authSessionStore.load()
        val myUsername = session?.username

        ChatNotifications.showOutgoingMessage(
            context = context,
            conversationKey = conversationKey,
            conversationTitle = conversationTitle,
            myUsername = myUsername,
            messageText = replyText,
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val accessToken = authSessionStore.load()?.token.orEmpty()
            val success = if (accessToken.isBlank()) {
                false
            } else {
                withTimeoutOrNull(8_000) {
                    ChatQuickReplySender.send(accessToken, replyText)
                } ?: false
            }

            if (!success) {
                ChatNotifications.showSendFailed(
                    context = context,
                    conversationKey = conversationKey,
                    conversationTitle = conversationTitle,
                    myUsername = myUsername,
                )
            }

            pendingResult.finish()
        }
    }
}
