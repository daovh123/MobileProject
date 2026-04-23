package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend
import com.example.mobileproject.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val spendingTrend: List<SpendingTrend> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun loadCategoryBreakdown(coupleId: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = analyticsRepository.getCategoryBreakdown(coupleId, startDate, endDate)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categoryBreakdown = result.data
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

    fun loadSpendingTrend(coupleId: String, year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = analyticsRepository.getSpendingTrend(coupleId, year, month)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        spendingTrend = result.data
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

    fun clearState() {
        _uiState.value = AnalyticsUiState()
    }
}