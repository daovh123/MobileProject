package com.example.mobileproject.domain.entity

enum class PayoutStatus {
    PENDING,
    PAID,
    FAILED,
    UNKNOWN,
}

data class PayoutRequest(
    val id: String,
    val coupleId: String,
    val amount: Long,
    val status: PayoutStatus,
    val transferCode: String,
    val createdAt: String,
    val paidAt: String?,
    val currentBalance: Long?,
)

