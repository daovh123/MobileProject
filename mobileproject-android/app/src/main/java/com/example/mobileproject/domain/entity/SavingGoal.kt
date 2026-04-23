package com.example.mobileproject.domain.entity

data class SavingGoal(
    val id: String,
    val coupleId: String,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val status: GoalStatus,
    val deadline: String?,
    val createdAt: String?
)

enum class GoalStatus {
    IN_PROGRESS,
    ACHIEVED,
    FAILED
}

data class GoalContribution(
    val id: String,
    val goalId: String,
    val amount: Long,
    val contributorId: String,
    val note: String?,
    val timestamp: String
)

data class GoalResponse(
    val success: Boolean,
    val message: String,
    val goalId: String?,
    val name: String?,
    val targetAmount: Long?,
    val currentAmount: Long?,
    val status: GoalStatus?,
    val deadline: String?,
    val createdAt: String?
)

data class ContributeResponse(
    val success: Boolean,
    val message: String,
    val contributionId: String?,
    val goalId: String?,
    val amount: Long?,
    val currentAmount: Long?,
    val walletBalance: Long?,
    val timestamp: String?
)

data class CreateGoalRequest(
    val coupleId: String,
    val name: String,
    val targetAmount: Long,
    val deadline: String?
)

data class ContributeRequest(
    val amount: Long,
    val contributorId: String,
    val note: String?
)