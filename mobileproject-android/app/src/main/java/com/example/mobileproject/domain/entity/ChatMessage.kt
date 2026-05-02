package com.example.mobileproject.domain.entity

data class ChatMessage(
    val id: String,
    val text: String,
    val senderUsername: String?,
    val mine: Boolean,
    val createdAt: String?,
    val senderAvatarUrl: String? = null,
    val readStatus: ReadStatus = ReadStatus.SENT,
    val replyToId: String? = null,
)

enum class ReadStatus { SENT, DELIVERED, READ }
