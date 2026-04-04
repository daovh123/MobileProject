package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.ProductDto
import com.example.mobileproject.data.model.auth.AuthResponseDto
import com.example.mobileproject.data.model.auth.LoginRequestDto
import com.example.mobileproject.data.model.auth.LogoutResponseDto
import com.example.mobileproject.data.model.auth.RegisterRequestDto
import com.example.mobileproject.data.model.favorite.FavoriteListResponseDto
import com.example.mobileproject.data.model.favorite.FavoriteToggleResponseDto
import com.example.mobileproject.data.model.favorite.HistoryListResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestActionResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestCreateRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestDecisionRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleStatusResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileUpsertRequestDto
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
                        profileCompleted = true,
                        coupleConnected = false,
                    )
                )
            }

            override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
                return Response.success(AuthResponseDto(false, "unused", null, null, null))
            }

            override suspend fun logout(authorization: String): Response<LogoutResponseDto> {
                return Response.success(LogoutResponseDto(false, "unused"))
            }

            override suspend fun upsertProfile(
                authorization: String,
                request: ProfileUpsertRequestDto,
            ): Response<ProfileResponseDto> {
                return Response.success(ProfileResponseDto(false, "unused", null, null, null, null, null, false, false))
            }

            override suspend fun getCoupleStatus(authorization: String): Response<CoupleStatusResponseDto> {
                return Response.success(
                    CoupleStatusResponseDto(
                        success = false,
                        message = "unused",
                        profileCompleted = false,
                        paired = false,
                        partnerUsername = null,
                        myCoupleCode = null,
                        incomingRequestId = null,
                        incomingRequesterUsername = null,
                        incomingRequesterDisplayName = null,
                        incomingCreatedAt = null,
                        outgoingRequestId = null,
                        outgoingRecipientUsername = null,
                        outgoingStatus = null,
                        outgoingUpdatedAt = null,
                    )
                )
            }

            override suspend fun sendCoupleRequest(
                authorization: String,
                request: CoupleRequestCreateRequestDto,
            ): Response<CoupleRequestActionResponseDto> {
                return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
            }

            override suspend fun decideCoupleRequest(
                authorization: String,
                requestId: String,
                request: CoupleRequestDecisionRequestDto,
            ): Response<CoupleRequestActionResponseDto> {
                return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
            }

            override suspend fun toggleFavorite(
                authorization: String,
                placeId: String,
            ): Response<FavoriteToggleResponseDto> {
                return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
            }

            override suspend fun getFavorites(authorization: String): Response<FavoriteListResponseDto> {
                return Response.success(FavoriteListResponseDto(false, "unused", emptyList()))
            }

            override suspend fun checkFavorite(
                authorization: String,
                placeId: String,
            ): Response<FavoriteToggleResponseDto> {
                return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
            }

            override suspend fun recordHistory(
                authorization: String,
                placeId: String,
            ): Response<Unit> {
                return Response.success(Unit)
            }

            override suspend fun getHistory(authorization: String): Response<HistoryListResponseDto> {
                return Response.success(HistoryListResponseDto(false, "unused", emptyList()))
            }

            override suspend fun clearHistory(authorization: String): Response<Unit> {
                return Response.success(Unit)
            }
        }

        val repository = AuthRepositoryImpl(apiService, Gson())

        val result = repository.login("alice@example.com", "secret")

        assertEquals("token-123", result.token)
        assertEquals("alice", result.username)
        assertEquals("alice@example.com", result.email)
        assertTrue(result.profileCompleted)
        assertTrue(!result.coupleConnected)
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
                        profileCompleted = false,
                        coupleConnected = false,
                    )
                )
            }

            override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
                return Response.success(AuthResponseDto(false, "unused", null, null, null))
            }

            override suspend fun logout(authorization: String): Response<LogoutResponseDto> {
                return Response.success(LogoutResponseDto(false, "unused"))
            }

            override suspend fun upsertProfile(
                authorization: String,
                request: ProfileUpsertRequestDto,
            ): Response<ProfileResponseDto> {
                return Response.success(ProfileResponseDto(false, "unused", null, null, null, null, null, false, false))
            }

            override suspend fun getCoupleStatus(authorization: String): Response<CoupleStatusResponseDto> {
                return Response.success(
                    CoupleStatusResponseDto(
                        success = false,
                        message = "unused",
                        profileCompleted = false,
                        paired = false,
                        partnerUsername = null,
                        myCoupleCode = null,
                        incomingRequestId = null,
                        incomingRequesterUsername = null,
                        incomingRequesterDisplayName = null,
                        incomingCreatedAt = null,
                        outgoingRequestId = null,
                        outgoingRecipientUsername = null,
                        outgoingStatus = null,
                        outgoingUpdatedAt = null,
                    )
                )
            }

            override suspend fun sendCoupleRequest(
                authorization: String,
                request: CoupleRequestCreateRequestDto,
            ): Response<CoupleRequestActionResponseDto> {
                return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
            }

            override suspend fun decideCoupleRequest(
                authorization: String,
                requestId: String,
                request: CoupleRequestDecisionRequestDto,
            ): Response<CoupleRequestActionResponseDto> {
                return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
            }

            override suspend fun toggleFavorite(
                authorization: String,
                placeId: String,
            ): Response<FavoriteToggleResponseDto> {
                return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
            }

            override suspend fun getFavorites(authorization: String): Response<FavoriteListResponseDto> {
                return Response.success(FavoriteListResponseDto(false, "unused", emptyList()))
            }

            override suspend fun checkFavorite(
                authorization: String,
                placeId: String,
            ): Response<FavoriteToggleResponseDto> {
                return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
            }

            override suspend fun recordHistory(
                authorization: String,
                placeId: String,
            ): Response<Unit> {
                return Response.success(Unit)
            }

            override suspend fun getHistory(authorization: String): Response<HistoryListResponseDto> {
                return Response.success(HistoryListResponseDto(false, "unused", emptyList()))
            }

            override suspend fun clearHistory(authorization: String): Response<Unit> {
                return Response.success(Unit)
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
                        profileCompleted = false,
                        coupleConnected = false,
                    )
                )
            }

            override suspend fun logout(authorization: String): Response<LogoutResponseDto> {
                return Response.success(LogoutResponseDto(false, "unused"))
            }

            override suspend fun upsertProfile(
                authorization: String,
                request: ProfileUpsertRequestDto,
            ): Response<ProfileResponseDto> {
                return Response.success(ProfileResponseDto(false, "unused", null, null, null, null, null, false, false))
            }

            override suspend fun getCoupleStatus(authorization: String): Response<CoupleStatusResponseDto> {
                return Response.success(
                    CoupleStatusResponseDto(
                        success = false,
                        message = "unused",
                        profileCompleted = false,
                        paired = false,
                        partnerUsername = null,
                        myCoupleCode = null,
                        incomingRequestId = null,
                        incomingRequesterUsername = null,
                        incomingRequesterDisplayName = null,
                        incomingCreatedAt = null,
                        outgoingRequestId = null,
                        outgoingRecipientUsername = null,
                        outgoingStatus = null,
                        outgoingUpdatedAt = null,
                    )
                )
            }

            override suspend fun sendCoupleRequest(
                authorization: String,
                request: CoupleRequestCreateRequestDto,
            ): Response<CoupleRequestActionResponseDto> {
                return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
            }

            override suspend fun decideCoupleRequest(
                authorization: String,
                requestId: String,
                request: CoupleRequestDecisionRequestDto,
            ): Response<CoupleRequestActionResponseDto> {
                return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
            }

            override suspend fun toggleFavorite(
                authorization: String,
                placeId: String,
            ): Response<FavoriteToggleResponseDto> {
                return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
            }

            override suspend fun getFavorites(authorization: String): Response<FavoriteListResponseDto> {
                return Response.success(FavoriteListResponseDto(false, "unused", emptyList()))
            }

            override suspend fun checkFavorite(
                authorization: String,
                placeId: String,
            ): Response<FavoriteToggleResponseDto> {
                return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
            }

            override suspend fun recordHistory(
                authorization: String,
                placeId: String,
            ): Response<Unit> {
                return Response.success(Unit)
            }

            override suspend fun getHistory(authorization: String): Response<HistoryListResponseDto> {
                return Response.success(HistoryListResponseDto(false, "unused", emptyList()))
            }

            override suspend fun clearHistory(authorization: String): Response<Unit> {
                return Response.success(Unit)
            }
        }

        val repository = AuthRepositoryImpl(apiService, Gson())

        val exception = runCatching { repository.register("bob", "bob@example.com", "secret") }
            .exceptionOrNull()

        assertTrue(exception is IllegalStateException)
        assertEquals("Registered: missing token", exception?.message)
    }

    @Test
    fun `logout succeeds when api returns success true`() = runBlocking {
        val apiService = object : ApiService {
            override suspend fun getProducts(): List<ProductDto> = emptyList()

            override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
                return Response.success(AuthResponseDto(false, "unused", null, null, null))
            }

            override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
                return Response.success(AuthResponseDto(false, "unused", null, null, null))
            }

            override suspend fun logout(authorization: String): Response<LogoutResponseDto> {
                return Response.success(LogoutResponseDto(success = true, message = "Logout successful"))
            }

            override suspend fun upsertProfile(
                authorization: String,
                request: ProfileUpsertRequestDto,
            ): Response<ProfileResponseDto> {
                return Response.success(ProfileResponseDto(false, "unused", null, null, null, null, null, false, false))
            }

            override suspend fun getCoupleStatus(authorization: String): Response<CoupleStatusResponseDto> {
                return Response.success(
                    CoupleStatusResponseDto(
                        success = false,
                        message = "unused",
                        profileCompleted = false,
                        paired = false,
                        partnerUsername = null,
                        myCoupleCode = null,
                        incomingRequestId = null,
                        incomingRequesterUsername = null,
                        incomingRequesterDisplayName = null,
                        incomingCreatedAt = null,
                        outgoingRequestId = null,
                        outgoingRecipientUsername = null,
                        outgoingStatus = null,
                        outgoingUpdatedAt = null,
                    )
                )
            }

            override suspend fun sendCoupleRequest(
                authorization: String,
                request: CoupleRequestCreateRequestDto,
            ): Response<CoupleRequestActionResponseDto> {
                return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
            }

            override suspend fun decideCoupleRequest(
                authorization: String,
                requestId: String,
                request: CoupleRequestDecisionRequestDto,
            ): Response<CoupleRequestActionResponseDto> {
                return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
            }

            override suspend fun toggleFavorite(
                authorization: String,
                placeId: String,
            ): Response<FavoriteToggleResponseDto> {
                return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
            }

            override suspend fun getFavorites(authorization: String): Response<FavoriteListResponseDto> {
                return Response.success(FavoriteListResponseDto(false, "unused", emptyList()))
            }

            override suspend fun checkFavorite(
                authorization: String,
                placeId: String,
            ): Response<FavoriteToggleResponseDto> {
                return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
            }

            override suspend fun recordHistory(
                authorization: String,
                placeId: String,
            ): Response<Unit> {
                return Response.success(Unit)
            }

            override suspend fun getHistory(authorization: String): Response<HistoryListResponseDto> {
                return Response.success(HistoryListResponseDto(false, "unused", emptyList()))
            }

            override suspend fun clearHistory(authorization: String): Response<Unit> {
                return Response.success(Unit)
            }
        }

        val repository = AuthRepositoryImpl(apiService, Gson())

        repository.logout("token-logout")
    }

    @Test
    fun `logout throws message when api returns failure`() = runBlocking {
        val apiService = object : ApiService {
            override suspend fun getProducts(): List<ProductDto> = emptyList()

            override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
                return Response.success(AuthResponseDto(false, "unused", null, null, null))
            }

            override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
                return Response.success(AuthResponseDto(false, "unused", null, null, null))
            }

            override suspend fun logout(authorization: String): Response<LogoutResponseDto> {
                return Response.success(LogoutResponseDto(success = false, message = "Invalid access token"))
            }

            override suspend fun upsertProfile(
                authorization: String,
                request: ProfileUpsertRequestDto,
            ): Response<ProfileResponseDto> {
                return Response.success(ProfileResponseDto(false, "unused", null, null, null, null, null, false, false))
            }

            override suspend fun getCoupleStatus(authorization: String): Response<CoupleStatusResponseDto> {
                return Response.success(
                    CoupleStatusResponseDto(
                        success = false,
                        message = "unused",
                        profileCompleted = false,
                        paired = false,
                        partnerUsername = null,
                        myCoupleCode = null,
                        incomingRequestId = null,
                        incomingRequesterUsername = null,
                        incomingRequesterDisplayName = null,
                        incomingCreatedAt = null,
                        outgoingRequestId = null,
                        outgoingRecipientUsername = null,
                        outgoingStatus = null,
                        outgoingUpdatedAt = null,
                    )
                )
            }

            override suspend fun sendCoupleRequest(
                authorization: String,
                request: CoupleRequestCreateRequestDto,
            ): Response<CoupleRequestActionResponseDto> {
                return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
            }

            override suspend fun decideCoupleRequest(
                authorization: String,
                requestId: String,
                request: CoupleRequestDecisionRequestDto,
            ): Response<CoupleRequestActionResponseDto> {
                return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
            }

            override suspend fun toggleFavorite(
                authorization: String,
                placeId: String,
            ): Response<FavoriteToggleResponseDto> {
                return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
            }

            override suspend fun getFavorites(authorization: String): Response<FavoriteListResponseDto> {
                return Response.success(FavoriteListResponseDto(false, "unused", emptyList()))
            }

            override suspend fun checkFavorite(
                authorization: String,
                placeId: String,
            ): Response<FavoriteToggleResponseDto> {
                return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
            }

            override suspend fun recordHistory(
                authorization: String,
                placeId: String,
            ): Response<Unit> {
                return Response.success(Unit)
            }

            override suspend fun getHistory(authorization: String): Response<HistoryListResponseDto> {
                return Response.success(HistoryListResponseDto(false, "unused", emptyList()))
            }

            override suspend fun clearHistory(authorization: String): Response<Unit> {
                return Response.success(Unit)
            }
        }

        val repository = AuthRepositoryImpl(apiService, Gson())

        val exception = runCatching { repository.logout("token-logout") }
            .exceptionOrNull()

        assertTrue(exception is IllegalStateException)
        assertEquals("Invalid access token", exception?.message)
    }
}
