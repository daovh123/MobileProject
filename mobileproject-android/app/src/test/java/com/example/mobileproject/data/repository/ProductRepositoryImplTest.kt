package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.datasource.remote.RemoteDataSource
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
import com.example.mobileproject.data.model.ProductDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class ProductRepositoryImplTest {

    @Test
    fun `getProducts maps dto to domain`() = runBlocking {
        val apiService = object : ApiService {
            override suspend fun getProducts(): List<ProductDto> = listOf(
                ProductDto(id = "1", name = "Coffee"),
                ProductDto(id = "2", name = "Tea"),
            )

            override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
                return Response.success(AuthResponseDto(false, "unused", null, null, null))
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

        val remoteDataSource = RemoteDataSource(apiService)
        val repository = ProductRepositoryImpl(remoteDataSource)

        val result = repository.getProducts()

        assertEquals(listOf("1", "2"), result.map { it.id })
        assertEquals(listOf("Coffee", "Tea"), result.map { it.name })
    }
}
