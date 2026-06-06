package com.example.mobileproject.domain.entity

/**
 * Enum xác định đích đến của người dùng ngay sau khi đăng nhập thành công.
 * Được sử dụng để điều hướng người dùng đến đúng màn hình dựa trên trạng thái hồ sơ.
 *
 * - PROFILE: Chuyển đến màn hình hoàn thiện hồ sơ (người dùng chưa điền đủ thông tin bắt buộc)
 * - HOME: Chuyển đến màn hình chính (hồ sơ đã hoàn thiện, có thể sử dụng đầy đủ tính năng)
 */
enum class PostLoginDestination {
    PROFILE,
    HOME,
}

/**
 * Xác định đích đến phù hợp sau khi đăng nhập dựa trên trạng thái phiên.
 * Logic: nếu hồ sơ chưa hoàn thiện → PROFILE, ngược lại → HOME.
 */
fun AuthSession.resolvePostLoginDestination(): PostLoginDestination {
    return when {
        !profileCompleted -> PostLoginDestination.PROFILE
        else -> PostLoginDestination.HOME
    }
}
