package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case xử lý đăng ký tài khoản mới.
 *
 * Tạo tài khoản với username, email, password và trả về [AuthSession]
 * để người dùng có thể đăng nhập ngay sau khi đăng ký.
 */
class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(username: String, email: String, password: String): AuthSession {
        return repository.register(username, email, password)
    }
}
