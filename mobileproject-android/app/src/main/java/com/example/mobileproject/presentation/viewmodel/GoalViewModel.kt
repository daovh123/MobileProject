package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.ContributeResponse
import com.example.mobileproject.domain.entity.GoalResponse
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalUiState(
    val isLoading: Boolean = false,
    val goals: List<SavingGoal> = emptyList(),
    val createGoalResponse: GoalResponse? = null,
    val contributeResponse: ContributeResponse? = null,
    val error: String? = null
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    fun loadGoals(coupleId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = goalRepository.getGoalsByCouple(coupleId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        goals = result.data
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

    fun createGoal(
        coupleId: String,
        name: String,
        targetAmount: Long,
        deadline: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = goalRepository.createGoal(coupleId, name, targetAmount, deadline)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createGoalResponse = result.data
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

    fun contributeFromWallet(
        goalId: String,
        amount: Long,
        contributorId: String,
        note: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = goalRepository.contributeFromWallet(goalId, amount, contributorId, note)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        contributeResponse = result.data
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

    fun contributeToGoal(
        goalId: String,
        amount: Long,
        contributorId: String,
        note: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = goalRepository.contributeToGoal(goalId, amount, contributorId, note)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        contributeResponse = result.data
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
        _uiState.value = GoalUiState()
    }
}