package com.example.mobileproject.data.repository

import android.util.Log
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.GoalApiService
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.data.model.goal.ContributeDirectRequestDto
import com.example.mobileproject.data.model.goal.ContributeFromWalletRequestDto
import com.example.mobileproject.data.model.goal.ContributionResponseDto
import com.example.mobileproject.data.model.goal.CreateGoalRequestDto
import com.example.mobileproject.data.model.goal.GoalTaskDto
import com.example.mobileproject.domain.entity.Goal
import com.example.mobileproject.domain.entity.GoalContributionResult
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.GoalTask
import com.example.mobileproject.domain.repository.GoalRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val apiService: GoalApiService,
    private val authSessionStore: AuthSessionStore,
    private val gson: Gson,
) : GoalRepository {

    companion object {
        private const val TAG = "GoalRepository"
    }

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
            val body = response.body()
            if (response.isSuccessful && body != null && body.success != false) {
                emit(Resource.Success(body.toDomain()))
            } else {
                val errorMessage = body?.message ?: response.parseErrorMessage(gson) ?: "Failed to create goal"
                Log.e(TAG, "createGoal failed: code=${response.code()} message=$errorMessage body=$body")
                emit(Resource.Error(IllegalStateException(errorMessage)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "createGoal exception", e)
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
                val errorMessage = response.body()?.message ?: response.parseErrorMessage(gson) ?: "Toggle task failed"
                Log.e(TAG, "toggleTask failed: code=${response.code()} message=$errorMessage")
                emit(Resource.Error(Exception(errorMessage)))
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
                emit(Resource.Success(body.toGoalContributionResult(goalId, amount)))
            } else {
                val errorMessage = body?.message ?: response.parseErrorMessage(gson) ?: "Contribution failed"
                Log.e(TAG, "contributeFromWallet failed: code=${response.code()} message=$errorMessage")
                emit(Resource.Error(Exception(errorMessage)))
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
                emit(Resource.Success(body.toGoalContributionResult(goalId, amount)))
            } else {
                val errorMessage = body?.message ?: response.parseErrorMessage(gson) ?: "Contribution failed"
                Log.e(TAG, "contributeDirect failed: code=${response.code()} message=$errorMessage")
                emit(Resource.Error(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }

    override fun withdrawToWallet(goalId: String): Flow<Resource<GoalContributionResult>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.withdrawToWallet(getAuthHeader(), goalId)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(body.toGoalContributionResult(goalId, body.amount ?: 0L)))
            } else {
                val errorMessage = body?.message ?: response.parseErrorMessage(gson) ?: "Withdraw failed"
                Log.e(TAG, "withdrawToWallet failed: code=${response.code()} message=$errorMessage")
                emit(Resource.Error(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }
}

private fun ContributionResponseDto.toGoalContributionResult(
    fallbackGoalId: String,
    fallbackAmount: Long,
): GoalContributionResult {
    return GoalContributionResult(
        success = true,
        message = message ?: "Success",
        goalId = goalId ?: fallbackGoalId,
        contributionId = contributionId ?: "",
        amount = amount ?: fallbackAmount,
        currentAmount = currentGoalAmount ?: 0L,
        walletBalance = currentWalletBalance,
        goalStatus = GoalStatus.fromString(goalStatus),
        withdrawnAmount = withdrawnAmount ?: 0L,
        timestamp = "",
    )
}

private fun Response<*>.parseErrorMessage(gson: Gson): String? {
    return runCatching {
        errorBody()?.charStream()?.use { gson.fromJson(it, GoalErrorDto::class.java).message }
    }.getOrNull()?.takeIf { !it.isNullOrBlank() }
}

private data class GoalErrorDto(
    val message: String? = null,
)
