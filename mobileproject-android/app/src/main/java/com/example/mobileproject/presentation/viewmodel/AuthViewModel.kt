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

/**
 * ViewModel cho màn hình Xác thực (Authentication), bao gồm Đăng nhập, Đăng ký và Đăng xuất.
 *
 * Quản lý business logic:
 * - Xác thực người dùng bằng username/email + password
 * - Đăng ký tài khoản mới
 * - Đăng xuất và xóa session local
 * - Quản lý trạng thái loading, lỗi và kết quả xác thực
 *
 * Sử dụng [runCatching] để bắt exception từ use case, giúp code gọn hơn try/catch
 * và tránh crash khi network hoặc server lỗi.
 *
 * UI state được expose dưới dạng [StateFlow] để Compose quan sát và recompose tự động.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Đăng nhập bằng username hoặc email.
     *
     * @param usernameOrEmail Tên đăng nhập hoặc email của người dùng
     * @param password Mật khẩu
     *
     * Flow: Validate input -> Hiển thị loading -> Gọi [LoginUseCase] -> Cập nhật UI state với kết quả
     */
    fun login(usernameOrEmail: String, password: String) {
        if (usernameOrEmail.isBlank() || password.isBlank()) {
            // Dùng .update {} để chỉ thay đổi errorMessage, giữ nguyên các field khác
            _uiState.update { it.copy(errorMessage = "Vui long nhap day du thong tin") }
            return
        }

        viewModelScope.launch {
            // Dùng .value = thay vì .update {} vì reset toàn bộ state về isLoading
            _uiState.value = AuthUiState(isLoading = true)
            // runCatching wrap try-catch gọn hơn, trả Result<T>
            runCatching { loginUseCase(usernameOrEmail.trim(), password) }
                .onSuccess { _uiState.value = AuthUiState(authSession = it) }
                .onFailure {
                    _uiState.value = AuthUiState(errorMessage = it.message ?: "Dang nhap that bai")
                }
        }
    }

    /**
     * Đăng ký tài khoản mới.
     *
     * @param username Tên đăng nhập
     * @param email Địa chỉ email
     * @param password Mật khẩu
     *
     * Flow: Validate input -> Hiển thị loading -> Gọi [RegisterUseCase] -> Cập nhật UI state
     */
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

    /**
     * Đăng xuất người dùng hiện tại.
     *
     * @param token Access token hiện tại để gọi API logout
     *
     * Ngay cả khi API logout thất bại (token hết hạn/hỏng), vẫn đánh dấu logoutCompleted = true
     * vì session local đã được xóa và UI cần chuyển về màn hình Login.
     */
    fun logout(token: String) {
        val normalizedToken = token.trim()
        if (normalizedToken.isBlank()) {
            _uiState.value = AuthUiState(logoutCompleted = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            runCatching { logoutUseCase(normalizedToken) }
                .onSuccess {
                    _uiState.value = AuthUiState(logoutCompleted = true)
                }
                .onFailure {
                    // Backend token có thể đã hết hạn, nhưng session local vẫn cần xóa.
                    // Vẫn đánh dấu logoutCompleted để UI luôn quay về Login.
                    _uiState.value = AuthUiState(logoutCompleted = true)
                }
        }
    }

    /**
     * Xóa thông báo lỗi hiện tại khỏi UI state.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Đánh dấu đã xử lý kết quả xác thực thành công.
     * Gọi sau khi UI đã điều hướng để tránh re-trigger navigation khi recompose.
     */
    fun consumeAuthSuccess() {
        _uiState.update { it.copy(authSession = null) }
    }

    /**
     * Đánh dấu đã xử lý kết quả đăng xuất thành công.
     * Gọi sau khi UI đã điều hướng về màn hình Login.
     */
    fun consumeLogoutSuccess() {
        _uiState.update { it.copy(logoutCompleted = false) }
    }
}

/**
 * Trạng thái UI cho màn hình Xác thực (Đăng nhập / Đăng ký / Đăng xuất).
 *
 * @property isLoading True khi đang chờ API trả về, UI hiển thị loading indicator
 * @property errorMessage Thông báo lỗi (nếu có) để hiển thị Snackbar/Toast. Null nếu không có lỗi
 * @property authSession Kết quả xác thực thành công chứa token và thông tin session.
 *   UI quan sát field này để điều hướng sang màn hình chính. Null nếu chưa đăng nhập hoặc đã consume
 * @property logoutCompleted True khi đăng xuất hoàn tất, UI quan sát để điều hướng về màn hình Login
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authSession: AuthSession? = null,
    val logoutCompleted: Boolean = false,
)
