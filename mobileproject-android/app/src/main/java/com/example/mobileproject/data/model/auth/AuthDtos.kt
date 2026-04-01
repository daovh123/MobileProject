package com.example.mobileproject.data.model.auth

data class LoginRequestDto(
    val usernameOrEmail: String,
    val password: String,
)

data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
)

data class AuthResponseDto(
    val success: Boolean,
    val message: String,
    val token: String?,
    val username: String?,
    val email: String?,
)
