package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.ProductDto
import com.example.mobileproject.data.model.auth.AuthResponseDto
import com.example.mobileproject.data.model.auth.LoginRequestDto
import com.example.mobileproject.data.model.auth.RegisterRequestDto
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AuthRepositoryImplTest {

    @Test
    fun `login returns mapped auth session when api returns success`() = runBlocking {
        val apiService = object : ApiService {
            override suspend fun getProducts(): List<ProductDto> = emptyList()

            override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
                return Response.success(
                    AuthResponseDto(
                        success = true,
                        message = "Login successful",
                        token = "token-123",
                        username = "alice",
                        email = "alice@example.com",
                    )
                )
            }

            override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
                return Response.success(AuthResponseDto(false, "unused", null, null, null))
            }
        }

        val repository = AuthRepositoryImpl(apiService, Gson())

        val result = repository.login("alice@example.com", "secret")

        assertEquals("token-123", result.token)
        assertEquals("alice", result.username)
        assertEquals("alice@example.com", result.email)
    }

    @Test
    fun `login throws message from api response when login fails`() = runBlocking {
        val apiService = object : ApiService {
            override suspend fun getProducts(): List<ProductDto> = emptyList()

            override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
                return Response.success(
                    AuthResponseDto(
                        success = false,
                        message = "Invalid credentials",
                        token = null,
                        username = null,
                        email = null,
                    )
                )
            }

            override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
                return Response.success(AuthResponseDto(false, "unused", null, null, null))
            }
        }

        val repository = AuthRepositoryImpl(apiService, Gson())

        val exception = runCatching { repository.login("alice@example.com", "wrong") }
            .exceptionOrNull()

        assertTrue(exception is IllegalStateException)
        assertEquals("Invalid credentials", exception?.message)
    }

    @Test
    fun `register throws when success response is missing token`() = runBlocking {
        val apiService = object : ApiService {
            override suspend fun getProducts(): List<ProductDto> = emptyList()

            override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
                return Response.success(AuthResponseDto(false, "unused", null, null, null))
            }

            override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
                return Response.success(
                    AuthResponseDto(
                        success = true,
                        message = "Registered",
                        token = null,
                        username = "bob",
                        email = "bob@example.com",
                    )
                )
            }
        }

        val repository = AuthRepositoryImpl(apiService, Gson())

        val exception = runCatching { repository.register("bob", "bob@example.com", "secret") }
            .exceptionOrNull()

        assertTrue(exception is IllegalStateException)
        assertEquals("Registered: missing token", exception?.message)
    }
}
