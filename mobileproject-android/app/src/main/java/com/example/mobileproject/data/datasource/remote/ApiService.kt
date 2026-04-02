package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.auth.AuthResponseDto
import com.example.mobileproject.data.model.auth.LoginRequestDto
import com.example.mobileproject.data.model.auth.LogoutResponseDto
import com.example.mobileproject.data.model.auth.RegisterRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestActionResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestCreateRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestDecisionRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleStatusResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileUpsertRequestDto
import com.example.mobileproject.data.model.ProductDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.Response

interface ApiService {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("api/auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String,
    ): Response<LogoutResponseDto>

    @PUT("api/auth/profile")
    suspend fun upsertProfile(
        @Header("Authorization") authorization: String,
        @Body request: ProfileUpsertRequestDto,
    ): Response<ProfileResponseDto>

    @GET("api/auth/couple/status")
    suspend fun getCoupleStatus(
        @Header("Authorization") authorization: String,
    ): Response<CoupleStatusResponseDto>

    @POST("api/auth/couple/requests")
    suspend fun sendCoupleRequest(
        @Header("Authorization") authorization: String,
        @Body request: CoupleRequestCreateRequestDto,
    ): Response<CoupleRequestActionResponseDto>

    @POST("api/auth/couple/requests/{requestId}/decision")
    suspend fun decideCoupleRequest(
        @Header("Authorization") authorization: String,
        @Path("requestId") requestId: String,
        @Body request: CoupleRequestDecisionRequestDto,
    ): Response<CoupleRequestActionResponseDto>
}
