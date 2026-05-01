package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.model.ExpenseCategory
import com.example.mobileproject.domain.repository.TransactionRepository
import com.example.mobileproject.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddExpenseUiState(
    val amount: String = "", // Khởi tạo rỗng để không bị vướng số 0 ban đầu
    val selectedCategory: ExpenseCategory = ExpenseCategory.FoodDrink,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val availableBalance: Long = 0L,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        loadBalance()
    }

    private fun loadBalance() {
        val session = authSessionStore.load()
        val coupleId = session?.coupleId ?: return
        val token = session.token
        
        viewModelScope.launch {
            walletRepository.getWallet(coupleId, token).collect { result ->
                result.onSuccess { wallet ->
                    _uiState.update { it.copy(availableBalance = wallet.balance) }
                }
            }
        }
    }

    fun onAmountChange(newAmount: String) {
        // Chỉ cho phép nhập số và tối đa 1 dấu chấm thập phân
        if (newAmount.isEmpty()) {
            _uiState.update { it.copy(amount = "") }
            return
        }
        if (newAmount.all { it.isDigit() || it == '.' } && newAmount.count { it == '.' } <= 1) {
            _uiState.update { it.copy(amount = newAmount) }
        }
    }

    fun onCategorySelect(category: ExpenseCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onNoteChange(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun onDateChange(newDate: Long) {
        _uiState.update { it.copy(date = newDate) }
    }

    fun saveExpense() {
        val state = _uiState.value
        val amountDouble = state.amount.toDoubleOrNull() ?: 0.0
        val amountLong = amountDouble.toLong()
        
        if (amountLong <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        val session = authSessionStore.load()
        val coupleId = session?.coupleId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = transactionRepository.createTransaction(
                coupleId = coupleId,
                amount = amountLong,
                type = "EXPENSE",
                category = state.selectedCategory.id,
                note = state.note
            )

            when (result) {
                is com.example.mobileproject.core.result.Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is com.example.mobileproject.core.result.Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.throwable.message ?: "Unknown error") }
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
