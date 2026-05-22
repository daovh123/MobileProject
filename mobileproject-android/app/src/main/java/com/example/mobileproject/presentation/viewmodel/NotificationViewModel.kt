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

data class NotificationUiState(
    val notifications: List<AppNotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val hasNextPage: Boolean = false,
    val currentPage: Int = 0,
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
    val notifPrefs: NotificationPreferencesStore,
) : ViewModel() {
    companion object {
        private const val MIN_REFRESH_INTERVAL_MS = 2_500L
    }

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // Expose preferences as StateFlows
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

    private var lastRefreshAtMs: Long = 0L

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

    fun loadNextPage() {
        loadNotifications(reset = false)
    }

    fun refresh() {
        val now = System.currentTimeMillis()
        if (_uiState.value.isLoading) return
        if (now - lastRefreshAtMs < MIN_REFRESH_INTERVAL_MS) return
        lastRefreshAtMs = now
        loadUnreadCount()
        loadNotifications(reset = true)
    }

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

    fun refreshUnreadCount() = loadUnreadCount()

    // Preference setters
    fun setNotifChat(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifChat(v) }
    fun setNotifPayment(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifPayment(v) }
    fun setNotifTransaction(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifTransaction(v) }
    fun setNotifGoal(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifGoal(v) }
    fun setNotifMemory(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifMemory(v) }
}
