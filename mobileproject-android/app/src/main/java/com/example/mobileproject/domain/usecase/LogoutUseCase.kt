package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(token: String) {
        repository.logout(token)
    }
}
