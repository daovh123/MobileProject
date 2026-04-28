package com.example.mobileproject.data.model.goal

import com.google.gson.annotations.SerializedName

data class CreateGoalRequest(
    @SerializedName("coupleId") val coupleId: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("targetAmount") val targetAmount: Long,
    @SerializedName("deadline") val deadline: String?
)

data class GoalResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("goalId") val goalId: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("targetAmount") val targetAmount: Long?,
    @SerializedName("currentAmount") val currentAmount: Long?,
    @SerializedName("status") val status: String?,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("createdAt") val createdAt: String?
)

data class ContributeFromWalletRequest(
    @SerializedName("amount") val amount: Long,
    @SerializedName("note") val note: String?
)

data class ContributeDirectRequest(
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
    @SerializedName("currentAmount") val currentAmount: Long?,
    @SerializedName("walletBalance") val walletBalance: Long?,
    @SerializedName("timestamp") val timestamp: String?
)

data class SavingGoalDto(
    @SerializedName("id") val id: String,
    @SerializedName("coupleId") val coupleId: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String?,
    @SerializedName("targetAmount") val targetAmount: Long,
    @SerializedName("currentAmount") val currentAmount: Long,
    @SerializedName("status") val status: String,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("createdAt") val createdAt: String?
)
