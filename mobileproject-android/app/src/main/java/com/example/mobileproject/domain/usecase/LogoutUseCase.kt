package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case xử lý đăng xuất người dùng.
 *
 * Hủy phiên làm việc phía server và yêu cầu ViewModel
 * xóa token/local cache sau khi gọi thành công.
 */
class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(token: String) {
        repository.logout(token)
    }
}
