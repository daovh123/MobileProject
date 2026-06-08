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
    val errorMessage: String? = null,
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
    val notifPrefs: NotificationPreferencesStore,
) : ViewModel() {

    private companion object {
        const val NOTIFICATION_STALE_MS: Long = 45_000L
        const val UNREAD_STALE_MS: Long = 20_000L
        val HIDDEN_NOTIFICATION_TYPES = setOf("CHAT_MESSAGE", "CHAT_MESSENGE")
    }

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    private var lastNotificationsLoadedAt: Long = 0L
    private var lastUnreadLoadedAt: Long = 0L

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

    init {
        ensureLoaded()
    }

    fun ensureLoaded() {
        val state = _uiState.value
        if (state.notifications.isEmpty() && lastNotificationsLoadedAt == 0L) {
            loadNotifications(reset = true, force = true, showLoading = true)
        } else {
            refreshIfStale()
        }
        if (lastUnreadLoadedAt == 0L || isUnreadStale()) {
            loadUnreadCount(force = state.unreadCount == 0)
        }
    }

    fun refreshIfStale() {
        if (isUnreadStale()) {
            loadUnreadCount(force = false)
        }
        if (isNotificationsStale()) {
            loadNotifications(
                reset = true,
                force = false,
                showLoading = _uiState.value.notifications.isEmpty(),
            )
        }
    }

    fun loadUnreadCount(force: Boolean = false) {
        val token = authSessionStore.load()?.token ?: return
        if (!force && !isUnreadStale()) return
        viewModelScope.launch {
            runCatching {
                val resp = apiService.getUnreadNotificationCount("Bearer $token")
                if (resp.isSuccessful) {
                    val count = resp.body()?.count?.toInt() ?: 0
                    lastUnreadLoadedAt = System.currentTimeMillis()
                    _uiState.update { it.copy(unreadCount = count) }
                }
            }
        }
    }

    fun loadNotifications(
        reset: Boolean = false,
        force: Boolean = false,
        showLoading: Boolean = true,
    ) {
        val token = authSessionStore.load()?.token ?: return
        val page = if (reset) 0 else _uiState.value.currentPage + 1
        if (!reset && !_uiState.value.hasNextPage) return
        if (reset && !force && !isNotificationsStale() && _uiState.value.notifications.isNotEmpty()) return
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(errorMessage = null) }
            }
            runCatching {
                var nextPage = page
                var loadedPage = page
                var hasNextPage = false
                var foundVisibleNotification = false
                var newList: List<AppNotificationDto> = if (reset) {
                    emptyList()
                } else {
                    _uiState.value.notifications
                }

                while (true) {
                    val requestedPage = nextPage
                    val resp = apiService.getNotifications("Bearer $token", requestedPage)
                    if (!resp.isSuccessful) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi tải thông báo (${resp.code()})") }
                        return@runCatching
                    }

                    val pageData = resp.body()
                    if (pageData == null) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Dữ liệu trả về trống") }
                        return@runCatching
                    }

                    lastNotificationsLoadedAt = System.currentTimeMillis()
                    val visibleNotifications = pageData.content
                        .filterNot { isHiddenNotificationType(it.type) }
                    if (visibleNotifications.isNotEmpty()) {
                        newList = newList + visibleNotifications
                        foundVisibleNotification = true
                    }

                    loadedPage = pageData.currentPage
                    hasNextPage = pageData.hasNext

                    if (foundVisibleNotification || !hasNextPage) {
                        break
                    }

                    nextPage = requestedPage + 1
                }

                _uiState.update {
                    it.copy(
                        notifications = newList,
                        hasNextPage = hasNextPage,
                        currentPage = loadedPage,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { e ->
                _uiState.update { s -> s.copy(isLoading = false, errorMessage = e.message ?: "Không thể tải thông báo") }
            }
        }
    }

    fun loadNextPage() {
        loadNotifications(reset = false, force = true, showLoading = true)
    }

    fun refresh(force: Boolean = false) {
        loadUnreadCount(force = force || _uiState.value.unreadCount == 0)
        loadNotifications(
            reset = true,
            force = force,
            showLoading = force && _uiState.value.notifications.isEmpty(),
        )
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

    fun refreshUnreadCount() = loadUnreadCount(force = true)

    // Preference setters
    fun setNotifChat(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifChat(v) }
    fun setNotifPayment(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifPayment(v) }
    fun setNotifTransaction(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifTransaction(v) }
    fun setNotifGoal(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifGoal(v) }
    fun setNotifMemory(v: Boolean) = viewModelScope.launch { notifPrefs.setNotifMemory(v) }

    private fun isNotificationsStale(now: Long = System.currentTimeMillis()): Boolean {
        return now - lastNotificationsLoadedAt >= NOTIFICATION_STALE_MS
    }

    private fun isUnreadStale(now: Long = System.currentTimeMillis()): Boolean {
        return now - lastUnreadLoadedAt >= UNREAD_STALE_MS
    }

    private fun isHiddenNotificationType(type: String): Boolean {
        return type.trim().uppercase() in HIDDEN_NOTIFICATION_TYPES
    }
}
