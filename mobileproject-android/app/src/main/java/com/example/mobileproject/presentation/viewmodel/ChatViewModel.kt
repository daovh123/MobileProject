package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.domain.entity.ChatMessage
import com.example.mobileproject.utils.ApiBaseUrlResolver
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
import java.util.UUID
import javax.inject.Inject

enum class SendStatus { SENDING, FAILED }

data class PendingMessage(
    val localId: String,
    val text: String,
    val status: SendStatus,
)

data class ChatUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isConnected: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val pendingMessages: List<PendingMessage> = emptyList(),
    val errorMessage: String? = null,
    val isPartnerTyping: Boolean = false,
    val replyingToMessage: ChatMessage? = null,
    val partnerName: String? = null,
    val partnerAvatarUrl: String? = null,
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

    private val pendingTimeoutJobs = mutableMapOf<String, Job>()
    private var typingStopJob: Job? = null

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

    fun setReplyTo(message: ChatMessage?) {
        _uiState.update { it.copy(replyingToMessage = message) }
    }

    fun send(token: String, text: String, replyToId: String? = null) {
        val trimmed = text.trim()
        if (token.isBlank() || trimmed.isBlank()) {
            return
        }

        val localId = UUID.randomUUID().toString()
        val pending = PendingMessage(localId = localId, text = trimmed, status = SendStatus.SENDING)
        _uiState.update { it.copy(pendingMessages = it.pendingMessages + pending, errorMessage = null, replyingToMessage = null) }

        val socket = webSocket
        if (socket == null) {
            markPendingFailed(localId)
            activeToken?.let { connectWebSocket(it) }
            return
        }

        val payload = JSONObject()
            .put("type", "chat_message")
            .put("text", trimmed)
            .apply { if (replyToId != null) put("replyToId", replyToId) }
            .toString()

        val sent = runCatching { socket.send(payload) }.getOrDefault(false)
        if (!sent) {
            markPendingFailed(localId)
            return
        }

        scheduleSendTimeout(localId)
    }

    fun retry(localId: String) {
        val pending = _uiState.value.pendingMessages.find { it.localId == localId } ?: return
        _uiState.update {
            it.copy(
                pendingMessages = it.pendingMessages.map { p ->
                    if (p.localId == localId) p.copy(status = SendStatus.SENDING) else p
                },
            )
        }

        val socket = webSocket
        if (socket == null) {
            markPendingFailed(localId)
            activeToken?.let { connectWebSocket(it) }
            return
        }

        val payload = JSONObject()
            .put("type", "chat_message")
            .put("text", pending.text)
            .toString()

        val sent = runCatching { socket.send(payload) }.getOrDefault(false)
        if (!sent) {
            markPendingFailed(localId)
            return
        }

        scheduleSendTimeout(localId)
    }

    private fun scheduleSendTimeout(localId: String) {
        pendingTimeoutJobs[localId]?.cancel()
        pendingTimeoutJobs[localId] = viewModelScope.launch {
            delay(SEND_TIMEOUT_MS)
            markPendingFailed(localId)
        }
    }

    private fun markPendingFailed(localId: String) {
        pendingTimeoutJobs.remove(localId)?.cancel()
        _uiState.update {
            it.copy(
                pendingMessages = it.pendingMessages.map { p ->
                    if (p.localId == localId) p.copy(status = SendStatus.FAILED) else p
                },
            )
        }
    }

    private fun confirmPendingSent(text: String) {
        val match = _uiState.value.pendingMessages
            .firstOrNull { it.status == SendStatus.SENDING && it.text == text }
            ?: return
        pendingTimeoutJobs.remove(match.localId)?.cancel()
        _uiState.update {
            it.copy(pendingMessages = it.pendingMessages.filter { p -> p.localId != match.localId })
        }
    }

    fun sendTypingEvent(token: String) {
        val socket = webSocket ?: return
        runCatching {
            socket.send(JSONObject().put("type", "typing").put("typing", true).toString())
        }
        typingStopJob?.cancel()
        typingStopJob = viewModelScope.launch {
            delay(TYPING_DEBOUNCE_MS)
            runCatching {
                socket.send(JSONObject().put("type", "typing").put("typing", false).toString())
            }
        }
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
                val partnerName = obj.optString("partnerName").takeIf { it.isNotBlank() }
                    ?: obj.optString("partnerUsername").takeIf { it.isNotBlank() }
                val partnerAvatarUrl = obj.optString("partnerAvatarUrl").takeIf { it.isNotBlank() }
                if (partnerName != null || partnerAvatarUrl != null) {
                    _uiState.update { it.copy(partnerName = partnerName, partnerAvatarUrl = partnerAvatarUrl) }
                }
                if (messages.isNotEmpty()) {
                    mergeMessages(messages)
                }
            }

            "chat_message" -> {
                val message = parseMessage(obj)
                if (message != null) {
                    if (message.mine) confirmPendingSent(message.text)
                    mergeMessages(listOf(message))
                }
            }

            "chat_error" -> {
                val message = obj.optString("message")
                if (message.isNotBlank()) {
                    _uiState.update { it.copy(errorMessage = message) }
                }
            }

            "typing" -> {
                val isTyping = obj.optBoolean("isTyping", false)
                _uiState.update { it.copy(isPartnerTyping = isTyping) }
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
            senderAvatarUrl = obj.optString("senderAvatarUrl").takeIf { it.isNotBlank() },
            replyToId = obj.optString("replyToId").takeIf { it.isNotBlank() },
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
        val wsBase = ApiBaseUrlResolver.resolveWebSocketBase(BuildConfig.API_BASE_URL)

        return "$wsBase/ws/chat"
    }

    companion object {
        private const val RECONNECT_DELAY_MS = 3000L
        const val SEND_TIMEOUT_MS = 5000L
        private const val TYPING_DEBOUNCE_MS = 1000L
    }
}

