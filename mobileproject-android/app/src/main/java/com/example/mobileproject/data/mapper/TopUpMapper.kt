package com.example.mobileproject.data.mapper

import com.example.mobileproject.data.model.transaction.TopUpResponseDto
import com.example.mobileproject.data.model.transaction.PayoutResponseDto
import com.example.mobileproject.domain.entity.PayoutRequest
import com.example.mobileproject.domain.entity.PayoutStatus
import com.example.mobileproject.domain.entity.TopUpRequest
import com.example.mobileproject.domain.entity.TopUpStatus

/**
 * Ánh xạ [TopUpResponseDto] sang [TopUpRequest] domain entity.
 *
 * Xử lý null safety:
 * - Các trường String nullable dùng `.orEmpty()` → chuỗi rỗng nếu null
 * - [amount] dùng `?: 0L` → 0 nếu null
 * - [status] chuyển đổi qua [toTopUpStatus] enum parsing an toàn
 * - [qrImageUrl], [paidAt], [currentBalance] giữ nguyên nullable
 */
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

/**
 * Chuyển đổi chuỗi status sang [TopUpStatus] enum một cách an toàn.
 *
 * Xử lý:
 * - Trim và uppercase chuỗi trước khi parse
 * - Nếu parse thất bại (unknown status từ API), fallback về [TopUpStatus.UNKNOWN]
 * - Nếu chuỗi null, trả về [TopUpStatus.UNKNOWN]
 */
private fun String?.toTopUpStatus(): TopUpStatus {
    return runCatching { TopUpStatus.valueOf(orEmpty().trim().uppercase()) }.getOrDefault(TopUpStatus.UNKNOWN)
}

/**
 * Ánh xạ [PayoutResponseDto] sang [PayoutRequest] domain entity.
 *
 * Xử lý null safety:
 * - Các trường String nullable dùng `.orEmpty()` → chuỗi rỗng nếu null
 * - [amount] dùng `?: 0L` → 0 nếu null
 * - [status] chuyển đổi qua [toPayoutStatus] enum parsing an toàn
 * - [paidAt], [currentBalance] giữ nguyên nullable
 */
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

/**
 * Chuyển đổi chuỗi status sang [PayoutStatus] enum một cách an toàn.
 *
 * Xử lý:
 * - Trim và uppercase chuỗi trước khi parse
 * - Nếu parse thất bại (unknown status từ API), fallback về [PayoutStatus.UNKNOWN]
 * - Nếu chuỗi null, trả về [PayoutStatus.UNKNOWN]
 */
private fun String?.toPayoutStatus(): PayoutStatus {
    return runCatching { PayoutStatus.valueOf(orEmpty().trim().uppercase()) }.getOrDefault(PayoutStatus.UNKNOWN)
}
