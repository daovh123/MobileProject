package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransferMoneyUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class TransferMoneyViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferMoneyUiState())
    val uiState: StateFlow<TransferMoneyUiState> = _uiState.asStateFlow()

    fun submitTransfer(
        amount: Long,
        bankName: String,
        accountNumber: String,
        note: String?,
    ) {
        val session = authSessionStore.load()
        val coupleId = session?.coupleId
        if (coupleId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Không tìm thấy thông tin cặp đôi để tạo giao dịch.") }
            return
        }
        if (amount <= 0L) {
            _uiState.update { it.copy(errorMessage = "Số tiền chuyển phải lớn hơn 0.") }
            return
        }

        val transferNote = buildString {
            append("Chuyển khoản ")
            append(bankName)
            append(" - STK ")
            append(accountNumber)
            note?.takeIf { it.isNotBlank() }?.let {
                append(" | ")
                append(it.trim())
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (
                val result = transactionRepository.createTransaction(
                    coupleId = coupleId,
                    amount = amount,
                    type = "EXPENSE",
                    category = "OTHERS",
                    note = transferNote,
                )
            ) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.throwable.message ?: "Không thể tạo giao dịch chuyển khoản.",
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
