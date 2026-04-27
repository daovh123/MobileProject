package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.usecase.analytics.GetCategoryBreakdownUseCase
import com.example.mobileproject.domain.usecase.analytics.GetMonthlyTrendUseCase
import com.example.mobileproject.presentation.viewmodel.state.AnalyticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase,
    private val getMonthlyTrendUseCase: GetMonthlyTrendUseCase,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun loadAnalytics(coupleId: String, year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Sửa lỗi build: GetMonthlyTrendUseCase.invoke hiện chỉ nhận coupleId và year
            val categoryFlow = getCategoryBreakdownUseCase(coupleId, month, year)
            val trendFlow = getMonthlyTrendUseCase(coupleId, year)

            combine(categoryFlow, trendFlow) { categoryResult, trendResult ->
                val categoryList = categoryResult.getOrNull() ?: emptyList()
                val trendList = trendResult.getOrNull() ?: emptyList()
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        categoryBreakdown = categoryList,
                        spendingTrend = trendList,
                        error = if (categoryResult.isFailure) categoryResult.exceptionOrNull()?.message else null
                    )
                }
            }.collect()
        }
    }
}
