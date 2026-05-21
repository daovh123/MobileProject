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

@HiltViewModel
class CoupleViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoupleUiState())
    val uiState: StateFlow<CoupleUiState> = _uiState.asStateFlow()

    private var token: String? = null
    private var pollingJob: Job? = null

    fun loadStatus(accessToken: String) {
        if (accessToken.isBlank()) return
        token = accessToken.trim()
        
        viewModelScope.launch {
            refreshStatus(showLoading = true)
        }
    }

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

    fun clearTransientMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

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

    fun startPolling(accessToken: String) {
        if (accessToken.isBlank()) return
        token = accessToken.trim()
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                refreshStatus(showLoading = false)
                delay(3000L)
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
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
    val startAt: String? = null,
    val daysTogether: Long? = null,
    val anniversaryTomorrow: Boolean? = null,
    val coupleId: String? = null,
    val incomingRequestId: String? = null,
    val incomingRequesterUsername: String? = null,
    val incomingRequesterDisplayName: String? = null,
    val outgoingStatus: String? = null
)
