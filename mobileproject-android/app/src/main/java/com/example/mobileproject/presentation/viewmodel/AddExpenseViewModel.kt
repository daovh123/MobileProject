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

/**
 * UI state cho màn hình thêm chi tiêu.
 *
 * @property amount Chuỗi số tiền người dùng nhập (cho phép số và 1 dấu chấm thập phân)
 * @property selectedCategory Danh mục chi tiêu đang được chọn
 * @property note Ghi chú cho giao dịch
 * @property date Thời gian giao dịch (millis), mặc định là thời điểm hiện tại
 * @property availableBalance Số dư khả dụng trong ví chung của cặp đôi
 * @property isLoading Đang gửi yêu cầu tạo giao dịch lên server
 * @property isSuccess Tạo chi tiêu thành công
 * @property error Thông báo lỗi nếu có
 */
data class AddExpenseUiState(
    val amount: String = "",
    val selectedCategory: ExpenseCategory = ExpenseCategory.FoodDrink,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val availableBalance: Long = 0L,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel phục vụ màn hình thêm chi tiêu mới.
 *
 * Xử lý business logic:
 * - Tải số dư ví chung từ [WalletRepository] khi màn hình mở
 * - Quản lý form nhập liệu: số tiền, danh mục, ghi chú, ngày
 * - Validate số tiền trước khi gửi lên server
 * - Tạo giao dịch chi tiêu thông qua [TransactionRepository]
 *
 * Tự động load số dư ví trong init block để hiển thị cho người dùng.
 */
@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    // StateFlow pattern: MutableStateFlow nội bộ + expose read-only asStateFlow
    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        loadBalance()
    }

    /**
     * Tải số dư ví chung của couple từ repository.
     *
     * Lấy coupleId và token từ AuthSessionStore, sau đó gọi walletRepository
     * để lấy thông tin ví và cập nhật availableBalance trong UI state.
     */
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

    /**
     * Cập nhật số tiền khi người dùng nhập.
     *
     * Chỉ cho phép nhập chữ số và tối đa 1 dấu chấm thập phân.
     * Nếu input không hợp lệ, giữ nguyên giá trị cũ (bỏ qua input).
     *
     * @param newAmount Chuỗi số tiền mới từ input của người dùng
     */
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

    /**
     * Chọn danh mục chi tiêu.
     *
     * @param category Danh mục chi tiêu được chọn
     */
    fun onCategorySelect(category: ExpenseCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    /**
     * Cập nhật ghi chú cho giao dịch.
     *
     * @param newNote Chuỗi ghi chú mới
     */
    fun onNoteChange(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    /**
     * Cập nhật ngày giao dịch.
     *
     * @param newDate Thời gian giao dịch tính bằng milliseconds
     */
    fun onDateChange(newDate: Long) {
        _uiState.update { it.copy(date = newDate) }
    }

    /**
     * Lưu chi tiêu mới lên server.
     *
     * Validate số tiền phải > 0 trước khi gửi. Lấy coupleId từ session.
     * Gọi transactionRepository.createTransaction với type = "EXPENSE".
     * Cập nhật isSuccess = true nếu thành công, error nếu thất bại.
     */
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
    
    /**
     * Xóa thông báo lỗi hiện tại trong UI state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
