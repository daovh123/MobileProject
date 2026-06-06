package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.local.NotificationPreferencesStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.notification.AppNotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state cho màn hình thông báo.
 *
 * @property notifications Danh sách thông báo hiện tại (hỗ trợ phân trang)
 * @property unreadCount Số lượng thông báo chưa đọc
 * @property isLoading Đang tải dữ liệu từ server
 * @property.hasNextPage Còn trang tiếp theo để load thêm (infinite scroll)
 * @property currentPage Trang hiện tại đã tải (dùng cho phân trang)
 */
data class NotificationUiState(
    val notifications: List<AppNotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val hasNextPage: Boolean = false,
    val currentPage: Int = 0,
)

/**
 * ViewModel phục vụ màn hình quản lý thông báo.
 *
 * Xử lý business logic:
 * - Tải và hiển thị danh sách thông báo với phân trang (infinite scroll)
 * - Đếm số thông báo chưa đọc
 * - Đánh dấu đã đọc từng thông báo hoặc tất cả
 * - Quản lý cài đặt bật/tắt thông báo theo loại (chat, thanh toán, giao dịch, mục tiêu, kỷ niệm)
 *
 * Tích hợp [NotificationPreferencesStore] để lưu trữ cài đặt thông báo qua DataStore.
 * Các preference được expose dưới dạng StateFlow sử dụng pattern stateIn(WhileSubscribed).
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
    val notifPrefs: NotificationPreferencesStore,
) : ViewModel() {

    // StateFlow pattern: MutableStateFlow nội bộ + expose read-only asStateFlow
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // DataStore integration pattern: chuyển Flow từ DataStore thành StateFlow
    // stateIn với WhileSubscribed(5_000) giữ cache 5 giây sau khi subscriber cuối ngừng collect
    val notifChat: StateFlow<Boolean> = notifPrefs.notifChat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val notifPayment: StateFlow<Boolean> = notifPrefs.notifPayment
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val notifTransaction: StateFlow<Boolean> = notifPrefs.notifTransaction
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val notifGoal: StateFlow<Boolean> = notifPrefs.notifGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val notifMemory: StateFlow<Boolean> = notifPrefs.notifMemory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    init {
        loadUnreadCount()
        loadNotifications(reset = true)
    }

    /**
     * Tải số lượng thông báo chưa đọc từ server.
     *
     * Gọi API getUnreadNotificationCount và cập nhật unreadCount trong UI state.
     * Sử dụng runCatching để bắt lỗi mạng mà không crash app.
     */
    fun loadUnreadCount() {
        val token = authSessionStore.load()?.token ?: return
        viewModelScope.launch {
            runCatching {
                val resp = apiService.getUnreadNotificationCount("Bearer $token")
                if (resp.isSuccessful) {
                    val count = resp.body()?.count?.toInt() ?: 0
                    _uiState.update { it.copy(unreadCount = count) }
                }
            }
        }
    }

    /**
     * Tải danh sách thông báo từ server (hỗ trợ phân trang).
     *
     * @param reset true = tải lại từ trang đầu (refresh), false = tải trang tiếp theo
     *
     * Error handling: sử dụng runCatching + onFailure để bắt exception,
     * chỉ cập nhật isLoading = false mà không crash app.
     */
    fun loadNotifications(reset: Boolean = false) {
        val token = authSessionStore.load()?.token ?: return
        val page = if (reset) 0 else _uiState.value.currentPage + 1
        if (!reset && !_uiState.value.hasNextPage) return
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val resp = apiService.getNotifications("Bearer $token", page)
                if (resp.isSuccessful) {
                    val pageData = resp.body()
                    if (pageData != null) {
                        val newList = if (reset) pageData.content
                        else _uiState.value.notifications + pageData.content
                        _uiState.update {
                            it.copy(
                                notifications = newList,
                                hasNextPage = pageData.hasNext,
                                currentPage = pageData.currentPage,
                                isLoading = false,
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }.onFailure {
                _uiState.update { s -> s.copy(isLoading = false) }
            }
        }
    }

    /**
     * Tải trang tiếp theo của danh sách thông báo (infinite scroll).
     */
    fun loadNextPage() {
        loadNotifications(reset = false)
    }

    /**
     * Làm mới toàn bộ: tải lại số chưa đọc + danh sách từ trang đầu.
     */
    fun refresh() {
        loadUnreadCount()
        loadNotifications(reset = true)
    }

    /**
     * Đánh dấu tất cả thông báo là đã đọc.
     *
     * Optimistic update: cập nhật UI ngay lập tức (unreadCount = 0, tất cả read = true)
     * trước khi đợi server phản hồi. Nếu API lỗi, UI vẫn giữ trạng thái đã cập nhật.
     */
    fun markAllRead() {
        val token = authSessionStore.load()?.token ?: return
        viewModelScope.launch {
            runCatching {
                apiService.markAllNotificationsRead("Bearer $token")
            }
            _uiState.update { state ->
                state.copy(
                    unreadCount = 0,
                    notifications = state.notifications.map { it.copy(read = true) },
                )
            }
        }
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     *
     * Optimistic update: cập nhật UI ngay (đặt read = true, giảm unreadCount)
     * trước khi đợi server phản hồi.
     *
     * @param notificationId ID của thông báo cần đánh dấu đã đọc
     */
    fun markRead(notificationId: String) {
        val token = authSessionStore.load()?.token ?: return
        viewModelScope.launch {
            runCatching {
                apiService.markNotificationRead("Bearer $token", notificationId)
            }
            _uiState.update { state ->
                val updated = state.notifications.map {
                    if (it.id == notificationId) it.copy(read = true) else it
                }
                val newCount = (state.unreadCount - 1).coerceAtLeast(0)
                state.copy(notifications = updated, unreadCount = newCount)
            }
        }
    }

    /** Làm mới số thông báo chưa đọc (alias của [loadUnreadCount]). */
    fun refreshUnreadCount() = loadUnreadCount()

    // Preference setters - lưu cài đặt thông báo qua DataStore
    /** Bật/tắt thông báo tin nhắn chat. */
    fun setNotifChat(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifChat(v) }
    /** Bật/tắt thông báo thanh toán. */
    fun setNotifPayment(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifPayment(v) }
    /** Bật/tắt thông báo giao dịch. */
    fun setNotifTransaction(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifTransaction(v) }
    /** Bật/tắt thông báo mục tiêu tiết kiệm. */
    fun setNotifGoal(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifGoal(v) }
    /** Bật/tắt thông báo kỷ niệm. */
    fun setNotifMemory(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifMemory(v) }
}
