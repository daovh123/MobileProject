package com.example.mobileproject.domain.usecase.wallet

import com.example.mobileproject.domain.repository.WalletRepository
import javax.inject.Inject

class UpdateWalletBalanceUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(newBalance: Long) {
        repository.updateLocalBalance(newBalance)
    }
}