package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.auth.AuthResponseDto
import com.example.mobileproject.data.model.auth.LoginRequestDto
import com.example.mobileproject.data.model.auth.RegisterRequestDto
import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.repository.AuthRepository
import com.google.gson.Gson
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
) : AuthRepository {

    override suspend fun login(usernameOrEmail: String, password: String): AuthSession {
        val response = apiService.login(
            LoginRequestDto(
                usernameOrEmail = usernameOrEmail,
                password = password,
            )
        )
        return response.toAuthSessionOrThrow(gson, defaultFailureMessage = "Dang nhap that bai")
    }

    override suspend fun register(username: String, email: String, password: String): AuthSession {
        val response = apiService.register(
            RegisterRequestDto(
                username = username,
                email = email,
                password = password,
            )
        )
        return response.toAuthSessionOrThrow(gson, defaultFailureMessage = "Dang ky that bai")
    }
}

private fun Response<AuthResponseDto>.toAuthSessionOrThrow(
    gson: Gson,
    defaultFailureMessage: String,
): AuthSession {
    val bodyOrError = body() ?: parseErrorBody(gson)
    val message = bodyOrError?.message?.ifBlank { defaultFailureMessage } ?: defaultFailureMessage

    if (!isSuccessful || bodyOrError == null || !bodyOrError.success) {
        throw IllegalStateException(message)
    }

    val token = bodyOrError.token ?: throw IllegalStateException("$message: missing token")
    val username = bodyOrError.username ?: throw IllegalStateException("$message: missing username")
    val email = bodyOrError.email ?: throw IllegalStateException("$message: missing email")

    return AuthSession(
        token = token,
        username = username,
        email = email,
    )
}

private fun Response<AuthResponseDto>.parseErrorBody(gson: Gson): AuthResponseDto? {
    return runCatching {
        errorBody()?.charStream()?.use { reader ->
            gson.fromJson(reader, AuthResponseDto::class.java)
        }
    }.getOrNull()
}
