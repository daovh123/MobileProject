package com.example.mobileproject.domain.entity

sealed class Goal(
    open val id: String,
    open val coupleId: String,
    open val name: String,
    open val category: String,
    open val deadline: String?,
    open val status: GoalStatus,
    open val createdAt: String?,
    open val type: GoalType
)

data class SavingGoal(
    override val id: String,
    override val coupleId: String,
    override val name: String,
    override val category: String,
    override val deadline: String?,
    override val status: GoalStatus,
    override val createdAt: String?,
    val targetAmount: Long,
    val currentAmount: Long,
    val withdrawnAmount: Long = 0L,
) : Goal(id, coupleId, name, category, deadline, status, createdAt, GoalType.SAVING)

data class FutureGoal(
    override val id: String,
    override val coupleId: String,
    override val name: String,
    override val category: String,
    override val deadline: String?,
    override val status: GoalStatus,
    override val createdAt: String?,
    val tasks: List<GoalTask>,
    val progress: Double
) : Goal(id, coupleId, name, category, deadline, status, createdAt, GoalType.FUTURE)

data class GoalTask(
    val taskId: String,
    val content: String,
    val isCompleted: Boolean
)

enum class GoalType {
    SAVING, FUTURE
}

enum class GoalStatus {
    IN_PROGRESS,
    ACHIEVED,
    WITHDRAWN,
    FAILED;

    companion object {
        fun fromString(status: String?): GoalStatus = when (status?.uppercase()) {
            "ACHIEVED" -> ACHIEVED
            "WITHDRAWN" -> WITHDRAWN
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
    val goalStatus: GoalStatus? = null,
    val withdrawnAmount: Long = 0L,
    val timestamp: String
)
