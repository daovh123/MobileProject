package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.repository.TransactionRepository
import com.example.mobileproject.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopUpUiState(
    val amount: String = "",
    val note: String = "",
    val destination: String = "Nạp vào Ví chính",
    val currentBalance: Long = 0L,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) {
    val predictedBalance: Long
        get() {
            val addAmount = amount.toLongOrNull() ?: 0L
            return currentBalance + addAmount
        }
}

@HiltViewModel
class TopUpViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopUpUiState())
    val uiState: StateFlow<TopUpUiState> = _uiState.asStateFlow()

    init {
        loadCurrentBalance()
    }

    private fun loadCurrentBalance() {
        val session = authSessionStore.load()
        val coupleId = session?.coupleId ?: return
        val token = session.token

        viewModelScope.launch {
            walletRepository.getWallet(coupleId, token).collect { result ->
                result.onSuccess { wallet ->
                    _uiState.update { it.copy(currentBalance = wallet.balance) }
                }
            }
        }
    }

    fun onAmountChange(newAmount: String) {
        if (newAmount.all { it.isDigit() }) {
            _uiState.update { it.copy(amount = newAmount) }
        }
    }

    fun onNoteChange(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun topUpNow() {
        val state = _uiState.value
        val amountLong = state.amount.toLongOrNull() ?: 0L
        if (amountLong <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        val session = authSessionStore.load()
        val coupleId = session?.coupleId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Sử dụng processIncome hoặc createTransaction tùy theo API backend
            // Ở đây dựa trên yêu cầu là Income
            val result = transactionRepository.createTransaction(
                coupleId = coupleId,
                amount = amountLong,
                type = "INCOME",
                category = "INCOME",
                note = state.note
            )

            when (result) {
                is com.example.mobileproject.core.result.Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is com.example.mobileproject.core.result.Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.throwable.message ?: "Top up failed") }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
