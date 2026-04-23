package com.example.mobileproject.data.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.goal.ContributeRequestDto
import com.example.mobileproject.data.model.goal.CreateGoalRequestDto
import com.example.mobileproject.domain.entity.ContributeResponse
import com.example.mobileproject.domain.entity.GoalResponse
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.repository.GoalRepository
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : GoalRepository {

    override suspend fun createGoal(
        coupleId: String,
        name: String,
        targetAmount: Long,
        deadline: String?
    ): Resource<GoalResponse> {
        return try {
            val response = apiService.createGoal(
                CreateGoalRequestDto(
                    coupleId = coupleId,
                    name = name,
                    targetAmount = targetAmount,
                    deadline = deadline
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Resource.Success(
                    GoalResponse(
                        success = body.success,
                        message = body.message,
                        goalId = body.goalId,
                        name = body.name,
                        targetAmount = body.targetAmount,
                        currentAmount = body.currentAmount,
                        status = body.status?.toGoalStatus(),
                        deadline = body.deadline,
                        createdAt = body.createdAt
                    )
                )
            } else {
                Resource.Error(Exception(response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun getGoalsByCouple(coupleId: String): Resource<List<SavingGoal>> {
        return try {
            val response = apiService.getGoalsByCouple(coupleId)
            if (response.isSuccessful && response.body() != null) {
                val goals = response.body()!!.map { dto ->
                    SavingGoal(
                        id = dto.id,
                        coupleId = dto.coupleId,
                        name = dto.name,
                        targetAmount = dto.targetAmount,
                        currentAmount = dto.currentAmount,
                        status = dto.status.toGoalStatus(),
                        deadline = dto.deadline,
                        createdAt = dto.createdAt
                    )
                }
                Resource.Success(goals)
            } else {
                Resource.Error(Exception(response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun contributeFromWallet(
        goalId: String,
        amount: Long,
        contributorId: String,
        note: String?
    ): Resource<ContributeResponse> {
        return try {
            val response = apiService.contributeFromWallet(
                goalId,
                ContributeRequestDto(amount, contributorId, note)
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Resource.Success(
                    ContributeResponse(
                        success = body.success,
                        message = body.message,
                        contributionId = body.contributionId,
                        goalId = body.goalId,
                        amount = body.amount,
                        currentAmount = body.currentAmount,
                        walletBalance = body.walletBalance,
                        timestamp = body.timestamp
                    )
                )
            } else {
                Resource.Error(Exception(response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun contributeToGoal(
        goalId: String,
        amount: Long,
        contributorId: String,
        note: String?
    ): Resource<ContributeResponse> {
        return try {
            val response = apiService.contributeToGoal(
                goalId,
                ContributeRequestDto(amount, contributorId, note)
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Resource.Success(
                    ContributeResponse(
                        success = body.success,
                        message = body.message,
                        contributionId = body.contributionId,
                        goalId = body.goalId,
                        amount = body.amount,
                        currentAmount = body.currentAmount,
                        walletBalance = body.walletBalance,
                        timestamp = body.timestamp
                    )
                )
            } else {
                Resource.Error(Exception(response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    private fun String.toGoalStatus(): GoalStatus {
        return when (this.uppercase()) {
            "IN_PROGRESS" -> GoalStatus.IN_PROGRESS
            "ACHIEVED" -> GoalStatus.ACHIEVED
            "FAILED" -> GoalStatus.FAILED
            else -> GoalStatus.IN_PROGRESS
        }
    }
}