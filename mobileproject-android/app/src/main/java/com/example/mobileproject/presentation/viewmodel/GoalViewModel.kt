package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.*
import com.example.mobileproject.domain.repository.WalletRepository
import com.example.mobileproject.domain.usecase.CreateGoalUseCase
import com.example.mobileproject.domain.usecase.GetGoalsUseCase
import com.example.mobileproject.domain.usecase.ToggleTaskUseCase
import com.example.mobileproject.domain.usecase.ContributeToGoalUseCase
import com.example.mobileproject.domain.usecase.WithdrawGoalToWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalUiState(
    val isLoading: Boolean = false,
    val isContributing: Boolean = false,
    val isWithdrawing: Boolean = false,
    val goals: List<Goal> = emptyList(),
    val error: String? = null,
    val contributionMessage: String? = null,
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val getGoalsUseCase: GetGoalsUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val contributeToGoalUseCase: ContributeToGoalUseCase,
    private val withdrawGoalToWalletUseCase: WithdrawGoalToWalletUseCase,
    private val walletRepository: WalletRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private companion object {
        const val GOALS_STALE_MS: Long = 45_000L
    }

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()
    private var loadGoalsJob: Job? = null
    private var lastLoadedAt: Long = 0L
    private var hasLoadedGoals: Boolean = false

    private fun getCoupleId(): String? = authSessionStore.load()?.coupleId

    fun ensureLoaded(force: Boolean = false) {
        val isFresh = System.currentTimeMillis() - lastLoadedAt < GOALS_STALE_MS
        if (!force && hasLoadedGoals && isFresh) {
            return
        }
        loadGoalsInternal()
    }

    fun refreshIfStale() {
        ensureLoaded(force = false)
    }

    private fun loadGoalsInternal() {
        val cid = getCoupleId() ?: return
        loadGoalsJob?.cancel()
        loadGoalsJob = viewModelScope.launch {
            getGoalsUseCase(cid).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { state ->
                        state.copy(isLoading = state.goals.isEmpty())
                    }
                    is Resource.Success -> _uiState.update {
                        hasLoadedGoals = true
                        lastLoadedAt = System.currentTimeMillis()
                        it.copy(isLoading = false, goals = resource.data, error = null)
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.throwable.message) }
                }
            }
        }
    }

    fun createSavingGoal(name: String, category: String, targetAmount: Long, deadline: String?) {
        val cid = getCoupleId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createSuccess = false, error = null) }
            createGoalUseCase(cid, name, category, "SAVING", targetAmount, deadline).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isCreating = true) }
                    is Resource.Success -> {
                        ensureLoaded(force = true)
                        _uiState.update { it.copy(isCreating = false, createSuccess = true) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isCreating = false, error = resource.throwable.message) }
                    }
                }
            }
        }
    }

    fun createFutureGoal(name: String, category: String, deadline: String?, tasks: List<GoalTask>) {
        val cid = getCoupleId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createSuccess = false, error = null) }
            createGoalUseCase(cid, name, category, "FUTURE", deadline = deadline, tasks = tasks).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isCreating = true) }
                    is Resource.Success -> {
                        ensureLoaded(force = true)
                        _uiState.update { it.copy(isCreating = false, createSuccess = true) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isCreating = false, error = resource.throwable.message) }
                    }
                }
            }
        }
    }

    fun toggleTask(goalId: String, taskId: String) {
        viewModelScope.launch {
            toggleTaskUseCase(goalId, taskId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> { /* Optionally show item-level loading */ }
                    is Resource.Success -> {
                        // Optimistic update or just refresh the specific goal in the list
                        val updatedProgress = resource.data
                        _uiState.update { state ->
                            val updatedGoals = state.goals.map { goal ->
                                if (goal is FutureGoal && goal.id == goalId) {
                                    val updatedTasks = goal.tasks.map { task ->
                                        if (task.taskId == taskId) task.copy(isCompleted = !task.isCompleted)
                                        else task
                                    }
                                    goal.copy(tasks = updatedTasks, progress = updatedProgress)
                                } else goal
                            }
                            state.copy(goals = updatedGoals)
                        }
                    }
                    is Resource.Error -> _uiState.update { it.copy(error = resource.throwable.message) }
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
                    is Resource.Error -> _uiState.update {
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
                    is Resource.Error -> _uiState.update {
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

    private fun applyGoalActionResult(result: GoalContributionResult) {
        hasLoadedGoals = true
        lastLoadedAt = System.currentTimeMillis()
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                isContributing = false,
                isWithdrawing = false,
                goals = state.goals.map { goal ->
                    if (goal is SavingGoal && goal.id == result.goalId) {
                        goal.copy(
                            currentAmount = result.currentAmount,
                            status = result.goalStatus ?: goal.status,
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
                createSuccess = false,
                isContributing = false,
                isWithdrawing = false,
            )
        }
    }
}
