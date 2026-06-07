package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.Goal
import com.example.mobileproject.domain.entity.GoalContributionResult
import com.example.mobileproject.domain.entity.SavingGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getGoals(coupleId: String): Flow<Resource<List<Goal>>>
    
    fun createGoal(
        coupleId: String,
        name: String,
        category: String,
        type: String,
        targetAmount: Long? = null,
        deadline: String? = null,
        tasks: List<com.example.mobileproject.domain.entity.GoalTask>? = null
    ): Flow<Resource<Goal>>

    fun toggleTask(goalId: String, taskId: String): Flow<Resource<Double>>

    fun contributeFromWallet(
        goalId: String,
        amount: Long,
        note: String?
    ): Flow<Resource<GoalContributionResult>>

    fun contributeDirect(
        goalId: String,
        amount: Long,
        contributorId: String,
        note: String?
    ): Flow<Resource<GoalContributionResult>>

    fun withdrawToWallet(
        goalId: String,
    ): Flow<Resource<GoalContributionResult>>
}
