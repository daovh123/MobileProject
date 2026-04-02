package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.AuthSession

interface AuthRepository {

    suspend fun login(usernameOrEmail: String, password: String): AuthSession

    suspend fun register(username: String, email: String, password: String): AuthSession

    suspend fun logout(token: String)
}
