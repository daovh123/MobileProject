package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.ProfileResult
import com.example.mobileproject.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun saveProfile(
        token: String,
        fullName: String,
        nickName: String?,
        birthDate: String,
        gender: String,
    ) {
        if (token.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }
        if (fullName.isBlank() || birthDate.isBlank() || gender.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui long nhap day du thong tin") }
            return
        }

        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)
            runCatching {
                onboardingRepository.saveProfile(
                    token = token,
                    fullName = fullName.trim(),
                    nickName = nickName?.trim()?.takeIf { it.isNotBlank() },
                    birthDate = birthDate.trim(),
                    gender = gender.trim(),
                )
            }.onSuccess { profile ->
                authSessionStore.updateProfileState(
                    profileCompleted = profile.profileCompleted,
                    coupleConnected = profile.coupleConnected,
                )
                _uiState.value = ProfileUiState(savedProfile = profile)
            }.onFailure {
                _uiState.value = ProfileUiState(errorMessage = it.message ?: "Luu ho so that bai")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(savedProfile = null) }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val savedProfile: ProfileResult? = null,
)
