/**
 * Quản lý hiển thị và lưu trữ thông báo chat trên Android.
 *
 * Cung cấp notification channel, MessagingStyle, inline reply,
 * và lưu trữ tin nhắn gần đây trong SharedPreferences.
 */
package com.example.mobileproject.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

/**
 * Quản lý hiển thị thông báo chat trên Android.
 *
 * Chịu trách nhiệm:
 * - Tạo và quản lý notification channel "chat_messages" (IMPORTANCE_HIGH).
 * - Hiển thị thông báo với MessagingStyle (hỗ trợ hiển thị lịch sử tin nhắn).
 * - Lưu trữ tin nhắn gần đây (tối đa 12) trong SharedPreferences.
 * - Hỗ trợ trả lời nhanh (inline reply) qua [ChatReplyReceiver].
 * - Mở HomeActivity (màn hình chat) khi nhấn vào thông báo.
 *
 * Sử dụng notification ID dựa trên hash của conversationKey để
 * đảm bảo mỗi cuộc trò chuyện có notification riêng biệt.
 */
object ChatNotifications {

    private const val CHANNEL_ID = "chat_messages"

    private const val PREFS_NAME = "chat_notification_store"
    private const val PREFS_KEY_MESSAGES_PREFIX = "messages_"
    private const val MAX_STORED_MESSAGES = 12

    const val ACTION_REPLY: String = "com.example.mobileproject.chat.ACTION_REPLY"

    const val EXTRA_CONVERSATION_KEY: String = "extra_conversation_key"
    const val EXTRA_CONVERSATION_TITLE: String = "extra_conversation_title"
    const val EXTRA_NOTIFICATION_ID: String = "extra_notification_id"

    const val KEY_TEXT_REPLY: String = "key_text_reply"

    private data class StoredMessage(
        val text: String,
        val senderName: String,
        val timestampMillis: Long,
        val mine: Boolean,
    )

    /**
     * Hiển thị thông báo tin nhắn đến từ đối phương.
     *
     * Lưu tin nhắn vào SharedPreferences, sau đó hiển thị notification
     * với MessagingStyle chứa lịch sử tin nhắn gần đây.
     *
     * @param context Context.
     * @param conversationKey Khóa định danh cuộc trò chuyện (thường là coupleId).
     * @param conversationTitle Tiêu đề cuộc trò chuyện (tên đối phương).
     * @param myUsername Tên người dùng hiện tại (dùng cho MessagingStyle).
     * @param senderUsername Tên người gửi.
     * @param messageText Nội dung tin nhắn.
     * @param timestampMillis Thời gian gửi (millis).
     */
    fun showIncomingMessage(
        context: Context,
        conversationKey: String,
        conversationTitle: String,
        myUsername: String?,
        senderUsername: String,
        messageText: String,
        timestampMillis: Long = System.currentTimeMillis(),
    ) {
        appendMessage(
            context = context,
            conversationKey = conversationKey,
            message = StoredMessage(
                text = messageText,
                senderName = senderUsername,
                timestampMillis = timestampMillis,
                mine = false,
            ),
        )

        val messages = loadMessages(context, conversationKey)
        notifyConversation(
            context = context,
            conversationKey = conversationKey,
            conversationTitle = conversationTitle,
            myUsername = myUsername,
            messages = messages,
            subText = null,
        )
    }

    /**
     * Hiển thị thông báo tin nhắn đã gửi (outgoing) để đồng bộ notification với chat.
     *
     * @param context Context.
     * @param conversationKey Khóa định danh cuộc trò chuyện.
     * @param conversationTitle Tiêu đề cuộc trò chuyện.
     * @param myUsername Tên người dùng hiện tại.
     * @param messageText Nội dung tin nhắn đã gửi.
     * @param timestampMillis Thời gian gửi.
     * @param subText Phụ đề optional (ví dụ: "Đang gửi...").
     */
    fun showOutgoingMessage(
        context: Context,
        conversationKey: String,
        conversationTitle: String,
        myUsername: String?,
        messageText: String,
        timestampMillis: Long = System.currentTimeMillis(),
        subText: String? = null,
    ) {
        val senderName = myUsername?.trim().takeIf { !it.isNullOrBlank() }
            ?: context.getString(R.string.chat_notification_you)

        appendMessage(
            context = context,
            conversationKey = conversationKey,
            message = StoredMessage(
                text = messageText,
                senderName = senderName,
                timestampMillis = timestampMillis,
                mine = true,
            ),
        )

        val messages = loadMessages(context, conversationKey)
        notifyConversation(
            context = context,
            conversationKey = conversationKey,
            conversationTitle = conversationTitle,
            myUsername = myUsername,
            messages = messages,
            subText = subText,
        )
    }

