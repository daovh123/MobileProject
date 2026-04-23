package com.example.mobileproject.domain.entity

data class Transaction(
    val id: String,
    val coupleId: String,
    val amount: Long,
    val type: TransactionType,
    val category: String,
    val note: String,
    val createdAt: String
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class TransactionResponse(
    val success: Boolean,
    val message: String,
    val transactionId: String?,
    val amount: Long?,
    val type: String?,
    val category: String?,
    val note: String?,
    val totalBalance: Long?,
    val createdAt: String?
)