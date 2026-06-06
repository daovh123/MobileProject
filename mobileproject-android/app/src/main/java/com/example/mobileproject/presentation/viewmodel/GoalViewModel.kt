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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Trạng thái UI cho màn hình Mục tiêu (Goals).
 *
 * @property isLoading True khi đang tải danh sách mục tiêu
 * @property goals Danh sách tất cả mục tiêu (cả tiết kiệm và tương lai)
 * @property error Thông báo lỗi chung
 * @property contributionMessage Thông báo thành công khi đóng góp vào mục tiêu tiết kiệm
 * @property isCreating True khi đang tạo mục tiêu mới
 * @property createSuccess True khi tạo mục tiêu thành công, UI quan sát để đóng dialog/form
 */
data class GoalUiState(
    val isLoading: Boolean = false,
    val goals: List<Goal> = emptyList(),
    val error: String? = null,
    val contributionMessage: String? = null,
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false
)

/**
 * ViewModel cho màn hình Mục tiêu (Goals) của cặp đôi.
 *
 * Quản lý business logic:
 * - Tải danh sách mục tiêu (tiết kiệm và tương lai) theo coupleId
 * - Tạo mục tiêu tiết kiệm mới (saving goal)
 * - Tạo mục tiêu tương lai mới (future goal) với danh sách task
 * - Đánh dấu hoàn thành task trong mục tiêu tương lai
 * - Đóng góp tiền vào mục tiêu tiết kiệm
 *
 * Sử dụng [Resource] sealed class (Loading/Success/Error) để xử lý kết quả từ use case,
 * phù hợp với pattern Flow<Resource<T>> trong repository layer.
 *
 * [loadGoalsJob] được quản lý để hủy job cũ khi load lại, tránh duplicate emissions.
 */
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
    private var loadGoalsJob: Job? = null

    /**
     * Lấy coupleId từ session local.
     * @return coupleId hoặc null nếu chưa đăng nhập/ghép đôi
     */
    private fun getCoupleId(): String? = authSessionStore.load()?.coupleId

    /**
     * Tải danh sách mục tiêu theo coupleId.
     * Hủy job cũ trước khi tạo mới để tránh duplicate emissions.
     * Sử dụng Flow<Resource> pattern: Loading -> Success/Error
     */
    fun loadGoals() {
        val cid = getCoupleId() ?: return
        loadGoalsJob?.cancel()
        loadGoalsJob = viewModelScope.launch {
            getGoalsUseCase(cid).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, goals = resource.data, error = null) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.throwable.message) }
                }
            }
        }
    }

    /**
     * Tạo mục tiêu tiết kiệm mới.
     *
     * @param name Tên mục tiêu
     * @param category Danh mục (vd: "du lịch", "mua sắm")
     * @param targetAmount Số tiền mục tiêu (VND)
     * @param deadline Hạn chót (nullable, format ISO date)
     *
     * Sau khi tạo thành công, tự động gọi [loadGoals] để refresh danh sách.
     */
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

    /**
     * Tạo mục tiêu tương lai mới với danh sách công việc cần hoàn thành.
     *
     * @param name Tên mục tiêu
     * @param category Danh mục
     * @param deadline Hạn chót (nullable)
     * @param tasks Danh sách công việc ([GoalTask]) cần hoàn thành
     */
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

    /**
     * Đánh dấu hoàn thành/bỏ hoàn thành một task trong mục tiêu tương lai.
     * Sử dụng optimistic update: cập nhật UI ngay trước khi server xác nhận.
     *
     * @param goalId ID của mục tiêu chứa task
     * @param taskId ID của task cần toggle
     */
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

    /**
     * Đóng góp tiền vào mục tiêu tiết kiệm.
     *
     * @param goalId ID mục tiêu tiết kiệm
     * @param amount Số tiền đóng góp (VND)
     * @param note Ghi chú (nullable)
     * @param contributorId ID người đóng góp (nullable, mặc định là người gọi)
     */
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

    /**
     * Xóa tất cả thông báo (thành công, lỗi) khỏi UI state.
     */
    fun clearMessage() {
        _uiState.update { it.copy(contributionMessage = null, error = null, createSuccess = false) }
    }
}
