package com.example.mobileproject.data.model.remote

import com.google.gson.annotations.SerializedName

/**
 * DTO đại diện cho thông tin ví chung của couple.
 *
 * Backend trả về trực tiếp object JSON, không bọc trong wrapper success/data.
 * Tất cả các trường đều nullable để xử lý gracefully khi API trả về dữ liệu thiếu.
 */
data class WalletResponse(
    /** UUID của couple sở hữu ví, nullable. */
    @SerializedName("idCouple") val idCouple: String?,
    /** Tên hiển thị của ví, nullable. */
    @SerializedName("walletName") val walletName: String?,
    /** Tổng số dư hiện tại (VNĐ), nullable. */
    @SerializedName("totalBalance") val totalBalance: Long?
)
