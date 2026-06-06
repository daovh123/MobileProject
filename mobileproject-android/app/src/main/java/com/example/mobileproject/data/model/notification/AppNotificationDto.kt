package com.example.mobileproject.data.model.notification

import com.google.gson.annotations.SerializedName

/**
 * DTO đại diện cho một thông báo trong ứng dụng.
 *
 * Thông báo được phân loại bởi [type] (VD: "COUPLE_REQUEST", "TRANSACTION", "GOAL").
 * [read] cho biết thông báo đã được đọc hay chưa.
 */
data class AppNotificationDto(
    /** UUID của thông báo, bắt buộc. */
    @SerializedName("id") val id: String,
    /** Loại thông báo (VD: "COUPLE_REQUEST", "TRANSACTION", "GOAL"), bắt buộc. */
    @SerializedName("type") val type: String,
    /** Tiêu đề thông báo, nullable. */
    @SerializedName("title") val title: String?,
    /** Nội dung chi tiết thông báo, nullable. */
    @SerializedName("body") val body: String?,
    /** true nếu thông báo đã được đọc, false nếu chưa. */
    @SerializedName("read") val read: Boolean,
    /** Thời điểm tạo thông báo, định dạng ISO datetime, nullable. */
    @SerializedName("createdAt") val createdAt: String?,
)

/**
 * DTO phân trang danh sách thông báo.
 *
 * Hỗ trợ infinite scroll với [currentPage], [hasNext].
 * [totalElements] và [totalPages] dùng để tính toán phân trang.
 */
data class NotificationPageDto(
    /** Danh sách thông báo ở trang hiện tại. */
    @SerializedName("content") val content: List<AppNotificationDto>,
    /** Tổng số thông báo trên tất cả các trang. */
    @SerializedName("totalElements") val totalElements: Long,
    /** Tổng số trang. */
    @SerializedName("totalPages") val totalPages: Int,
    /** Số trang hiện tại (0-indexed). */
    @SerializedName("currentPage") val currentPage: Int,
    /** true nếu còn trang tiếp theo để load. */
    @SerializedName("hasNext") val hasNext: Boolean,
)

/**
 * DTO chứa số lượng thông báo chưa đọc.
 *
 * Dùng để hiển thị badge trên tab/notification icon.
 */
data class UnreadCountDto(
    /** Số lượng thông báo chưa đọc, >= 0. */
    @SerializedName("count") val count: Long,
)
