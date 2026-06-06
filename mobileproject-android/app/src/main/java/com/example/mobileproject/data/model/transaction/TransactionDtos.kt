package com.example.mobileproject.data.model.transaction

import com.google.gson.annotations.SerializedName

/**
 * Request body cho API tạo giao dịch chi tiêu.
 *
 * Phản ánh việc trừ tiền từ ví couple.
 * [coupleId], [amount], [type], [category] là bắt buộc.
 */
data class TransactionRequestDto(
    /** UUID của couple, bắt buộc. */
    @SerializedName("coupleId") val coupleId: String,
    /** Số tiền giao dịch (VNĐ), bắt buộc, phải > 0. */
    @SerializedName("amount") val amount: Long,
    /** Loại giao dịch (VD: "EXPENSE"), bắt buộc. */
    @SerializedName("type") val type: String,
    /** Phân loại chi tiêu (VD: "Food", "Transport"), bắt buộc. */
    @SerializedName("category") val category: String,
    /** Ghi chú cho giao dịch, nullable. */
    @SerializedName("note") val note: String?
)

/**
 * Response từ API tạo giao dịch chi tiêu.
 *
 * Trả về [currentBalance] là số dư ví sau giao dịch.
 * JSON field "currentBalance" phải khớp chính xác tên này.
 */
data class TransactionResponseDto(
    /** true nếu tạo giao dịch thành công. */
    @SerializedName("success") val success: Boolean,
    /** Thông báo mô tả kết quả. */
    @SerializedName("message") val message: String,
    /** UUID của giao dịch vừa tạo, null khi thất bại. */
    @SerializedName("transactionId") val transactionId: String?,
    /** Số tiền giao dịch (VNĐ), null khi thất bại. */
    @SerializedName("amount") val amount: Long?,
    /** Loại giao dịch, null khi thất bại. */
    @SerializedName("type") val type: String?,
    /** Phân loại chi tiêu, null khi thất bại. */
    @SerializedName("category") val category: String?,
    /** Ghi chú giao dịch, nullable. */
    @SerializedName("note") val note: String?,
    /** Số dư ví sau giao dịch (VNĐ), null khi thất bại.
     * Backend trả về field tên "currentBalance", phải khớp chính xác. */
    @SerializedName("currentBalance") val currentBalance: Long?,
    /** Thời điểm tạo giao dịch, định dạng ISO datetime, null khi thất bại. */
    @SerializedName("createdAt") val createdAt: String?
)

/**
 * Request body cho API ghi nhận thu nhập.
 *
 * Hỗ trợ 2 chế độ [targetType]:
 * - `"WALLET"`: Thu nhập cộng trực tiếp vào ví, [goalId] sẽ bị bỏ qua
 * - `"SAVING_GOAL"`: Thu nhập chuyển thẳng vào mục tiêu tiết kiệm, [goalId] là bắt buộc
 */
data class IncomeRequestDto(
    /** UUID của couple, bắt buộc. */
    @SerializedName("coupleId") val coupleId: String,
    /** Số tiền thu nhập (VNĐ), bắt buộc, phải > 0. */
    @SerializedName("amount") val amount: Long,
    /** Nơi nhận thu nhập: "WALCKET" hoặc "SAVING_GOAL", bắt buộc. */
    @SerializedName("targetType") val targetType: String,
    /** UUID mục tiêu tiết kiệm, bắt buộc khi targetType="SAVING_GOAL", nullable. */
    @SerializedName("goalId") val goalId: String?,
    /** Ghi chú cho giao dịch, nullable. */
    @SerializedName("note") val note: String?
)

/**
 * DTO đại diện cho một giao dịch trong danh sách lịch sử.
 *
 * Được sử dụng khi lấy danh sách giao dịch, khác với [TransactionResponseDto]
 * (dùng cho response khi tạo giao dịch mới).
 */
data class TransactionDto(
    /** UUID của giao dịch, bắt buộc. */
    @SerializedName("id") val id: String,
    /** UUID của couple, bắt buộc. */
    @SerializedName("coupleId") val coupleId: String,
    /** Số tiền giao dịch (VNĐ), bắt buộc. */
    @SerializedName("amount") val amount: Long,
    /** Loại giao dịch ("INCOME" hoặc "EXPENSE"), bắt buộc. */
    @SerializedName("type") val type: String,
    /** Phân loại giao dịch, bắt buộc. */
    @SerializedName("category") val category: String,
    /** Ghi chú giao dịch, nullable. */
    @SerializedName("note") val note: String?,
    /** Thời điểm tạo giao dịch, định dạng ISO datetime, bắt buộc. */
    @SerializedName("createdAt") val createdAt: String
)
