package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.GoalContributionResult
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.repository.WalletRepository
import com.example.mobileproject.domain.usecase.ContributeToGoalUseCase
import com.example.mobileproject.domain.usecase.CreateGoalUseCase
import com.example.mobileproject.domain.usecase.GetSavingGoalsUseCase
import com.example.mobileproject.domain.usecase.WithdrawGoalToWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavingGoalUiState(
    val isLoading: Boolean = false,
    val isContributing: Boolean = false,
    val isWithdrawing: Boolean = false,
    val goals: List<SavingGoal> = emptyList(),
    val error: String? = null,
    val contributionMessage: String? = null
)

@HiltViewModel
class SavingGoalViewModel @Inject constructor(
    private val getSavingGoalsUseCase: GetSavingGoalsUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val contributeToGoalUseCase: ContributeToGoalUseCase,
    private val withdrawGoalToWalletUseCase: WithdrawGoalToWalletUseCase,
    private val walletRepository: WalletRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private companion object {
        const val SAVING_GOALS_STALE_MS: Long = 45_000L
    }

    private val _uiState = MutableStateFlow(SavingGoalUiState())
    val uiState: StateFlow<SavingGoalUiState> = _uiState.asStateFlow()
    private var loadGoalsJob: Job? = null
    private var lastLoadedAt: Long = 0L
    private var hasLoadedGoals: Boolean = false

    private fun getCoupleId(): String? {
        return authSessionStore.load()?.coupleId
    }

    fun ensureLoaded(force: Boolean = false) {
        val isFresh = System.currentTimeMillis() - lastLoadedAt < SAVING_GOALS_STALE_MS
        if (!force && hasLoadedGoals && isFresh) {
            return
        }

        val cid = getCoupleId()
        if (cid == null) {
            _uiState.update { it.copy(error = "Couple not connected or Session expired") }
            return
        }

        loadGoalsJob?.cancel()
        loadGoalsJob = viewModelScope.launch {
            getSavingGoalsUseCase(cid).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { state -> state.copy(isLoading = state.goals.isEmpty()) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            hasLoadedGoals = true
                            lastLoadedAt = System.currentTimeMillis()
                            it.copy(isLoading = false, goals = resource.data, error = null)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = resource.throwable.message) }
                    }
                }
            }
        }
    }

    fun refreshIfStale() {
        ensureLoaded(force = false)
    }

    fun createGoal(name: String, category: String, targetAmount: Long, deadline: String?) {
        val cid = getCoupleId() ?: return
        viewModelScope.launch {
            createGoalUseCase(
                coupleId = cid,
                name = name,
                category = category,
                type = "SAVING",
                targetAmount = targetAmount,
                deadline = deadline
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        ensureLoaded(force = true) // Refresh list after creation
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
                    is Resource.Loading -> _uiState.update { it.copy(isContributing = true, error = null) }
                    is Resource.Success -> {
                        val result = resource.data
                        val walletBalance = result.walletBalance
                        if (walletBalance != null) {
                            walletRepository.updateLocalBalance(walletBalance)
                        }
                        applyGoalActionResult(result)
                        ensureLoaded(force = true)
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isContributing = false,
                                isWithdrawing = false,
                                error = resource.throwable.message,
                            )
                        }
                    }
                }
            }
        }
    }

    fun withdrawToWallet(goalId: String) {
        viewModelScope.launch {
            withdrawGoalToWalletUseCase(goalId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isWithdrawing = true, error = null) }
                    is Resource.Success -> {
                        val result = resource.data
                        val walletBalance = result.walletBalance
                        if (walletBalance != null) {
                            walletRepository.updateLocalBalance(walletBalance)
                        }
                        applyGoalActionResult(result)
                        ensureLoaded(force = true)
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isContributing = false,
                                isWithdrawing = false,
                                error = resource.throwable.message,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun applyGoalActionResult(result: GoalContributionResult) {
        val currentGoal = _uiState.value.goals.firstOrNull { it.id == result.goalId }
        val resolvedStatus = result.goalStatus ?: when {
            result.withdrawnAmount > 0L && result.currentAmount == 0L -> GoalStatus.WITHDRAWN
            currentGoal != null && result.currentAmount >= currentGoal.targetAmount -> GoalStatus.ACHIEVED
            else -> GoalStatus.IN_PROGRESS
        }

        hasLoadedGoals = true
        lastLoadedAt = System.currentTimeMillis()
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                isContributing = false,
                isWithdrawing = false,
                goals = state.goals.map { goal ->
                    if (goal.id == result.goalId) {
                        goal.copy(
                            currentAmount = result.currentAmount,
                            status = resolvedStatus,
                            withdrawnAmount = result.withdrawnAmount,
                        )
                    } else {
                        goal
                    }
                },
                contributionMessage = result.message,
                error = null,
            )
        }
    }

    fun clearMessage() {
        _uiState.update {
            it.copy(
                contributionMessage = null,
                error = null,
                isContributing = false,
                isWithdrawing = false,
            )
        }
    }
}
