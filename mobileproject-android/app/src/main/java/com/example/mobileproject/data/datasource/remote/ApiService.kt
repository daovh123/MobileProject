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
import com.example.mobileproject.data.model.goal.ContributeRequestDto
import com.example.mobileproject.data.model.goal.ContributeResponseDto
import com.example.mobileproject.data.model.goal.CreateGoalRequestDto
import com.example.mobileproject.data.model.goal.GoalResponseDto
import com.example.mobileproject.data.model.goal.SavingGoalDto
import com.example.mobileproject.data.model.map.MapLastLocationsResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestActionResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestCreateRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestDecisionRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleStatusResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileUpsertRequestDto
import com.example.mobileproject.data.model.ProductDto
import com.example.mobileproject.data.model.transaction.IncomeRequestDto
import com.example.mobileproject.data.model.transaction.TransactionDto
import com.example.mobileproject.data.model.transaction.TransactionRequestDto
import com.example.mobileproject.data.model.transaction.TransactionResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query
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

    // Transaction APIs
    @POST("api/v1/transactions")
    suspend fun createTransaction(
        @Body request: TransactionRequestDto,
    ): Response<TransactionResponseDto>

    @POST("api/v1/transactions/income")
    suspend fun processIncome(
        @Body request: IncomeRequestDto,
    ): Response<TransactionResponseDto>

    @GET("api/v1/transactions")
    suspend fun getTransactions(
        @Query("coupleId") coupleId: String,
    ): Response<List<TransactionDto>>

    // Goal APIs
    @POST("api/v1/goals")
    suspend fun createGoal(
        @Body request: CreateGoalRequestDto,
    ): Response<GoalResponseDto>

    @GET("api/v1/goals/couple/{coupleId}")
    suspend fun getGoalsByCouple(
        @Path("coupleId") coupleId: String,
    ): Response<List<SavingGoalDto>>

    @POST("api/v1/goals/{goalId}/contribute-from-wallet")
    suspend fun contributeFromWallet(
        @Path("goalId") goalId: String,
        @Body request: ContributeRequestDto,
    ): Response<ContributeResponseDto>

    @POST("api/v1/goals/{goalId}/contribute")
    suspend fun contributeToGoal(
        @Path("goalId") goalId: String,
        @Body request: ContributeRequestDto,
    ): Response<ContributeResponseDto>

    // Analytics APIs
    @GET("api/v1/analytics/category-breakdown")
    suspend fun getCategoryBreakdown(
        @Query("coupleId") coupleId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): Response<List<CategoryBreakdownDto>>

    @GET("api/v1/analytics/spending-trend")
    suspend fun getSpendingTrend(
        @Query("coupleId") coupleId: String,
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): Response<List<SpendingTrendDto>>
}
