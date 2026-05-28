package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.PayoutStatus
import com.example.mobileproject.domain.repository.PayoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransferMoneyUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val isWaitingBankConfirmation: Boolean = false,
    val payoutId: String? = null,
    val transferCode: String? = null,
    val statusText: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class TransferMoneyViewModel @Inject constructor(
    private val payoutRepository: PayoutRepository,
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
            _uiState.update { it.copy(errorMessage = "Khong tim thay thong tin cap doi de tao lenh rut.") }
            return
        }
        if (amount <= 0L) {
            _uiState.update { it.copy(errorMessage = "So tien rut phai lon hon 0.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    isSuccess = false,
                    isWaitingBankConfirmation = false,
                    payoutId = null,
                    transferCode = null,
                    statusText = null,
                    errorMessage = null,
                )
            }

            when (val result = payoutRepository.createPayout(coupleId = coupleId, amount = amount)) {
                is Resource.Success -> {
                    val payout = result.data
                    val summary = buildString {
                        append("Da tao lenh rut ")
                        append(payout.transferCode)
                        append(". Chuyen tien that theo thong tin ngan hang va ma nay.")
                        if (!bankName.isBlank() && !accountNumber.isBlank()) {
                            append(" (")
                            append(bankName)
                            append(" - ")
                            append(accountNumber)
                            append(")")
                        }
                        if (!note.isNullOrBlank()) {
                            append(" | ")
                            append(note.trim())
                        }
                    }
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isWaitingBankConfirmation = true,
                            payoutId = payout.id,
                            transferCode = payout.transferCode,
                            statusText = summary,
                        )
                    }
                    pollPayoutStatus(payout.id)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.throwable.message ?: "Khong the tao yeu cau rut tien.",
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private suspend fun pollPayoutStatus(payoutId: String) {
        repeat(20) {
            delay(3000L)
            when (val result = payoutRepository.getPayout(payoutId)) {
                is Resource.Success -> {
                    when (result.data.status) {
                        PayoutStatus.PAID -> {
                            _uiState.update {
                                it.copy(
                                    isWaitingBankConfirmation = false,
                                    isSuccess = true,
                                    statusText = "Rut tien thanh cong. Quy da duoc cap nhat.",
                                )
                            }
                            return
                        }
                        PayoutStatus.FAILED -> {
                            _uiState.update {
                                it.copy(
                                    isWaitingBankConfirmation = false,
                                    statusText = "Lenh rut tien that bai.",
                                    errorMessage = "SePay khong xac nhan duoc giao dich tien ra.",
                                )
                            }
                            return
                        }
                        else -> {
                            _uiState.update {
                                it.copy(statusText = "Dang cho SePay xac nhan giao dich tien ra...")
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(statusText = "Da tao lenh rut, dang dong bo trang thai...")
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

