package com.example.mobileproject.domain.entity

data class ChatMessage(
    val id: String,
    val text: String,
    val senderUsername: String?,
    val mine: Boolean,
    val createdAt: String?,
)
