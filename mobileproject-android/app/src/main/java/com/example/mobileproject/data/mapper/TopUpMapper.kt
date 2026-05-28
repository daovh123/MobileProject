package com.example.mobileproject.data.mapper

import com.example.mobileproject.data.model.transaction.TopUpResponseDto
import com.example.mobileproject.data.model.transaction.PayoutResponseDto
import com.example.mobileproject.domain.entity.PayoutRequest
import com.example.mobileproject.domain.entity.PayoutStatus
import com.example.mobileproject.domain.entity.TopUpRequest
import com.example.mobileproject.domain.entity.TopUpStatus

fun TopUpResponseDto.toDomain(): TopUpRequest {
    return TopUpRequest(
        id = id.orEmpty(),
        coupleId = coupleId.orEmpty(),
        amount = amount ?: 0L,
        status = status.toTopUpStatus(),
        bankId = bankId.orEmpty(),
        bankName = bankName.orEmpty(),
        accountNumber = accountNumber.orEmpty(),
        accountName = accountName.orEmpty(),
        transferCode = transferCode.orEmpty(),
        transferContent = transferContent.orEmpty(),
        qrContent = qrContent.orEmpty(),
        qrImageUrl = qrImageUrl,
        createdAt = createdAt.orEmpty(),
        paidAt = paidAt,
        currentBalance = currentBalance,
    )
}

private fun String?.toTopUpStatus(): TopUpStatus {
    return runCatching { TopUpStatus.valueOf(orEmpty().trim().uppercase()) }.getOrDefault(TopUpStatus.UNKNOWN)
}

fun PayoutResponseDto.toDomain(): PayoutRequest {
    return PayoutRequest(
        id = id.orEmpty(),
        coupleId = coupleId.orEmpty(),
        amount = amount ?: 0L,
        status = status.toPayoutStatus(),
        transferCode = transferCode.orEmpty(),
        createdAt = createdAt.orEmpty(),
        paidAt = paidAt,
        currentBalance = currentBalance,
    )
}

private fun String?.toPayoutStatus(): PayoutStatus {
    return runCatching { PayoutStatus.valueOf(orEmpty().trim().uppercase()) }.getOrDefault(PayoutStatus.UNKNOWN)
}
