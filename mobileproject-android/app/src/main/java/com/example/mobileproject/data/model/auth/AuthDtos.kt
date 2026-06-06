package com.example.mobileproject.data.model.auth

/**
 * Request body cho API đăng nhập.
 *
 * Hỗ trợ đăng nhập bằng username hoặc email.
 * JSON field name trùng với tên thuộc tính Kotlin.
 */
data class LoginRequestDto(
    /** Username hoặc email của người dùng, bắt buộc, không được rỗng. */
    val usernameOrEmail: String,
    /** Mật khẩu người dùng, bắt buộc, không được rỗng. */
    val password: String,
)

/**
 * Request body cho API đăng ký tài khoản mới.
 *
 * Cả 3 trường đều bắt buộc. Password phải tuân thủ quy tắc bảo mật của backend.
 * JSON field name trùng với tên thuộc tính Kotlin.
 */
data class RegisterRequestDto(
    /** Tên đăng ký, phải unique trong hệ thống, bắt buộc. */
    val username: String,
    /** Email đăng ký, phải đúng định dạng email, bắt buộc. */
    val email: String,
    /** Mật khẩu đăng ký, bắt buộc. */
    val password: String,
)

/**
 * Response chung cho cả API login và register.
 *
 * Khi [success] = true, các trường [token], [username], [email] sẽ có giá trị.
 * Khi [success] = false, chỉ [message] mô tả lý do thất bại.
 *
 * [profileCompleted] và [coupleConnected] chỉ có ý nghĩa sau khi login/register thành công,
 * dùng để điều hướng onboarding flow.
 */
data class AuthResponseDto(
    /** true nếu xác thực thành công. */
    val success: Boolean,
    /** Thông báo mô tả kết quả (thành công hoặc lý do thất bại). */
    val message: String,
    /** JWT token xác thực, chỉ có khi [success] = true, null khi thất bại. */
    val token: String?,
    /** Username của người dùng đã đăng nhập, null khi thất bại. */
    val username: String?,
    /** Email của người dùng đã đăng nhập, null khi thất bại. */
    val email: String?,
    /** true nếu hồ sơ cá nhân đã hoàn thành, null nếu API không trả về. */
    val profileCompleted: Boolean? = null,
    /** true nếu đã kết nối với partner (couple), null nếu API không trả về. */
    val coupleConnected: Boolean? = null,
)

/**
 * Response cho API đăng xuất.
 *
 * Logout là idempotent — gọi nhiều lần vẫn trả success.
 */
data class LogoutResponseDto(
    /** true nếu logout thành công. */
    val success: Boolean,
    /** Thông báo mô tả kết quả. */
    val message: String,
)
