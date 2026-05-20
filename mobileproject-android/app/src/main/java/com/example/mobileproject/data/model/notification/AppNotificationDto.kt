package com.example.mobileproject.data.model.notification

import com.google.gson.annotations.SerializedName

data class AppNotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String?,
    @SerializedName("body") val body: String?,
    @SerializedName("read") val read: Boolean,
    @SerializedName("createdAt") val createdAt: String?,
)

data class NotificationPageDto(
    @SerializedName("content") val content: List<AppNotificationDto>,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
)

data class UnreadCountDto(
    @SerializedName("count") val count: Long,
)
