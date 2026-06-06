package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho màn hình Ghép đôi (Couple/Onboarding).
 *
 * Quản lý business logic:
 * - Tải trạng thái ghép đôi hiện tại (đã ghép, đang chờ, có lời mời)
 * - Gửi yêu cầu ghép đôi bằng mã code của đối phương
 * - Phản hồi lời mời ghép đôi (chấp nhận/từ chối)
 * - Polling trạng thái ghép đôi mỗi 3 giây để phát hiện thay đổi realtime
 *
 * Polling sử dụng [while] + [isActive] + [delay] thay vì Flow để đơn giản hơn
 * cho use case chỉ cần check định kỳ. Job được cancel trong [onCleared] để tránh leak.
 *
 * Sử dụng [runCatching] cho tất cả API call vì single-shot requests,
 * không cần Resource wrapper phức tạp.
 */
@HiltViewModel
class CoupleViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoupleUiState())
    val uiState: StateFlow<CoupleUiState> = _uiState.asStateFlow()

    /** Access token lưu tạm để dùng cho các API call trong session này. */
    private var token: String? = null
    /** Job polling trạng thái ghép đôi, cancel trong [onCleared]. */
    private var pollingJob: Job? = null

    /**
     * Tải trạng thái ghép đôi lần đầu khi màn hình mở.
     *
     * @param accessToken JWT access token
     */
    fun loadStatus(accessToken: String) {
        if (accessToken.isBlank()) return
        token = accessToken.trim()
        
        viewModelScope.launch {
            refreshStatus(showLoading = true)
        }
    }

    /**
     * Tải lại trạng thái ghép đôi từ API.
     *
     * @param showLoading True nếu cần hiển thị loading indicator (lần đầu),
     *   False nếu chỉ refresh ngầm (polling, sau action)
     */
    private suspend fun refreshStatus(showLoading: Boolean) {
        val accessToken = token ?: authSessionStore.load()?.token ?: return

        if (showLoading) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        }

        runCatching {
            onboardingRepository.getCoupleStatus(accessToken)
        }.onSuccess { status ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    profileCompleted = status.profileCompleted,
                    paired = status.paired,
                    partnerUsername = status.partnerUsername,
                    myCoupleCode = status.myCoupleCode,
                    myCoupleCodeExpiresAt = status.myCoupleCodeExpiresAt,
                    startAt = status.startAt,
                    daysTogether = status.daysTogether,
                    anniversaryTomorrow = status.anniversaryTomorrow,
                    coupleId = status.coupleId,
                    incomingRequestId = status.incomingRequestId,
                    incomingRequesterUsername = status.incomingRequesterUsername,
                    incomingRequesterDisplayName = status.incomingRequesterDisplayName,
                    outgoingStatus = status.outgoingStatus
                )
            }
        }.onFailure { throwable ->
            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Khong the tai trang thai ghep doi",
                )
            }
        }
    }

    /**
     * Xóa thông báo lỗi và info tạm thời.
     */
    fun clearTransientMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    /**
     * Gửi yêu cầu ghép đôi đến đối phương bằng mã couple code.
     * Sau khi gửi thành công, tự động refresh trạng thái.
     *
     * @param partnerCode Mã couple code của đối phương
     */
    fun sendCoupleRequest(partnerCode: String) {
        val accessToken = token ?: return
        if (partnerCode.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            runCatching {
                onboardingRepository.sendCoupleRequest(accessToken, partnerCode)
            }.onSuccess { action ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = action.message,
                        outgoingStatus = action.status
                    )
                }
                refreshStatus(false)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to send couple request"
                    )
                }
            }
        }
    }

    /**
     * Phản hồi lời mời ghép đôi đến (chấp nhận hoặc từ chối).
     * Sau khi phản hồi, tự động refresh trạng thái.
     *
     * @param requestId ID của lời mời ghép đôi
     * @param accept True để chấp nhận, False để từ chối
     */
    fun respondIncomingRequest(requestId: String, accept: Boolean) {
        val accessToken = token ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            runCatching {
                onboardingRepository.decideCoupleRequest(accessToken, requestId, accept)
            }.onSuccess { action ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = action.message,
                        incomingRequestId = if (accept) null else it.incomingRequestId
                    )
                }
                refreshStatus(false)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to respond to request"
                    )
                }
            }
        }
    }

    /**
     * Bắt đầu polling trạng thái ghép đôi mỗi 3 giây.
     * Dùng khi đang chờ đối phương phản hồi yêu cầu ghép đôi.
     * Tự động dừng khi ViewModel bị destroy ([onCleared]).
     *
     * @param accessToken JWT access token
     */
    fun startPolling(accessToken: String) {
        if (accessToken.isBlank()) return
        token = accessToken.trim()
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            // Polling loop: lặp vô hạn cho đến khi isActive = false (ViewModel bị hủy)
            while (isActive) {
                refreshStatus(showLoading = false) // Không hiển thị loading mỗi lần poll
                delay(3000L) // Chờ 3 giây giữa các lần poll
            }
        }
    }

    /**
     * Hủy polling job khi ViewModel bị destroy để tránh leak coroutine.
     */
    override fun onCleared() {
        // Hủy polling job để tránh leak coroutine khi ViewModel bị destroy
        pollingJob?.cancel()
        super.onCleared()
    }
}

/**
 * Trạng thái UI cho màn hình Ghép đôi.
 *
 * @property isLoading True khi đang tải hoặc chờ API
 * @property errorMessage Thông báo lỗi
 * @property infoMessage Thông báo thành công (vd: "Đã gửi yêu cầu")
 * @property profileCompleted True nếu người dùng đã hoàn thành hồ sơ
 * @property paired True nếu đã ghép đôi thành công
 * @property partnerUsername Username của đối phương (nếu đã ghép đôi)
 * @property myCoupleCode Mã couple code của người dùng (để chia sẻ cho đối phương)
 * @property myCoupleCodeExpiresAt Thời gian hết hạn mã couple code (ISO datetime)
 * @property startAt Ngày bắt đầu mối quan hệ (ISO date)
 * @property daysTogether Số ngày bên nhau
 * @property anniversaryTomorrow True nếu ngày mai là kỷ niệm
 * @property coupleId ID của cặp đôi (null nếu chưa ghép)
 * @property incomingRequestId ID lời mời ghép đôi đang chờ phản hồi
 * @property incomingRequesterUsername Username người gửi lời mời
 * @property incomingRequesterDisplayName Tên hiển thị người gửi lời mời
 * @property outgoingStatus Trạng thái yêu cầu đã gửi ("PENDING", "ACCEPTED", null)
 */
data class CoupleUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val profileCompleted: Boolean = false,
    val paired: Boolean = false,
    val partnerUsername: String? = null,
    val myCoupleCode: String? = null,
    val myCoupleCodeExpiresAt: String? = null,
    val startAt: String? = null,
    val daysTogether: Long? = null,
    val anniversaryTomorrow: Boolean? = null,
    val coupleId: String? = null,
    val incomingRequestId: String? = null,
    val incomingRequesterUsername: String? = null,
    val incomingRequesterDisplayName: String? = null,
    val outgoingStatus: String? = null
)
