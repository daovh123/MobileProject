package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.AuthSession

/**
 * Repository quản lý xác thực người dùng (authentication).
 *
 * Chịu trách nhiệm đăng nhập, đăng ký và đăng xuất.
 * Được inject vào các use case thông qua Hilt để đảm bảo
 * nguyên tắc Dependency Inversion (domain không phụ thuộc data layer).
 */
interface AuthRepository {

    /**
     * Đăng nhập bằng tên người dùng hoặc email kết hợp mật khẩu.
     *
     * @param usernameOrEmail Tên đăng nhập hoặc địa chỉ email
     * @param password Mật khẩu của người dùng
     * @return [AuthSession] chứa token và thông tin phiên đăng nhập
     * @throws com.example.mobileproject.core.exception.ApiException khi thông tin đăng nhập không hợp lệ
     * @throws java.io.IOException khi mất kết nối mạng
     */
    suspend fun login(usernameOrEmail: String, password: String): AuthSession

    /**
     * Đăng ký tài khoản mới.
     *
     * @param username Tên đăng nhập (duy nhất trong hệ thống)
     * @param email Địa chỉ email hợp lệ
     * @param password Mật khẩu (phải đáp ứng yêu cầu bảo mật)
     * @return [AuthSession] chứa token và thông tin phiên đăng nhập sau khi đăng ký
     * @throws com.example.mobileproject.core.exception.ApiException khi username/email đã tồn tại
     * @throws java.io.IOException khi mất kết nối mạng
     */
    suspend fun register(username: String, email: String, password: String): AuthSession

    /**
     * Đăng xuất và hủy phiên làm việc hiện tại.
     *
     * @param token Token xác thực của phiên cần hủy
     * @throws com.example.mobileproject.core.exception.ApiException khi token không hợp lệ
     * @throws java.io.IOException khi mất kết nối mạng
     */
    suspend fun logout(token: String)
}
