package com.example.mobileproject.domain.entity

enum class TopUpStatus {
    PENDING,
    PAID,
    FAILED,
    EXPIRED,
    UNKNOWN,
}

data class TopUpRequest(
    val id: String,
    val coupleId: String,
    val amount: Long,
    val status: TopUpStatus,
    val bankId: String,
    val bankName: String,
    val accountNumber: String,
    val accountName: String,
    val transferCode: String,
    val transferContent: String,
    val qrContent: String,
    val qrImageUrl: String?,
    val createdAt: String,
    val paidAt: String?,
    val currentBalance: Long?,
)
