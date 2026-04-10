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
