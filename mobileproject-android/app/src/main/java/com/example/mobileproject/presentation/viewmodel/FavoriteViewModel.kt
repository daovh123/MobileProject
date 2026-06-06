package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state cho màn hình danh sách yêu thích.
 *
 * @property isLoading Đang tải dữ liệu từ server
 * @property favorites Danh sách địa điểm yêu thích
 * @property errorMessage Thông báo lỗi nếu có
 * @property favoriteIds Set chứa ID các địa điểm đã được đánh dấu yêu thích (dùng cho quick lookup)
 * @property toggleInProgress Set chứa ID các địa điểm đang trong quá trình toggle (tránh double-click)
 */
data class FavoriteUiState(
    val isLoading: Boolean = false,
    val favorites: List<Place> = emptyList(),
    val errorMessage: String? = null,
    val favoriteIds: Set<String> = emptySet(),
    val toggleInProgress: Set<String> = emptySet(),
)

/**
 * ViewModel phục vụ màn hình quản lý danh sách địa điểm yêu thích.
 *
 * Xử lý business logic:
 * - Tải danh sách địa điểm yêu thích từ [FavoriteRepository]
 * - Toggle yêu thích/bỏ yêu thích một địa điểm
 * - Kiểm tra trạng thái yêu thích của một địa điểm cụ thể
 * - Theo dõi trạng thái đang toggle để disable UI, tránh gọi API trùng lặp
 *
 * Optimistic update: cập nhật favoriteIds và favorites ngay trong UI
 * sau khi API toggle thành công, không cần reload toàn bộ danh sách.
 */
@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    // StateFlow pattern: MutableStateFlow nội bộ + expose read-only asStateFlow
    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    /**
     * Tải danh sách địa điểm yêu thích từ server.
     *
     * Kiểm tra token trước khi gọi API. Cập nhật cả favorites list
     * và favoriteIds set để UI có thể lookup nhanh bằng ID.
     *
     * @param token JWT token xác thực người dùng
     */
    fun loadFavorites(token: String) {
        if (token.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    favorites = emptyList(),
                    favoriteIds = emptySet(),
                    errorMessage = "Phien dang nhap het han, vui long dang nhap lai",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            favoriteRepository.getFavorites(token)
                .onSuccess { favorites ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            favorites = favorites,
                            favoriteIds = favorites.map { place -> place.id }.toSet(),
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Khong the tai danh sach yeu thich",
                        )
                    }
                }
        }
    }

    /**
     * Toggle trạng thái yêu thích của một địa điểm.
     *
     * Thêm placeId vào toggleInProgress trước khi gọi API để disable UI.
     * Sau khi API trả về, cập nhật favoriteIds (thêm/bớt) và favorites list
     * (loại bỏ nếu bỏ yêu thích) ngay lập tức.
     * Nếu lỗi, chỉ xóa placeId khỏi toggleInProgress.
     *
     * @param token JWT token xác thực người dùng
     * @param placeId ID địa điểm cần toggle yêu thích
     */
    fun toggleFavorite(token: String, placeId: String) {
        if (token.isBlank() || placeId.isBlank()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(toggleInProgress = it.toggleInProgress + placeId) }

            favoriteRepository.toggleFavorite(token, placeId)
                .onSuccess { isNowFavorite ->
                    _uiState.update { current ->
                        val newIds = if (isNowFavorite) {
                            current.favoriteIds + placeId
                        } else {
                            current.favoriteIds - placeId
                        }

                        val newFavorites = when {
                            isNowFavorite -> current.favorites
                            else -> current.favorites.filterNot { it.id == placeId }
                        }

                        current.copy(
                            favoriteIds = newIds,
                            favorites = newFavorites,
                            toggleInProgress = current.toggleInProgress - placeId,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            toggleInProgress = current.toggleInProgress - placeId,
                            errorMessage = throwable.message ?: "Khong the cap nhat yeu thich",
                        )
                    }
                }
        }
    }

    /**
     * Kiểm tra một địa điểm có nằm trong danh sách yêu thích hay không.
     *
     * Cập nhật favoriteIds trong UI state dựa trên kết quả từ server.
     *
     * @param token JWT token xác thực người dùng
     * @param placeId ID địa điểm cần kiểm tra
     */
    fun checkFavorite(token: String, placeId: String) {
        if (token.isBlank() || placeId.isBlank()) {
            return
        }

        viewModelScope.launch {
            favoriteRepository.checkFavorite(token, placeId)
                .onSuccess { isFavorite ->
                    _uiState.update { current ->
                        val newIds = if (isFavorite) {
                            current.favoriteIds + placeId
                        } else {
                            current.favoriteIds - placeId
                        }
                        current.copy(favoriteIds = newIds)
                    }
                }
        }
    }
}
