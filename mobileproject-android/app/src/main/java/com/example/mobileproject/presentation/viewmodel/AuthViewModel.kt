package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.usecase.LoginUseCase
import com.example.mobileproject.domain.usecase.LogoutUseCase
import com.example.mobileproject.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(usernameOrEmail: String, password: String) {
        if (usernameOrEmail.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui long nhap day du thong tin") }
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            runCatching { loginUseCase(usernameOrEmail.trim(), password) }
                .onSuccess { _uiState.value = AuthUiState(authSession = it) }
                .onFailure {
                    _uiState.value = AuthUiState(errorMessage = it.message ?: "Dang nhap that bai")
                }
        }
    }

    fun register(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui long nhap day du thong tin") }
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            runCatching { registerUseCase(username.trim(), email.trim(), password) }
                .onSuccess { _uiState.value = AuthUiState(authSession = it) }
                .onFailure {
                    _uiState.value = AuthUiState(errorMessage = it.message ?: "Dang ky that bai")
                }
        }
    }

    fun logout(token: String) {
        if (token.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            runCatching { logoutUseCase(token.trim()) }
                .onSuccess {
                    _uiState.value = AuthUiState(logoutCompleted = true)
                }
                .onFailure {
                    _uiState.value = AuthUiState(errorMessage = it.message ?: "Dang xuat that bai")
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeAuthSuccess() {
        _uiState.update { it.copy(authSession = null) }
    }

    fun consumeLogoutSuccess() {
        _uiState.update { it.copy(logoutCompleted = false) }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authSession: AuthSession? = null,
    val logoutCompleted: Boolean = false,
)
