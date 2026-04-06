package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.ProfileResult
import com.example.mobileproject.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(token: String) {
        if (token.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, coupleError = null) }

            val profileDeferred = async {
                runCatching { onboardingRepository.getProfile(token) }
            }
            val coupleDeferred = async {
                runCatching { onboardingRepository.getCoupleStatus(token) }
            }

            val profileResult = profileDeferred.await()
            val coupleResult = coupleDeferred.await()

            profileResult.onSuccess { profile ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        savedProfile = profile,
                        initialProfile = profile,
                        fullName = profile.fullName ?: "",
                        nickName = profile.nickName ?: "",
                        birthDate = profile.birthDate ?: "",
                        gender = profile.gender ?: "",
                        isDirty = false,
                    )
                }
            }.onFailure { error ->
                val sessionExpired = error is IllegalStateException &&
                    error.message?.contains("Unauthorized", ignoreCase = true) == true ||
                    error.message?.contains("expired", ignoreCase = true) == true ||
                    error.message?.contains("401", ignoreCase = true) == true
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sessionExpired = sessionExpired,
                        errorMessage = if (sessionExpired) null else (error.message ?: "Tai ho so that bai"),
                    )
                }
            }

            coupleResult.onSuccess { status ->
                _uiState.update { it.copy(isLoadingCouple = false, coupleStatus = status) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingCouple = false, coupleError = error.message) }
            }
        }
    }

    fun updateDraft(fullName: String, nickName: String, birthDate: String, gender: String) {
        val initial = _uiState.value.initialProfile
        _uiState.update { state ->
            state.copy(
                fullName = fullName,
                nickName = nickName,
                birthDate = birthDate,
                gender = gender,
                isDirty = fullName.trim() != (initial?.fullName?.trim() ?: "") ||
                    nickName.trim() != (initial?.nickName?.trim() ?: "") ||
                    birthDate != (initial?.birthDate ?: "") ||
                    gender != (initial?.gender ?: ""),
            )
        }
    }

    fun saveProfile(token: String) {
        if (token.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }

        val state = _uiState.value
        val fullName = state.fullName
        val nickName = state.nickName
        val birthDate = state.birthDate
        val gender = state.gender

        // Validation
        if (fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ho ten khong duoc de trong") }
            return
        }
        if (fullName.trim().length > 100) {
            _uiState.update { it.copy(errorMessage = "Ho ten qua 100 ky tu") }
            return
        }
        if (nickName.isNotBlank() && nickName.trim().length > 50) {
            _uiState.update { it.copy(errorMessage = "Bi danh qua 50 ky tu") }
            return
        }
        if (!birthDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            _uiState.update { it.copy(errorMessage = "Ngay sinh khong hop le") }
            return
        }
        val parsedDate = runCatching { java.time.LocalDate.parse(birthDate) }.getOrNull()
        if (parsedDate == null) {
            _uiState.update { it.copy(errorMessage = "Ngay sinh khong hop le") }
            return
        }
        if (parsedDate.isAfter(java.time.LocalDate.now())) {
            _uiState.update { it.copy(errorMessage = "Ngay sinh khong duoc o tuong lai") }
            return
        }
        if (gender !in listOf("MALE", "FEMALE", "OTHER")) {
            _uiState.update { it.copy(errorMessage = "Gioi tinh khong hop le") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                onboardingRepository.saveProfile(
                    token = token,
                    fullName = fullName.trim(),
                    nickName = nickName.trim().takeIf { it.isNotBlank() },
                    birthDate = birthDate.trim(),
                    gender = gender.trim(),
                )
            }.onSuccess { profile ->
                authSessionStore.updateProfileState(
                    profileCompleted = profile.profileCompleted,
                    coupleConnected = profile.coupleConnected,
                )
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedProfile = profile,
                        initialProfile = profile,
                        isDirty = false,
                        saveSuccessMessage = "Luu ho so thanh cong",
                    )
                }
            }.onFailure { error ->
                val sessionExpired = error is IllegalStateException &&
                    error.message?.contains("Unauthorized", ignoreCase = true) == true ||
                    error.message?.contains("expired", ignoreCase = true) == true ||
                    error.message?.contains("401", ignoreCase = true) == true
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        sessionExpired = sessionExpired,
                        errorMessage = if (sessionExpired) null else (error.message ?: "Luu ho so that bai"),
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, coupleError = null) }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccessMessage = null) }
    }

    fun clearSessionExpired() {
        _uiState.update { it.copy(sessionExpired = false) }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedProfile: ProfileResult? = null,
    val initialProfile: ProfileResult? = null,
    val fullName: String = "",
    val nickName: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val isDirty: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccessMessage: String? = null,
    val sessionExpired: Boolean = false,
    val isLoadingCouple: Boolean = false,
    val coupleStatus: CoupleStatus? = null,
    val coupleError: String? = null,
)
