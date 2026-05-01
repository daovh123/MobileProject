package com.example.mobileproject.domain.usecase.wallet

import com.example.mobileproject.domain.entity.Wallet
import com.example.mobileproject.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    operator fun invoke(coupleId: String, token: String): Flow<Result<Wallet>> {
        return repository.getWallet(coupleId, token)
    }
}