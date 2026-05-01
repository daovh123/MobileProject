package com.example.mobileproject.presentation.viewmodel.state

import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend
import com.example.mobileproject.domain.entity.Transaction
import com.example.mobileproject.domain.entity.Wallet

data class WalletUiState(
    val isLoading: Boolean = false,
    val wallet: Wallet? = null,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val spendingTrend: List<SpendingTrend> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val allTransactions: List<Transaction> = emptyList(),
    val error: String? = null
)
