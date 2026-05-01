package com.example.mobileproject.data.model.transaction

import com.google.gson.annotations.SerializedName

data class TransactionRequestDto(
    @SerializedName("coupleId") val coupleId: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("type") val type: String,
    @SerializedName("category") val category: String,
    @SerializedName("note") val note: String?
)

data class TransactionResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("transactionId") val transactionId: String?,
    @SerializedName("amount") val amount: Long?,
    @SerializedName("type") val type: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("note") val note: String?,
    // BACKEND TRẢ VỀ currentBalance, PHẢI KHỚP CHÍNH XÁC TÊN NÀY
    @SerializedName("currentBalance") val currentBalance: Long?,
    @SerializedName("createdAt") val createdAt: String?
)

data class IncomeRequestDto(
    @SerializedName("coupleId") val coupleId: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("targetType") val targetType: String,
    @SerializedName("goalId") val goalId: String?,
    @SerializedName("note") val note: String?
)

data class TransactionDto(
    @SerializedName("id") val id: String,
    @SerializedName("coupleId") val coupleId: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("type") val type: String,
    @SerializedName("category") val category: String,
    @SerializedName("note") val note: String?,
    @SerializedName("createdAt") val createdAt: String
)