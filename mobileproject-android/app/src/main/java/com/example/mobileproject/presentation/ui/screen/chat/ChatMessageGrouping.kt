package com.example.mobileproject.presentation.ui.screen.chat

import com.example.mobileproject.domain.entity.ChatMessage

internal data class GroupedMessage(
    val message: ChatMessage,
    val showAvatar: Boolean,
    val showTimestamp: Boolean,
)

internal fun groupMessages(
    messages: List<ChatMessage>,
    groupWindowMinutes: Long = 5,
): List<GroupedMessage> {
    if (messages.isEmpty()) return emptyList()
    val result = mutableListOf<GroupedMessage>()
    var i = 0
    while (i < messages.size) {
        val current = messages[i]
        var j = i
        while (j + 1 < messages.size) {
            val next = messages[j + 1]
            if (next.mine != current.mine || next.senderUsername != current.senderUsername) break
            val currentTime = parseInstantMillis(current.createdAt)
            val nextTime = parseInstantMillis(next.createdAt)
            if (nextTime - currentTime > groupWindowMinutes * 60_000) break
            j++
        }
        for (k in i until j) {
            result.add(GroupedMessage(messages[k], showAvatar = false, showTimestamp = false))
        }
        result.add(GroupedMessage(messages[j], showAvatar = true, showTimestamp = true))
        i = j + 1
    }
    return result
}

internal fun parseInstantMillis(createdAt: String?): Long {
    if (createdAt.isNullOrBlank()) return 0L
    return try {
        java.time.Instant.parse(createdAt.trim()).toEpochMilli()
    } catch (e: Exception) {
        0L
    }
}
