package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(username: String, email: String, password: String): AuthSession {
        return repository.register(username, email, password)
    }
}
