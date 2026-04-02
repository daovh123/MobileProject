package com.example.mobileproject.domain.entity

data class AuthSession(
    val token: String,
    val username: String,
    val email: String,
    val profileCompleted: Boolean,
    val coupleConnected: Boolean,
)
