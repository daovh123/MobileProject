package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getWallet(coupleId: String, token: String): Flow<Result<Wallet>>
    suspend fun updateLocalBalance(newBalance: Long)
    val walletState: Flow<Wallet?>
}