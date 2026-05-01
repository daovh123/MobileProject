package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.repository.WalletRepository
import com.example.mobileproject.domain.usecase.ContributeToGoalUseCase
import com.example.mobileproject.domain.usecase.CreateGoalUseCase
import com.example.mobileproject.domain.usecase.GetSavingGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavingGoalUiState(
    val isLoading: Boolean = false,
    val goals: List<SavingGoal> = emptyList(),
    val error: String? = null,
    val contributionMessage: String? = null
)

@HiltViewModel
class SavingGoalViewModel @Inject constructor(
    private val getSavingGoalsUseCase: GetSavingGoalsUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val contributeToGoalUseCase: ContributeToGoalUseCase,
    private val walletRepository: WalletRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingGoalUiState())
    val uiState: StateFlow<SavingGoalUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun getCoupleId(): String? {
        return authSessionStore.load()?.coupleId
    }

    fun loadGoals() {
        val cid = getCoupleId()
        if (cid == null) {
            _uiState.update { it.copy(error = "Couple not connected or Session expired") }
            return
        }

        viewModelScope.launch {
            getSavingGoalsUseCase(cid).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, goals = resource.data, error = null) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = resource.throwable.message) }
                    }
                }
            }
        }
    }

    fun createGoal(name: String, category: String, targetAmount: Long, deadline: String?) {
        val cid = getCoupleId() ?: return
        viewModelScope.launch {
            createGoalUseCase(cid, name, category, targetAmount, deadline).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        loadGoals() // Refresh list after creation
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = resource.throwable.message) }
                    }
                }
            }
        }
    }

    fun contribute(goalId: String, amount: Long, note: String?, contributorId: String? = null) {
        viewModelScope.launch {
            contributeToGoalUseCase(goalId, amount, note, contributorId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val result = resource.data
                        if (result.walletBalance != null) {
                            walletRepository.updateLocalBalance(result.walletBalance)
                        }
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                contributionMessage = result.message,
                                error = null 
                            ) 
                        }
                        loadGoals()
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = resource.throwable.message) }
                    }
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(contributionMessage = null, error = null) }
    }
}
