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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state cho màn hình mục tiêu tiết kiệm.
 *
 * @property isLoading Đang tải dữ liệu từ server
 * @property goals Danh sách mục tiêu tiết kiệm của cặp đôi
 * @property error Thông báo lỗi nếu có
 * @property contributionMessage Thông báo thành công sau khi đóng góp vào mục tiêu
 */
data class SavingGoalUiState(
    val isLoading: Boolean = false,
    val goals: List<SavingGoal> = emptyList(),
    val error: String? = null,
    val contributionMessage: String? = null
)

/**
 * ViewModel phục vụ màn hình quản lý mục tiêu tiết kiệm.
 *
 * Xử lý business logic:
 * - Tải danh sách mục tiêu tiết kiệm của cặp đôi
 * - Tạo mục tiêu tiết kiệm mới (tên, danh mục, số tiền mục tiêu, hạn chót)
 * - Đóng góp tiền vào mục tiêu tiết kiệm, cập nhật số dư ví cục bộ
 * - Quản lý Job để hủy request cũ khi load lại danh sách
 *
 * Sử dụng các UseCase: [GetSavingGoalsUseCase], [CreateGoalUseCase], [ContributeToGoalUseCase]
 * để tách biệt business logic khỏi ViewModel.
 */
@HiltViewModel
class SavingGoalViewModel @Inject constructor(
    private val getSavingGoalsUseCase: GetSavingGoalsUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val contributeToGoalUseCase: ContributeToGoalUseCase,
    private val walletRepository: WalletRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    // StateFlow pattern: MutableStateFlow nội bộ + expose read-only asStateFlow
    private val _uiState = MutableStateFlow(SavingGoalUiState())
    val uiState: StateFlow<SavingGoalUiState> = _uiState.asStateFlow()
    // Job để hủy request load goals cũ khi có request mới (tránh race condition)
    private var loadGoalsJob: Job? = null

    /**
     * Lấy coupleId từ phiên đăng nhập hiện tại.
     *
     * @return coupleId hoặc null nếu chưa đăng nhập/không có couple
     */
    private fun getCoupleId(): String? {
        return authSessionStore.load()?.coupleId
    }

    /**
     * Tải danh sách mục tiêu tiết kiệm từ server.
     *
     * Hủy job load cũ trước khi tạo job mới để tránh race condition.
     * Sử dụng Flow collect với Resource pattern: Loading → Success/Error.
     *
     * @return Cập nhật [SavingGoalUiState.goals] nếu thành công, [SavingGoalUiState.error] nếu thất bại
     */
    fun loadGoals() {
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

    /**
     * Tạo mục tiêu tiết kiệm mới.
     *
     * Sau khi tạo thành công, tự động gọi [loadGoals] để refresh danh sách.
     *
     * @param name Tên mục tiêu tiết kiệm
     * @param category Danh mục mục tiêu
     * @param targetAmount Số tiền mục tiêu cần đạt (đơn vị: VND)
     * @param deadline Hạn chót đạt mục tiêu (chuỗi ngày, có thể null)
     */
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
                        loadGoals() // Refresh list after creation
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = resource.throwable.message) }
                    }
                }
            }
        }
    }

    /**
     * Đóng góp tiền vào mục tiêu tiết kiệm.
     *
     * Cập nhật số dư ví cục bộ thông qua walletRepository.updateLocalBalance
     * nếu server trả về walletBalance mới. Sau đó gọi [loadGoals] để refresh.
     *
     * @param goalId ID mục tiêu tiết kiệm cần đóng góp
     * @param amount Số tiền đóng góp (đơn vị: VND)
     * @param note Ghi chú cho giao dịch đóng góp (có thể null)
     * @param contributorId ID người đóng góp (có thể null, dùng mặc định từ session)
     */
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

    /**
     * Xóa thông báo (thành công hoặc lỗi) trong UI state.
     */
    fun clearMessage() {
        _uiState.update { it.copy(contributionMessage = null, error = null) }
    }
}
