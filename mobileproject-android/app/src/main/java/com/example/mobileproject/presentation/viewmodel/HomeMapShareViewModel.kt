package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.HomeMapSharingStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state cho màn hình cài đặt chia sẻ bản đồ.
 *
 * @property shareLocationEnabled Trạng thái bật/tắt chia sẻ vị trí trên bản đồ
 */
data class HomeMapShareUiState(
    val shareLocationEnabled: Boolean = false,
)

/**
 * ViewModel phục vụ màn hình cài đặt chia sẻ bản đồ (Home Map Sharing).
 *
 * Xử lý business logic:
 * - Quản lý trạng thái bật/tắt chia sẻ vị trí trên bản đồ chung
 * - Đọc và ghi trạng thái chia sẻ thông qua [HomeMapSharingStore] (DataStore)
 *
 * Pattern: collect Flow từ DataStore trong init block để đồng bộ trạng thái
 * vào MutableStateFlow, sau đó expose read-only StateFlow cho UI.
 */
@HiltViewModel
class HomeMapShareViewModel @Inject constructor(
    private val homeMapSharingStore: HomeMapSharingStore,
) : ViewModel() {

    // StateFlow pattern: collect từ DataStore Flow → cập nhật MutableStateFlow
    private val _uiState = MutableStateFlow(HomeMapShareUiState())
    val uiState: StateFlow<HomeMapShareUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            homeMapSharingStore.shareLocationEnabled.collect { enabled ->
                _uiState.update { it.copy(shareLocationEnabled = enabled) }
            }
        }
    }

    /**
     * Bật/tắt tính năng chia sẻ vị trí trên bản đồ.
     *
     * @param enabled true = bật chia sẻ, false = tắt chia sẻ
     */
    fun setShareLocationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            homeMapSharingStore.setShareLocationEnabled(enabled)
        }
    }
}
