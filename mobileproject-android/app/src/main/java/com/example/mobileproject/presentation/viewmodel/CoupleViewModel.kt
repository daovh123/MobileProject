package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.domain.entity.CoupleStatus
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

@HiltViewModel
class CoupleViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoupleUiState())
    val uiState: StateFlow<CoupleUiState> = _uiState.asStateFlow()

    private var token: String? = null
    private var pollingJob: Job? = null

    fun startPolling(accessToken: String) {
        if (accessToken.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }

        token = accessToken.trim()
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            refreshStatus(showLoading = true)
            while (isActive) {
                delay(POLLING_INTERVAL_MS)
                refreshStatus(showLoading = false)
            }
        }
    }

    fun loadStatus(accessToken: String) {
        if (accessToken.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }

        token = accessToken.trim()
        pollingJob?.cancel()
        pollingJob = null

        viewModelScope.launch {
            refreshStatus(showLoading = true)
        }
    }

    fun sendCoupleRequest(partnerCode: String) {
        val accessToken = token
        if (accessToken.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }
        if (partnerCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui long nhap ma doi tac") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                onboardingRepository.sendCoupleRequest(accessToken, partnerCode.trim())
            }.onSuccess { action ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = action.message,
                        errorMessage = null,
                    )
                }
                refreshStatus(showLoading = false)
            }.onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Gui loi moi ghep doi that bai",
                    )
                }
            }
        }
    }

    fun respondIncomingRequest(requestId: String, accept: Boolean) {
        val accessToken = token
        if (accessToken.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }
        if (requestId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Khong tim thay yeu cau ghep doi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                onboardingRepository.decideCoupleRequest(accessToken, requestId, accept)
            }.onSuccess { action ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = action.message,
                        errorMessage = null,
                    )
                }
                refreshStatus(showLoading = false)
            }.onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Xu ly yeu cau ghep doi that bai",
                    )
                }
            }
        }
    }

    fun clearTransientMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun refreshStatus(showLoading: Boolean) {
        val accessToken = token ?: return

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
                    incomingRequestId = status.incomingRequestId,
                    incomingRequesterUsername = status.incomingRequesterUsername,
                    incomingRequesterDisplayName = status.incomingRequesterDisplayName,
                    startAt = status.startAt,
                    daysTogether = status.daysTogether,
                    anniversaryTomorrow = status.anniversaryTomorrow,
                    outgoingRequestId = status.outgoingRequestId,
                    outgoingRecipientUsername = status.outgoingRecipientUsername,
                    outgoingStatus = status.outgoingStatus,
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

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }

    private companion object {
        const val POLLING_INTERVAL_MS: Long = 3000L
    }
}

data class CoupleUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val profileCompleted: Boolean = false,
    val paired: Boolean = false,
    val partnerUsername: String? = null,
    val myCoupleCode: String? = null,
    val myCoupleCodeExpiresAt: String? = null,
    val incomingRequestId: String? = null,
    val incomingRequesterUsername: String? = null,
    val incomingRequesterDisplayName: String? = null,
    val startAt: String? = null,
    val daysTogether: Long? = null,
    val anniversaryTomorrow: Boolean? = null,
    val outgoingRequestId: String? = null,
    val outgoingRecipientUsername: String? = null,
    val outgoingStatus: String? = null,
)
