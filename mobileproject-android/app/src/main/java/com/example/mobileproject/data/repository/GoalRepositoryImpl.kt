package com.example.mobileproject.data.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.GoalApiService
import com.example.mobileproject.data.model.goal.*
import com.example.mobileproject.domain.entity.GoalContributionResult
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.SavingGoal
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

    override fun getGoals(coupleId: String): Flow<Resource<List<SavingGoal>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getGoalsByCouple(getAuthHeader(), coupleId)
            if (response.isSuccessful) {
                val goals = response.body()?.map { dto ->
                    SavingGoal(
                        id = dto.id,
                        coupleId = dto.coupleId,
                        name = dto.name,
                        category = dto.category ?: "General",
                        targetAmount = dto.targetAmount,
                        currentAmount = dto.currentAmount,
                        status = GoalStatus.fromString(dto.status),
                        deadline = dto.deadline,
                        createdAt = dto.createdAt
                    )
                } ?: emptyList()
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
        targetAmount: Long,
        deadline: String?
    ): Flow<Resource<SavingGoal>> = flow {
        emit(Resource.Loading)
        try {
            val request = CreateGoalRequest(coupleId, name, category, targetAmount, deadline)
            val response = apiService.createGoal(getAuthHeader(), request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                val goal = SavingGoal(
                    id = body.id ?: body.goalId ?: "",
                    coupleId = coupleId,
                    name = body.name ?: name,
                    category = body.category ?: category,
                    targetAmount = body.targetAmount ?: targetAmount,
                    currentAmount = body.currentAmount ?: 0L,
                    status = GoalStatus.fromString(body.status),
                    deadline = body.deadline,
                    createdAt = body.createdAt
                )
                emit(Resource.Success(goal))
            } else {
                emit(Resource.Error(Exception(body?.message ?: "Failed to create goal")))
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
            val request = ContributeFromWalletRequest(amount, note)
            val response = apiService.contributeFromWallet(getAuthHeader(), goalId, request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(GoalContributionResult(
                    success = true,
                    message = body.message ?: "Success",
                    goalId = body.goalId ?: goalId,
                    contributionId = body.contributionId ?: "",
                    amount = body.amount ?: amount,
                    currentAmount = body.currentAmount ?: 0L,
                    walletBalance = body.walletBalance,
                    timestamp = body.timestamp ?: ""
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
            val request = ContributeDirectRequest(amount, contributorId, note)
            val response = apiService.contributeDirect(getAuthHeader(), goalId, request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(GoalContributionResult(
                    success = true,
                    message = body.message ?: "Success",
                    goalId = body.goalId ?: goalId,
                    contributionId = body.contributionId ?: "",
                    amount = body.amount ?: amount,
                    currentAmount = body.currentAmount ?: 0L,
                    walletBalance = body.walletBalance,
                    timestamp = body.timestamp ?: ""
                )))
            } else {
                emit(Resource.Error(Exception(body?.message ?: "Contribution failed")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }
}
