package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.repository.WalletRepository
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

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase,
    private val getMonthlyTrendUseCase: GetMonthlyTrendUseCase,
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private companion object {
        const val WALLET_STALE_MS: Long = 45_000L
    }

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private var loadDataJob: Job? = null
    private var lastLoadedMonth: Int? = null
    private var lastLoadedYear: Int? = null
    private var lastLoadedAt: Long = 0L

    init {
        viewModelScope.launch {
            walletRepository.walletState.collect { wallet ->
                if (wallet != null) {
                    _uiState.update { state -> state.copy(wallet = wallet) }
                }
            }
        }
    }

    fun ensureLoaded(force: Boolean = false) {
        fetchData(_selectedMonth.value, _selectedYear.value, force = force)
    }

    fun refreshIfStale() {
        fetchData(_selectedMonth.value, _selectedYear.value, force = false)
    }

    fun selectMonth(month: Int) {
        _selectedMonth.value = month
        fetchData(month, _selectedYear.value, force = true)
    }

    private fun fetchData(month: Int, year: Int, force: Boolean) {
        val hasCachedData = _uiState.value.wallet != null || _uiState.value.allTransactions.isNotEmpty()
        val samePeriod = lastLoadedMonth == month && lastLoadedYear == year
        val isFresh = System.currentTimeMillis() - lastLoadedAt < WALLET_STALE_MS
        if (!force && hasCachedData && samePeriod && isFresh) {
            return
        }

        loadDataJob?.cancel()
        val session = authSessionStore.load()
        val coupleId = session?.coupleId ?: return
        val token = session.token

        loadDataJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = state.wallet == null && state.allTransactions.isEmpty(),
                    error = null,
                )
            }

            try {
                val walletFlow = getWalletUseCase(coupleId, token)
                val categoryFlow = getCategoryBreakdownUseCase(coupleId, month, year)
                
                // Lấy danh sách giao dịch
                val transactionsResource = transactionRepository.getTransactions(coupleId)

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
                    lastLoadedMonth = month
                    lastLoadedYear = year
                    lastLoadedAt = System.currentTimeMillis()
                }.collect()

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
