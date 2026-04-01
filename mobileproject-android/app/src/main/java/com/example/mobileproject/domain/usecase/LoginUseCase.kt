package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(usernameOrEmail: String, password: String): AuthSession {
        return repository.login(usernameOrEmail, password)
    }
}
