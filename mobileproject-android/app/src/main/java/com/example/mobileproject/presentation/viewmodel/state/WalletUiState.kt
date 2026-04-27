package com.example.mobileproject.presentation.viewmodel.state

import com.example.mobileproject.domain.entity.Wallet

data class WalletUiState(
    val isLoading: Boolean = false,
    val wallet: Wallet? = null,
    val error: String? = null
)