    /**
     * Hiển thị trạng thái gửi thất bại trên notification.
     *
     * Cập nhật subText của notification thành "Gửi thất bại"
     * mà không thay đổi nội dung tin nhắn đã lưu.
     */
    fun showSendFailed(
        context: Context,
        conversationKey: String,
        conversationTitle: String,
        myUsername: String?,
    ) {
        val messages = loadMessages(context, conversationKey)
        notifyConversation(
            context = context,
            conversationKey = conversationKey,
            conversationTitle = conversationTitle,
            myUsername = myUsername,
            messages = messages,
            subText = context.getString(R.string.chat_notification_send_failed),
        )
    }

    private fun notifyConversation(
        context: Context,
        conversationKey: String,
        conversationTitle: String,
        myUsername: String?,
        messages: List<StoredMessage>,
        subText: String?,
    ) {
        if (messages.isEmpty()) {
            return
        }

        ensureChannel(context)

        val notificationId = notificationIdForConversation(conversationKey)

        val myName = myUsername?.trim().takeIf { !it.isNullOrBlank() }
            ?: context.getString(R.string.chat_notification_you)

        val me = Person.Builder()
            .setName(myName)
            .setKey("me")
            .build()

        val style = NotificationCompat.MessagingStyle(me)
            .setConversationTitle(conversationTitle)
            .setGroupConversation(false)

        val lastMessage = messages.last()
        for (stored in messages) {
            val sender = if (stored.mine) {
                me
            } else {
                Person.Builder()
                    .setName(stored.senderName.ifBlank { conversationTitle })
                    .setKey("partner")
                    .build()
            }

            style.addMessage(stored.text, stored.timestampMillis, sender)
        }

        val contentText = lastMessage.text
        val contentIntent = createOpenChatPendingIntent(context)
        val replyAction = createReplyAction(
            context = context,
            conversationKey = conversationKey,
            conversationTitle = conversationTitle,
            notificationId = notificationId,
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_24)
            .setContentTitle(conversationTitle)
            .setContentText(contentText)
            .setStyle(style)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .addAction(replyAction)

        if (!subText.isNullOrBlank()) {
            builder.setSubText(subText)
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun createOpenChatPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, HomeActivity::class.java).apply {
            putExtra(HomeActivity.EXTRA_OPEN_CHAT, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createReplyAction(
        context: Context,
        conversationKey: String,
        conversationTitle: String,
        notificationId: Int,
    ): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(context.getString(R.string.chat_notification_reply_label))
            .build()

        val replyIntent = Intent(context, ChatReplyReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra(EXTRA_CONVERSATION_KEY, conversationKey)
            putExtra(EXTRA_CONVERSATION_TITLE, conversationTitle)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }

        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            replyIntent,
            pendingFlags,
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_notifications_24,
            context.getString(R.string.chat_notification_reply_action),
            replyPendingIntent,
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .build()
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.chat_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.chat_notification_channel_description)
        }

        manager.createNotificationChannel(channel)
    }

    private fun loadMessages(context: Context, conversationKey: String): List<StoredMessage> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(PREFS_KEY_MESSAGES_PREFIX + conversationKey, null).orEmpty()
        if (stored.isBlank()) {
            return emptyList()
        }

        val array = runCatching { JSONArray(stored) }.getOrNull() ?: return emptyList()
        val result = ArrayList<StoredMessage>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue

            val text = obj.optString("text").orEmpty()
            val sender = obj.optString("sender").orEmpty()
            val timestamp = obj.optLong("ts", 0L)
            val mine = obj.optBoolean("mine", false)

            if (text.isBlank()) {
                continue
            }

            result.add(
                StoredMessage(
                    text = text,
                    senderName = sender,
                    timestampMillis = timestamp,
                    mine = mine,
                )
            )
        }
        return result
    }

    private fun appendMessage(context: Context, conversationKey: String, message: StoredMessage) {
        val existing = loadMessages(context, conversationKey).toMutableList()
        existing.add(message)

        val trimmed = if (existing.size > MAX_STORED_MESSAGES) {
            existing.takeLast(MAX_STORED_MESSAGES)
        } else {
            existing
        }

        val array = JSONArray()
        for (stored in trimmed) {
            val obj = JSONObject()
                .put("text", stored.text)
                .put("sender", stored.senderName)
                .put("ts", stored.timestampMillis)
                .put("mine", stored.mine)
            array.put(obj)
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(PREFS_KEY_MESSAGES_PREFIX + conversationKey, array.toString())
            .apply()
    }

    private fun notificationIdForConversation(conversationKey: String): Int {
        val hash = conversationKey.hashCode()
        return if (hash == Int.MIN_VALUE) {
            1
        } else {
            abs(hash)
        }
    }
}
