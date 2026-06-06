package com.example.mobileproject.domain.entity

/**
 * Domain entity đại diện cho phiên đăng nhập của người dùng.
 * Được sử dụng để quản lý trạng thái xác thực và điều hướng người dùng
 * đến đúng màn hình dựa trên mức độ hoàn thiện hồ sơ.
 *
 * @property token JWT token xác thực, được sử dụng cho mọi request API cần bảo mật
 * @property username Tên đăng nhập của người dùng, dùng để hiển thị và định danh
 * @property email Địa chỉ email đã đăng ký
 * @property profileCompleted True nếu người dùng đã hoàn thiện hồ sơ cá nhân (tên, ngày sinh, giới tính)
 * @property coupleConnected True nếu người dùng đã kết nối với đối tác (đã có couple)
 * @property coupleId Định danh cặp đôi, null nếu người dùng chưa kết nối với ai
 */
data class AuthSession(
    val token: String,
    val username: String,
    val email: String,
    val profileCompleted: Boolean,
    val coupleConnected: Boolean,
    val coupleId: String? = null
)
