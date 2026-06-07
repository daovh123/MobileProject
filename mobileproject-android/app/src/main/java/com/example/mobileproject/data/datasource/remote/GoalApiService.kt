package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.goal.*
import retrofit2.Response
import retrofit2.http.*

interface GoalApiService {
    @POST("api/v1/goals")
    suspend fun createGoal(
        @Header("Authorization") authorization: String,
        @Body request: CreateGoalRequestDto
    ): Response<GoalDto>

    @PATCH("api/v1/goals/{goalId}/tasks/{taskId}")
    suspend fun toggleTask(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
        @Path("taskId") taskId: String
    ): Response<ToggleTaskResponseDto>

    @GET("api/v1/goals/couple/{coupleId}")
    suspend fun getGoalsByCouple(
        @Header("Authorization") authorization: String,
        @Path("coupleId") coupleId: String
    ): Response<List<GoalDto>>

    @GET("api/v1/goals/{goalId}")
    suspend fun getGoalById(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String
    ): Response<GoalDto>

    @POST("api/v1/goals/{goalId}/contribute-from-wallet")
    suspend fun contributeFromWallet(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
        @Body request: ContributeFromWalletRequestDto
    ): Response<ContributionResponseDto>

    @POST("api/v1/goals/{goalId}/contribute")
    suspend fun contributeDirect(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
        @Body request: ContributeDirectRequestDto
    ): Response<ContributionResponseDto>

    @POST("api/v1/goals/{goalId}/withdraw-to-wallet")
    suspend fun withdrawToWallet(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
    ): Response<ContributionResponseDto>
}
