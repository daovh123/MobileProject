package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.Transaction
import com.example.mobileproject.domain.entity.TransactionResponse
import com.example.mobileproject.domain.repository.TransactionRepository
import com.example.mobileproject.domain.usecase.wallet.UpdateWalletBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactionResponse: TransactionResponse? = null,
    val error: String? = null,
    val totalBalance: Long = 0L,
    val transactions: List<Transaction> = emptyList()
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val updateWalletBalanceUseCase: UpdateWalletBalanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    fun createTransaction(
        coupleId: String,
        amount: Long,
        type: String,
        category: String,
        note: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.createTransaction(coupleId, amount, type, category, note)) {
                is Resource.Success -> {
                    val currentBalance = result.data.totalBalance
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transactionResponse = result.data,
                        totalBalance = currentBalance ?: _uiState.value.totalBalance
                    )
                    
                    // CHỈ CẬP NHẬT VÍ NẾU BACKEND TRẢ VỀ GIÁ TRỊ KHÁC NULL
                    if (currentBalance != null) {
                        updateWalletBalanceUseCase(currentBalance)
                    }
                    
                    loadTransactions(coupleId)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.throwable.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun processIncome(
        coupleId: String,
        amount: Long,
        targetType: String,
        goalId: String?,
        note: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.processIncome(coupleId, amount, targetType, goalId, note)) {
                is Resource.Success -> {
                    val currentBalance = result.data.totalBalance
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transactionResponse = result.data,
                        totalBalance = currentBalance ?: _uiState.value.totalBalance
                    )
                    
                    if (currentBalance != null) {
                        updateWalletBalanceUseCase(currentBalance)
                    }

                    loadTransactions(coupleId)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.throwable.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearState() {
        _uiState.value = TransactionUiState()
    }

    fun loadTransactions(coupleId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.getTransactions(coupleId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transactions = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.throwable.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }
}