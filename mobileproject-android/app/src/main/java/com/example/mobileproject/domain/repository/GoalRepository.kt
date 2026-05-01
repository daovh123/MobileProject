package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.GoalContributionResult
import com.example.mobileproject.domain.entity.SavingGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getGoals(coupleId: String): Flow<Resource<List<SavingGoal>>>
    
    fun createGoal(
        coupleId: String,
        name: String,
        category: String,
        targetAmount: Long,
        deadline: String?
    ): Flow<Resource<SavingGoal>>

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
}
