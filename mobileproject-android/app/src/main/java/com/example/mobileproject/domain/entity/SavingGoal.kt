package com.example.mobileproject.domain.entity

data class SavingGoal(
    val id: String,
    val coupleId: String,
    val name: String,
    val category: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val status: GoalStatus,
    val deadline: String?,
    val createdAt: String?
)

enum class GoalStatus {
    IN_PROGRESS,
    ACHIEVED,
    FAILED;

    companion object {
        fun fromString(status: String?): GoalStatus = when (status?.uppercase()) {
            "ACHIEVED" -> ACHIEVED
            "FAILED" -> FAILED
            else -> IN_PROGRESS
        }
    }
}

data class GoalContributionResult(
    val success: Boolean,
    val message: String,
    val goalId: String,
    val contributionId: String,
    val amount: Long,
    val currentAmount: Long,
    val walletBalance: Long?,
    val timestamp: String
)
