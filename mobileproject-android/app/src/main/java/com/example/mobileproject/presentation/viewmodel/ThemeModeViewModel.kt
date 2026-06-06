package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.ThemeModeStore
import com.example.mobileproject.presentation.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel phục vụ màn hình cài đặt chế độ giao diện (theme).
 *
 * Xử lý business logic:
 * - Đọc và quản lý chế độ giao diện hiện tại (Sáng/Tối/Theo hệ thống)
 * - Lưu trữ cài đặt theme qua [ThemeModeStore] (DataStore)
 *
 * Pattern: stateIn với WhileSubscribed(5_000) chuyển Flow từ DataStore
 * thành StateFlow, giữ cache 5 giây sau khi subscriber cuối ngừng collect.
 * Giá trị mặc định là ThemeMode.SYSTEM.
 */
@HiltViewModel
class ThemeModeViewModel @Inject constructor(
    private val themeModeStore: ThemeModeStore,
) : ViewModel() {

    // DataStore integration pattern: chuyển Flow từ DataStore thành StateFlow
    // WhileSubscribed(5_000) giữ cache 5 giây, tránh re-subscribe khi rotate screen
    val themeMode: StateFlow<ThemeMode> = themeModeStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    /**
     * Thay đổi chế độ giao diện.
     *
     * @param mode Chế độ giao diện mới: [ThemeMode.LIGHT], [ThemeMode.DARK], hoặc [ThemeMode.SYSTEM]
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeModeStore.setThemeMode(mode)
        }
    }
}
