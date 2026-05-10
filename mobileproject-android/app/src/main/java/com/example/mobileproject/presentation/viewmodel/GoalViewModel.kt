package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.*
import com.example.mobileproject.domain.usecase.CreateGoalUseCase
import com.example.mobileproject.domain.usecase.GetGoalsUseCase
import com.example.mobileproject.domain.usecase.ToggleTaskUseCase
import com.example.mobileproject.domain.usecase.ContributeToGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalUiState(
    val isLoading: Boolean = false,
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
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun getCoupleId(): String? = authSessionStore.load()?.coupleId

    fun loadGoals() {
        val cid = getCoupleId() ?: return
        viewModelScope.launch {
            getGoalsUseCase(cid).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, goals = resource.data, error = null) }
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
                        loadGoals()
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
                        loadGoals()
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
                    is Resource.Success -> {
                        _uiState.update { it.copy(contributionMessage = resource.data.message) }
                        loadGoals()
                    }
                    is Resource.Error -> _uiState.update { it.copy(error = resource.throwable.message) }
                    else -> {}
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(contributionMessage = null, error = null, createSuccess = false) }
    }
}
