package com.example.mobileproject.data.model.goal

import com.google.gson.annotations.SerializedName

data class GoalDto(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("id") val id: String?,
    @SerializedName("goalId") val goalId: String?,
    @SerializedName("coupleId") val coupleId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("type") val type: String?, // "SAVING" or "FUTURE"
    @SerializedName("status") val status: String?,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("createdAt") val createdAt: String?,
    
    // Saving Goal fields
    @SerializedName("targetAmount") val targetAmount: Long?,
    @SerializedName("currentAmount") val currentAmount: Long?,
    
    // Future Goal fields
    @SerializedName("progress") val progress: Double?,
    @SerializedName("tasks") val tasks: List<GoalTaskDto>?
)

data class GoalTaskDto(
    @SerializedName("taskId") val taskId: String?,
    @SerializedName("content") val content: String?, // Chấp nhận null từ server và xử lý ở mapper
    @SerializedName("completed") val isCompleted: Boolean = false
)

data class CreateGoalRequestDto(
    @SerializedName("coupleId") val coupleId: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("type") val type: String,
    @SerializedName("targetAmount") val targetAmount: Long? = null,
    @SerializedName("deadline") val deadline: String, // Bắt buộc
    @SerializedName("tasks") val tasks: List<GoalTaskDto>? = null
)

data class ToggleTaskResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("goalId") val goalId: String?,
    @SerializedName("taskId") val taskId: String?,
    @SerializedName("progress") val progress: Double?
)

data class ContributeFromWalletRequestDto(
    @SerializedName("amount") val amount: Long,
    @SerializedName("note") val note: String?
)

data class ContributeDirectRequestDto(
    @SerializedName("amount") val amount: Long,
    @SerializedName("contributorId") val contributorId: String,
    @SerializedName("note") val note: String?
)

data class ContributionResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("contributionId") val contributionId: String?,
    @SerializedName("goalId") val goalId: String?,
    @SerializedName("amount") val amount: Long?,
    @SerializedName("currentGoalAmount") val currentGoalAmount: Long?,
    @SerializedName("currentWalletBalance") val currentWalletBalance: Long?,
    @SerializedName("contributorId") val contributorId: String?
)
