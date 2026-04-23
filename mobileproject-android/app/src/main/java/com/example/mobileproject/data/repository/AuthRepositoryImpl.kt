package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.model.auth.AuthResponseDto
import com.example.mobileproject.data.model.auth.LoginRequestDto
import com.example.mobileproject.data.model.auth.LogoutResponseDto
import com.example.mobileproject.data.model.auth.RegisterRequestDto
import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.repository.AuthRepository
import com.google.gson.Gson
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val authSessionStore: AuthSessionStore? = null,
) : AuthRepository {

    override suspend fun login(usernameOrEmail: String, password: String): AuthSession {
        val response = apiService.login(
            LoginRequestDto(
                usernameOrEmail = usernameOrEmail,
                password = password,
            )
        )
        val session = response.toAuthSessionOrThrow(gson, defaultFailureMessage = "Dang nhap that bai")
        authSessionStore?.save(session)
        return session
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

    override suspend fun logout(token: String) {
        val logoutResult = runCatching {
            val response = apiService.logout(authorizationHeader(token))
            response.toLogoutSuccessOrThrow(gson, defaultFailureMessage = "Dang xuat that bai")
        }

        authSessionStore?.clear()
        logoutResult.getOrThrow()
    }
}

private fun authorizationHeader(token: String): String = "Bearer ${token.trim()}"

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
        profileCompleted = bodyOrError.profileCompleted ?: false,
        coupleConnected = bodyOrError.coupleConnected ?: false,
    )
}

private fun Response<AuthResponseDto>.parseErrorBody(gson: Gson): AuthResponseDto? {
    return runCatching {
        errorBody()?.charStream()?.use { reader ->
            gson.fromJson(reader, AuthResponseDto::class.java)
        }
    }.getOrNull()
}

private fun Response<LogoutResponseDto>.toLogoutSuccessOrThrow(
    gson: Gson,
    defaultFailureMessage: String,
) {
    val bodyOrError = body()
    val message = when {
        bodyOrError != null -> bodyOrError.message
        else -> parseErrorMessage(gson) ?: defaultFailureMessage
    }.ifBlank { defaultFailureMessage }

    if (!isSuccessful || bodyOrError == null || !bodyOrError.success) {
        throw IllegalStateException(message)
    }
}

private fun Response<*>.parseErrorMessage(gson: Gson): String? {
    return runCatching {
        errorBody()?.charStream()?.use { reader ->
            gson.fromJson(reader, MessageErrorDto::class.java).message
        }
    }.getOrNull()?.takeIf { !it.isNullOrBlank() }
}

private data class MessageErrorDto(
    val message: String? = null,
)
