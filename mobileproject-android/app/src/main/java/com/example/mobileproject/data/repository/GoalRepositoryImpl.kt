package com.example.mobileproject.data.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.GoalApiService
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.data.model.goal.ContributeDirectRequestDto
import com.example.mobileproject.data.model.goal.ContributeFromWalletRequestDto
import com.example.mobileproject.data.model.goal.CreateGoalRequestDto
import com.example.mobileproject.data.model.goal.GoalTaskDto
import com.example.mobileproject.domain.entity.Goal
import com.example.mobileproject.domain.entity.GoalContributionResult
import com.example.mobileproject.domain.entity.GoalTask
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val apiService: GoalApiService,
    private val authSessionStore: AuthSessionStore
) : GoalRepository {

    private fun getAuthHeader(): String {
        val token = authSessionStore.load()?.token ?: ""
        return "Bearer ${token.trim()}"
    }

    override fun getGoals(coupleId: String): Flow<Resource<List<Goal>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getGoalsByCouple(getAuthHeader(), coupleId)
            if (response.isSuccessful) {
                val goals = response.body()?.map { it.toDomain() } ?: emptyList()
                emit(Resource.Success(goals))
            } else {
                emit(Resource.Error(Exception("Failed to fetch goals: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }

    override fun createGoal(
        coupleId: String,
        name: String,
        category: String,
        type: String,
        targetAmount: Long?,
        deadline: String?,
        tasks: List<GoalTask>?
    ): Flow<Resource<Goal>> = flow {
        emit(Resource.Loading)
        try {
            val request = CreateGoalRequestDto(
                coupleId = coupleId,
                name = name,
                category = category,
                type = type,
                targetAmount = targetAmount,
                deadline = deadline ?: "",
                tasks = tasks?.map { GoalTaskDto(it.taskId, it.content, it.isCompleted) }
            )
            val response = apiService.createGoal(getAuthHeader(), request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
            } else {
                emit(Resource.Error(Exception("Failed to create goal")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }

    override fun toggleTask(goalId: String, taskId: String): Flow<Resource<Double>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.toggleTask(getAuthHeader(), goalId, taskId)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()?.progress ?: 0.0))
            } else {
                emit(Resource.Error(Exception(response.body()?.message ?: "Toggle task failed")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }

    override fun contributeFromWallet(
        goalId: String,
        amount: Long,
        note: String?
    ): Flow<Resource<GoalContributionResult>> = flow {
        emit(Resource.Loading)
        try {
            val request = ContributeFromWalletRequestDto(amount, note)
            val response = apiService.contributeFromWallet(getAuthHeader(), goalId, request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(GoalContributionResult(
                    success = true,
                    message = body.message ?: "Success",
                    goalId = body.goalId ?: goalId,
                    contributionId = body.contributionId ?: "",
                    amount = body.amount ?: amount,
                    currentAmount = body.currentGoalAmount ?: 0L,
                    walletBalance = body.currentWalletBalance,
                    timestamp = ""
                )))
            } else {
                emit(Resource.Error(Exception(body?.message ?: "Contribution failed")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }

    override fun contributeDirect(
        goalId: String,
        amount: Long,
        contributorId: String,
        note: String?
    ): Flow<Resource<GoalContributionResult>> = flow {
        emit(Resource.Loading)
        try {
            val request = ContributeDirectRequestDto(amount, contributorId, note)
            val response = apiService.contributeDirect(getAuthHeader(), goalId, request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(GoalContributionResult(
                    success = true,
                    message = body.message ?: "Success",
                    goalId = body.goalId ?: goalId,
                    contributionId = body.contributionId ?: "",
                    amount = body.amount ?: amount,
                    currentAmount = body.currentGoalAmount ?: 0L,
                    walletBalance = null,
                    timestamp = ""
                )))
            } else {
                emit(Resource.Error(Exception(body?.message ?: "Contribution failed")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }
}
