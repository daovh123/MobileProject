package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.ContributeResponse
import com.example.mobileproject.domain.entity.GoalResponse
import com.example.mobileproject.domain.entity.SavingGoal

interface GoalRepository {
    suspend fun createGoal(
        coupleId: String,
        name: String,
        targetAmount: Long,
        deadline: String?
    ): Resource<GoalResponse>

    suspend fun getGoalsByCouple(coupleId: String): Resource<List<SavingGoal>>

    suspend fun contributeFromWallet(
        goalId: String,
        amount: Long,
        contributorId: String,
        note: String?
    ): Resource<ContributeResponse>

    suspend fun contributeToGoal(
        goalId: String,
        amount: Long,
        contributorId: String,
        note: String?
    ): Resource<ContributeResponse>
}