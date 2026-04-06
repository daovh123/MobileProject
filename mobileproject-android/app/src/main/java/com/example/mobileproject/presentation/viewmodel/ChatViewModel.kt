package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.domain.entity.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isConnected: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var activeToken: String? = null

    private var isStarted: Boolean = false

    fun start(token: String) {
        if (isStarted) {
            return
        }
        isStarted = true
        activeToken = token

        if (token.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isConnected = false,
                    errorMessage = "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại.",
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, isConnected = false, errorMessage = null) }
        connectWebSocket(token)
    }

    fun send(token: String, text: String) {
        val trimmed = text.trim()
        if (token.isBlank() || trimmed.isBlank()) {
            return
        }

        val socket = webSocket
        if (socket == null) {
            _uiState.update { it.copy(errorMessage = "Chưa kết nối tới chat. Vui lòng thử lại.") }
            activeToken?.let { connectWebSocket(it) }
            return
        }

        _uiState.update { it.copy(isSending = true, errorMessage = null) }

        val payload = JSONObject()
            .put("type", "chat_message")
            .put("text", trimmed)
            .toString()

        val sent = runCatching { socket.send(payload) }.getOrDefault(false)
        if (!sent) {
            _uiState.update {
                it.copy(
                    isSending = false,
                    errorMessage = "Không thể gửi tin nhắn. Vui lòng thử lại.",
                )
            }
            return
        }

        _uiState.update { it.copy(isSending = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        reconnectJob?.cancel()
        reconnectJob = null
        closeWebSocket()
    }

    private fun connectWebSocket(token: String) {
        val trimmedToken = token.trim()
        if (trimmedToken.isBlank()) {
            return
        }

        closeWebSocket()

        val url = buildWebSocketUrl()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $trimmedToken")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                if (this@ChatViewModel.webSocket != webSocket) {
                    return
                }
                reconnectJob?.cancel()
                reconnectJob = null
                _uiState.update { it.copy(isLoading = false, isConnected = true, errorMessage = null) }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (this@ChatViewModel.webSocket != webSocket) {
                    return
                }
                handleIncoming(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (this@ChatViewModel.webSocket != webSocket) {
                    return
                }
                val message = mapWebSocketFailure(t, response)
                _uiState.update { it.copy(isLoading = false, isConnected = false, errorMessage = message) }

                val shouldReconnect = response == null || (response.code >= 500)
                if (shouldReconnect) {
                    scheduleReconnect()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (this@ChatViewModel.webSocket != webSocket) {
                    return
                }
                _uiState.update { it.copy(isConnected = false) }
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (reconnectJob != null) {
            return
        }

        val token = activeToken?.trim().orEmpty()
        if (token.isBlank()) {
            return
        }

        reconnectJob = viewModelScope.launch {
            delay(RECONNECT_DELAY_MS)
            reconnectJob = null
            if (isActive && isStarted) {
                _uiState.update { it.copy(isLoading = true) }
                connectWebSocket(token)
            }
        }
    }

    private fun closeWebSocket() {
        val socket = webSocket ?: return
        webSocket = null
        runCatching { socket.close(1000, null) }
    }

    private fun handleIncoming(text: String) {
        val obj = runCatching { JSONObject(text) }.getOrNull() ?: return
        when (obj.optString("type")) {
            "chat_history" -> {
                val messages = parseMessages(obj.optJSONArray("messages"))
                if (messages.isNotEmpty()) {
                    mergeMessages(messages)
                }
            }

            "chat_message" -> {
                val message = parseMessage(obj)
                if (message != null) {
                    mergeMessages(listOf(message))
                }
            }

            "chat_error" -> {
                val message = obj.optString("message")
                if (message.isNotBlank()) {
                    _uiState.update { it.copy(errorMessage = message) }
                }
            }
        }
    }

    private fun parseMessages(array: JSONArray?): List<ChatMessage> {
        if (array == null) {
            return emptyList()
        }

        val result = ArrayList<ChatMessage>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val msg = parseMessage(obj) ?: continue
            result.add(msg)
        }
        return result
    }

    private fun parseMessage(obj: JSONObject): ChatMessage? {
        val id = obj.optString("id").orEmpty().trim()
        if (id.isBlank()) {
            return null
        }
        return ChatMessage(
            id = id,
            text = obj.optString("text").orEmpty(),
            senderUsername = obj.optString("senderUsername").takeIf { it.isNotBlank() },
            mine = obj.optBoolean("mine", false),
            createdAt = obj.optString("createdAt").takeIf { it.isNotBlank() },
        )
    }

    private fun mergeMessages(incoming: List<ChatMessage>) {
        val merged = (_uiState.value.messages + incoming)
            .distinctBy { it.id }
            .sortedBy { it.createdAt.orEmpty() }

        _uiState.update { it.copy(messages = merged, errorMessage = null) }
    }

    private fun mapWebSocketFailure(t: Throwable, response: Response?): String {
        val code = response?.code
        return when (code) {
            401 -> "Phiên đăng nhập hết hạn hoặc token không hợp lệ. Vui lòng đăng nhập lại."
            403 -> "Bạn không có quyền truy cập phòng chat này."
            409 -> "Bạn chưa ghép đôi với partner để chat."
            else -> t.message?.takeIf { it.isNotBlank() } ?: "Không thể kết nối tới chat."
        }
    }

    private fun buildWebSocketUrl(): String {
        val base = BuildConfig.API_BASE_URL.trim().removeSuffix("/")

        val wsBase = when {
            base.startsWith("https://") -> "wss://" + base.removePrefix("https://")
            base.startsWith("http://") -> "ws://" + base.removePrefix("http://")
            base.startsWith("wss://") || base.startsWith("ws://") -> base
            else -> "ws://$base"
        }

        return "$wsBase/ws/chat"
    }

    companion object {
        private const val RECONNECT_DELAY_MS = 3000L
    }
}

