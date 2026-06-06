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

/**
 * Trạng thái UI cho màn hình Giao dịch (Transaction).
 *
 * @property isLoading True khi đang tải hoặc tạo giao dịch
 * @property transactionResponse Kết quả từ API tạo giao dịch (chứa totalBalance mới)
 * @property error Thông báo lỗi
 * @property totalBalance Số dư tổng sau giao dịch (cập nhật từ API response)
 * @property transactions Danh sách tất cả giao dịch của cặp đôi
 */
data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactionResponse: TransactionResponse? = null,
    val error: String? = null,
    val totalBalance: Long = 0L,
    val transactions: List<Transaction> = emptyList()
)

/**
 * ViewModel cho màn hình Giao dịch (Transaction).
 *
 * Quản lý business logic:
 * - Tạo giao dịch chi tiêu mới (expense)
 * - Xử lý thu nhập (income) với targetType (ví chính / mục tiêu tiết kiệm)
 * - Tải danh sách giao dịch
 * - Cập nhật số dư ví local sau mỗi giao dịch thành công
 *
 * Sử dụng [Resource] sealed class để xử lý kết quả từ repository,
 * pattern phổ biến trong clean architecture với Flow<Resource<T>>.
 *
 * Sau mỗi giao dịch thành công, gọi [updateWalletBalanceUseCase] để đồng bộ
 * số dư local (SharedPreferences) với server, giúp màn hình khác
 * hiển thị đúng số dư mà không cần reload.
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val updateWalletBalanceUseCase: UpdateWalletBalanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    /**
     * Tạo giao dịch chi tiêu mới.
     * Sau khi tạo thành công, cập nhật số dư ví local và reload danh sách giao dịch.
     *
     * @param coupleId ID cặp đôi
     * @param amount Số tiền (VND, dương)
     * @param type Loại giao dịch ("EXPENSE")
     * @param category Danh mục chi tiêu (vd: "Ăn uống", "Di chuyển")
     * @param note Ghi chú (nullable)
     */
    fun createTransaction(
        coupleId: String,
        amount: Long,
        type: String,
        category: String,
        note: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            // Resource pattern: Loading/Success/Error sealed class từ repository
            when (val result = transactionRepository.createTransaction(coupleId, amount, type, category, note)) {
                is Resource.Success -> {
                    val currentBalance = result.data.totalBalance
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transactionResponse = result.data,
                        totalBalance = currentBalance ?: _uiState.value.totalBalance
                    )
                    
                    // Chỉ cập nhật ví local nếu backend trả về totalBalance mới
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

    /**
     * Xử lý thu nhập: nạp tiền vào ví chính hoặc mục tiêu tiết kiệm.
     * Cập nhật số dư ví local và reload danh sách giao dịch.
     *
     * @param coupleId ID cặp đôi
     * @param amount Số tiền thu nhập (VND)
     * @param targetType Loại đích đến ("WALLET" hoặc "GOAL")
     * @param goalId ID mục tiêu tiết kiệm (bắt buộc nếu targetType = "GOAL")
     * @param note Ghi chú (nullable)
     */
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

    /**
     * Reset UI state về trạng thái ban đầu.
     * Gọi khi đóng dialog/form tạo giao dịch.
     */
    fun clearState() {
        _uiState.value = TransactionUiState()
    }

    /**
     * Tải danh sách giao dịch của cặp đôi.
     *
     * @param coupleId ID cặp đôi
     */
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