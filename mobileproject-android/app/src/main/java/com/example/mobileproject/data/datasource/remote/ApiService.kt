package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.analytics.CategoryBreakdownDto
import com.example.mobileproject.data.model.analytics.SpendingTrendDto
import com.example.mobileproject.data.model.auth.AuthResponseDto
import com.example.mobileproject.data.model.auth.LoginRequestDto
import com.example.mobileproject.data.model.auth.LogoutResponseDto
import com.example.mobileproject.data.model.auth.RegisterRequestDto
import com.example.mobileproject.data.model.favorite.FavoriteListResponseDto
import com.example.mobileproject.data.model.favorite.FavoriteToggleResponseDto
import com.example.mobileproject.data.model.favorite.HistoryListResponseDto
import com.example.mobileproject.data.model.goal.ContributeDirectRequestDto
import com.example.mobileproject.data.model.goal.ContributeFromWalletRequestDto
import com.example.mobileproject.data.model.goal.ContributionResponseDto
import com.example.mobileproject.data.model.goal.CreateGoalRequestDto
import com.example.mobileproject.data.model.goal.GoalDto
import com.example.mobileproject.data.model.map.MapLastLocationsResponseDto
import com.example.mobileproject.data.model.notification.FcmTokenRequestDto
import com.example.mobileproject.data.model.onboarding.AvatarFrameDto
import com.example.mobileproject.data.model.onboarding.AvatarFrameRequestDto
import com.example.mobileproject.data.model.onboarding.AvatarUploadResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestActionResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestCreateRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestDecisionRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleStatusResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileUpsertRequestDto
import com.example.mobileproject.data.model.moment.MomentDto
import com.example.mobileproject.data.model.moment.MomentRequestDto
import com.example.mobileproject.data.model.moment.MomentResponseDto
import com.example.mobileproject.data.model.ProductDto
import okhttp3.MultipartBody
import com.example.mobileproject.data.model.remote.WalletResponse
import com.example.mobileproject.data.model.transaction.IncomeRequestDto
import com.example.mobileproject.data.model.transaction.TransactionDto
import com.example.mobileproject.data.model.transaction.TransactionRequestDto
import com.example.mobileproject.data.model.transaction.TransactionResponseDto
import retrofit2.http.*
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

    @GET("api/auth/profile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String,
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

    @POST("api/favorites/toggle")
    suspend fun toggleFavorite(
        @Header("Authorization") authorization: String,
        @Query("placeId") placeId: String,
    ): Response<FavoriteToggleResponseDto>

    @GET("api/favorites")
    suspend fun getFavorites(
        @Header("Authorization") authorization: String,
    ): Response<FavoriteListResponseDto>

    @GET("api/favorites/check")
    suspend fun checkFavorite(
        @Header("Authorization") authorization: String,
        @Query("placeId") placeId: String,
    ): Response<FavoriteToggleResponseDto>

    @POST("api/history/view")
    suspend fun recordHistory(
        @Header("Authorization") authorization: String,
        @Query("placeId") placeId: String,
    ): Response<Unit>

    @GET("api/history")
    suspend fun getHistory(
        @Header("Authorization") authorization: String,
    ): Response<HistoryListResponseDto>

    @GET("api/auth/map/last")
    suspend fun getMapLastLocations(
        @Header("Authorization") authorization: String,
    ): Response<MapLastLocationsResponseDto>

    @DELETE("api/history")
    suspend fun clearHistory(
        @Header("Authorization") authorization: String,
    ): Response<Unit>

    // Notification APIs
    @POST("api/notifications/fcm-token")
    suspend fun registerFcmToken(
        @Header("Authorization") authorization: String,
        @Body request: FcmTokenRequestDto,
    ): Response<Unit>

    // Wallet APIs
    @GET("api/v1/wallet/{coupleId}")
    suspend fun getWallet(
        @Header("Authorization") authorization: String,
        @Path("coupleId", encoded = true) coupleId: String
    ): Response<WalletResponse>

    // Transaction APIs
    @POST("api/v1/transactions")
    suspend fun createTransaction(
        @Header("Authorization") authorization: String,
        @Body request: TransactionRequestDto,
    ): Response<TransactionResponseDto>

    @POST("api/v1/transactions/income")
    suspend fun processIncome(
        @Header("Authorization") authorization: String,
        @Body request: IncomeRequestDto,
    ): Response<TransactionResponseDto>

    @GET("api/v1/transactions")
    suspend fun getTransactions(
        @Header("Authorization") authorization: String,
        @Query("coupleId") coupleId: String,
    ): Response<List<TransactionDto>>

    // Goal APIs
    @POST("api/v1/goals")
    suspend fun createGoal(
        @Header("Authorization") authorization: String,
        @Body request: CreateGoalRequestDto,
    ): Response<GoalDto>

    @GET("api/v1/goals/couple/{coupleId}")
    suspend fun getGoalsByCouple(
        @Header("Authorization") authorization: String,
        @Path("coupleId") coupleId: String,
    ): Response<List<GoalDto>>

    @POST("api/v1/goals/{goalId}/contribute-from-wallet")
    suspend fun contributeFromWallet(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
        @Body request: ContributeFromWalletRequestDto,
    ): Response<ContributionResponseDto>

    @POST("api/v1/goals/{goalId}/contribute")
    suspend fun contributeToGoal(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
        @Body request: ContributeDirectRequestDto,
    ): Response<ContributionResponseDto>

    // Avatar APIs
    @Multipart
    @POST("api/auth/profile/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
    ): Response<AvatarUploadResponseDto>

    @GET("api/auth/profile/frames")
    suspend fun getAvatarFrames(
        @Header("Authorization") authorization: String,
    ): Response<List<AvatarFrameDto>>

    @PUT("api/auth/profile/frame")
    suspend fun setAvatarFrame(
        @Header("Authorization") authorization: String,
        @Body request: AvatarFrameRequestDto,
    ): Response<ProfileResponseDto>

    // Analytics APIs
    @GET("api/v1/analytics/expense-by-category")
    suspend fun getCategoryBreakdown(
        @Header("Authorization") authorization: String,
        @Query("coupleId") coupleId: String,
        @Query("month") month: Int,
        @Query("year") year: Int,
    ): Response<List<CategoryBreakdownDto>>

    @GET("api/v1/analytics/monthly-trend")
    suspend fun getMonthlyTrend(
        @Header("Authorization") authorization: String,
        @Query("coupleId") coupleId: String,
        @Query("year") year: Int
    ): Response<List<SpendingTrendDto>>

    @GET("api/v1/moments")
    suspend fun getMoments(
        @Query("coupleId") coupleId: String,
        @Header("Authorization") authorization: String,
    ): Response<List<MomentDto>>

    @POST("api/v1/moments")
    suspend fun createMoment(
        @Body request: MomentRequestDto,
        @Header("Authorization") authorization: String,
    ): Response<MomentResponseDto>
}
