package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state cho màn hình lịch sử xem địa điểm.
 *
 * @property isLoading Đang tải dữ liệu từ server
 * @property history Danh sách địa điểm đã xem gần đây
 * @property errorMessage Thông báo lỗi nếu có
 */
data class HistoryUiState(
    val isLoading: Boolean = false,
    val history: List<Place> = emptyList(),
    val errorMessage: String? = null,
)

/**
 * ViewModel phục vụ màn hình lịch sử xem địa điểm.
 *
 * Xử lý business logic:
 * - Tải danh sách địa điểm đã xem gần đây từ [HistoryRepository]
 * - Ghi nhận lượt xem địa điểm (record view) mỗi khi người dùng xem chi tiết
 * - Xóa toàn bộ lịch sử xem
 *
 * Tất cả các thao tác đều yêu cầu token xác thực hợp lệ.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    // StateFlow pattern: MutableStateFlow nội bộ + expose read-only asStateFlow
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    /**
     * Tải danh sách lịch sử xem địa điểm từ server.
     *
     * Kiểm tra token trước khi gọi API. Nếu token rỗng, hiển thị thông báo
     * yêu cầu đăng nhập lại.
     *
     * @param token JWT token xác thực người dùng
     */
    fun loadHistory(token: String) {
        if (token.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    history = emptyList(),
                    errorMessage = "Phien dang nhap het han, vui long dang nhap lai",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            historyRepository.getHistory(token)
                .onSuccess { history ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            history = history,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Khong the tai lich su",
                        )
                    }
                }
        }
    }

    /**
     * Ghi nhận lượt xem một địa điểm.
     *
     * Fire-and-forget: không cập nhật UI state, chỉ gọi API trong background.
     *
     * @param token JWT token xác thực người dùng
     * @param placeId ID địa điểm đã xem
     */
    fun recordView(token: String, placeId: String) {
        if (token.isBlank() || placeId.isBlank()) {
            return
        }

        viewModelScope.launch {
            historyRepository.recordView(token, placeId)
        }
    }

    /**
     * Xóa toàn bộ lịch sử xem địa điểm.
     *
     * Sau khi xóa thành công, đặt danh sách history về rỗng.
     *
     * @param token JWT token xác thực người dùng
     */
    fun clearHistory(token: String) {
        if (token.isBlank()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            historyRepository.clearHistory(token)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            history = emptyList(),
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Khong the xoa lich su",
                        )
                    }
                }
        }
    }
}
