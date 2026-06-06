package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.repository.TransactionRepository
import com.example.mobileproject.domain.usecase.analytics.GetCategoryBreakdownUseCase
import com.example.mobileproject.domain.usecase.analytics.GetMonthlyTrendUseCase
import com.example.mobileproject.domain.usecase.wallet.GetWalletUseCase
import com.example.mobileproject.presentation.viewmodel.state.WalletUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel cho màn hình Ví (Wallet) của cặp đôi.
 *
 * Quản lý business logic:
 * - Tải thông tin ví chung (số dư)
 * - Tải phân tích chi tiêu theo danh mục cho tháng/năm đã chọn
 * - Tải xu hướng chi tiêu theo tháng
 * - Tải và hiển thị giao dịch gần đây
 * - Cho phép chọn tháng để xem dữ liệu chi tiêu
 *
 * Sử dụng [combine] để gộp Flow từ wallet và category breakdown,
 * cập nhật UI state một lần khi cả hai source có dữ liệu.
 * Sử dụng try/catch thay vì runCatching vì cần bắt lỗi outer scope của combine.
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase,
    private val getMonthlyTrendUseCase: GetMonthlyTrendUseCase,
    private val transactionRepository: TransactionRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    /** Tháng đang được chọn để xem phân tích chi tiêu (1-12). */
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    /** Tháng đang chọn, expose cho UI hiển thị tab/tháng. */
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    /** Năm đang được chọn. */
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    /** Job đang chạy để tải dữ liệu, dùng để hủy khi chuyển tháng. */
    private var loadDataJob: Job? = null

    /**
     * Tải lại dữ liệu cho tháng/năm đang chọn.
     * Được gọi khi màn hình hiển thị hoặc khi cần refresh.
     */
    fun loadData() {
        fetchData(_selectedMonth.value, _selectedYear.value)
    }

    /**
     * Chọn tháng khác để xem phân tích chi tiêu.
     * Tự động tải lại dữ liệu cho tháng mới.
     *
     * @param month Tháng mới (1-12)
     */
    fun selectMonth(month: Int) {
        _selectedMonth.value = month
        fetchData(month, _selectedYear.value)
    }

    /**
     * Tải dữ liệu ví, phân tích danh mục, và giao dịch cho tháng/năm cụ thể.
     *
     * Hủy job cũ trước khi tạo mới (tránh duplicate khi chuyển tháng nhanh).
     * Sử dụng [combine] để gộp wallet Flow và category Flow,
     * cập nhật _uiState trong block combine vì cả 2 cần đồng bộ.
     */
    private fun fetchData(month: Int, year: Int) {
        loadDataJob?.cancel()
        val session = authSessionStore.load()
        val coupleId = session?.coupleId ?: return
        val token = session.token

        loadDataJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val walletFlow = getWalletUseCase(coupleId, token)
                val categoryFlow = getCategoryBreakdownUseCase(coupleId, month, year)
                
                // Lấy danh sách giao dịch
                val transactionsResource = transactionRepository.getTransactions(coupleId)

                // combine gộp 2 Flow, emit mỗi khi 1 trong 2 thay đổi.
                // Cập nhật _uiState bên trong combine để cả wallet và category đồng bộ.
                combine(walletFlow, categoryFlow) { walletResult, categoryResult ->
                    _uiState.update { state ->
                        val allTrans = if (transactionsResource is com.example.mobileproject.core.result.Resource.Success) {
                            transactionsResource.data.sortedByDescending { it.createdAt }
                        } else emptyList()

                        // Sắp xếp các category theo chi tiêu từ lớn đến bé
                        val sortedCategories = categoryResult.getOrNull()
                            ?.sortedByDescending { it.totalAmount }
                            ?: emptyList()

                        state.copy(
                            isLoading = false,
                            wallet = walletResult.getOrNull(),
                            categoryBreakdown = sortedCategories,
                            allTransactions = allTrans,
                            recentTransactions = allTrans.take(5),
                            error = if (walletResult.isFailure) walletResult.exceptionOrNull()?.message else null
                        )
                    }
                }.collect()

            } catch (e: Exception) {
                // Bắt lỗi outer scope (vd: combine throw, repository error)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
