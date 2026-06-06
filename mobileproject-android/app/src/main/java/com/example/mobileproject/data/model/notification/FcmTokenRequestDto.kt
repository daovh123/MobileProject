package com.example.mobileproject.data.model.notification

/**
 * Request body cho API đăng ký FCM token.
 *
 * FCM token được dùng để gửi push notification đến thiết bị cụ thể.
 * Token phải được đăng ký lại sau khi reinstall app hoặc clear data.
 */
data class FcmTokenRequestDto(
    /** FCM registration token từ Firebase Cloud Messaging, bắt buộc. */
    val token: String,
)
