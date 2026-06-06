package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.UserSettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel phục vụ màn hình cài đặt người dùng.
 *
 * Xử lý business logic:
 * - Quản lý cài đặt thông báo đẩy (push notifications)
 * - Quản lý cài đặt thông báo email
 * - Quản lý hiển thị trạng thái hoạt động
 * - Quản lý cho phép tìm kiếm theo email
 *
 * Tích hợp [UserSettingsStore] (DataStore) để lưu trữ cài đặt.
 * Tất cả preference được expose dưới dạng StateFlow sử dụng pattern
 * stateIn với WhileSubscribed(5_000) - giữ cache 5 giây sau khi
 * subscriber cuối ngừng collect, tối ưu bộ nhớ.
 */
@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val store: UserSettingsStore,
) : ViewModel() {

    // DataStore integration pattern: chuyển Flow từ DataStore thành StateFlow
    // stateIn với WhileSubscribed(5_000) giữ cache 5 giây, tránh re-subscribe không cần thiết
    val pushNotifications: StateFlow<Boolean> = store.pushNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val emailNotifications: StateFlow<Boolean> = store.emailNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val showActivityStatus: StateFlow<Boolean> = store.showActivityStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val searchableByEmail: StateFlow<Boolean> = store.searchableByEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /** Bật/tắt thông báo đẩy. @param v true = bật, false = tắt */
    fun setPushNotifications(v: Boolean) = viewModelScope.launch { store.setPushNotifications(v) }
    /** Bật/tắt thông báo email. @param v true = bật, false = tắt */
    fun setEmailNotifications(v: Boolean) = viewModelScope.launch { store.setEmailNotifications(v) }
    /** Bật/tắt hiển thị trạng thái hoạt động. @param v true = hiển thị, false = ẩn */
    fun setShowActivityStatus(v: Boolean) = viewModelScope.launch { store.setShowActivityStatus(v) }
    /** Bật/tắt cho phép tìm kiếm theo email. @param v true = cho phép, false = không cho phép */
    fun setSearchableByEmail(v: Boolean) = viewModelScope.launch { store.setSearchableByEmail(v) }
}
