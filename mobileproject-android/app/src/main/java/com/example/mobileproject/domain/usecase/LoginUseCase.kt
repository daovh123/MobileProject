package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case xử lý đăng nhập người dùng.
 *
 * Hỗ trợ đăng nhập bằng cả username hoặc email kết hợp mật khẩu.
 * Trả về [AuthSession] chứa token cần lưu lại cho các API call sau.
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(usernameOrEmail: String, password: String): AuthSession {
        return repository.login(usernameOrEmail, password)
    }
}
