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

/**
 * Trạng thái gửi của tin nhắn đang chờ xác nhận từ server.
 * - [SENDING]: Đang chờ server xác nhận
 * - [FAILED]: Gửi thất bại (timeout hoặc socket error)
 */
enum class SendStatus { SENDING, FAILED }

/**
 * Tin nhắn đang chờ server xác nhận (optimistic UI).
 *
 * @property localId UUID duy nhất để theo dõi tin nhắn trên client
 * @property text Nội dung tin nhắn
 * @property status Trạng thái gửi hiện tại
 */
data class PendingMessage(
    val localId: String,
    val text: String,
    val status: SendStatus,
)

/**
 * Trạng thái UI cho màn hình Chat thời gian thực.
 *
 * @property isLoading True khi đang kết nối WebSocket lần đầu
 * @property isSending True khi đang gửi tin nhắn (reserved, hiện dùng pendingMessages)
 * @property isConnected True khi WebSocket đã kết nối thành công
 * @property messages Danh sách tin nhắn đã nhận (sắp xếp theo thời gian)
 * @property pendingMessages Tin nhắn đang chờ server xác nhận (optimistic UI)
 * @property errorMessage Thông báo lỗi kết nối/gửi tin nhắn
 * @property isPartnerTyping True khi đối phương đang nhập (hiển thị "đang nhập...")
 * @property replyingToMessage Tin nhắn đang được reply (null nếu không reply)
 * @property partnerName Tên đối phương (lấy từ chat_history message)
 * @property partnerAvatarUrl Avatar URL đối phương (lấy từ chat_history message)
 */
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

/**
 * ViewModel cho màn hình Chat thời gian thực giữa cặp đôi.
 *
 * Quản lý business logic:
 * - Kết nối WebSocket để nhắn tin realtime
 * - Gửi/nhận tin nhắn qua WebSocket protocol (JSON)
 * - Hiển thị optimistic UI: tin nhắn xuất hiện ngay khi gửi, đánh dấu FAILED nếu timeout
 * - Xử lý typing indicator (đang nhập) với debounce
 * - Reply tin nhắn (trả lời một tin nhắn cụ thể)
 * - Tự động kết nối lại khi mất kết nối (reconnect với delay)
 * - Xử lý chat_history khi mới kết nối (lịch sử tin nhắn)
 *
 * WebSocket lifecycle:
 * 1. [start] -> [connectWebSocket] -> onOpen -> isConnected = true
 * 2. onMessage -> [handleIncoming] (parse JSON theo type)
 * 3. onFailure/onClosed -> [scheduleReconnect] (delay [RECONNECT_DELAY_MS])
 * 4. [onCleared] -> [closeWebSocket] + cancel reconnect
 *
 * Tin nhắn pending sử dụng timeout [SEND_TIMEOUT_MS] để đánh dấu FAILED
 * nếu server không xác nhận trong thời gian quy định.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /** OkHttpClient dùng cho WebSocket, không cần cấu hình đặc biệt. */
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    /** WebSocket instance hiện tại (null nếu chưa kết nối hoặc đã đóng). */
    private var webSocket: WebSocket? = null
    /** Job kết nối lại tự động khi mất kết nối. */
    private var reconnectJob: Job? = null
    /** Token hiện tại để reconnect tự động. */
    private var activeToken: String? = null

    /** Đánh dấu đã gọi [start] để tránh duplicate connection. */
    private var isStarted: Boolean = false

    /** Map các job timeout cho pending messages, keyed by localId. */
    private val pendingTimeoutJobs = mutableMapOf<String, Job>()
    /** Job debounce gửi typing stop event. */
    private var typingStopJob: Job? = null

    /**
     * Khởi tạo kết nối WebSocket cho chat.
     * Chỉ gọi một lần (guarded by [isStarted]).
     *
     * @param token JWT access token để authenticate WebSocket handshake
     */
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

    /**
     * Đặt tin nhắn đang được reply.
     *
     * @param message Tin nhắn cần reply (null để hủy reply)
     */
    fun setReplyTo(message: ChatMessage?) {
        _uiState.update { it.copy(replyingToMessage = message) }
    }

    /**
     * Gửi tin nhắn qua WebSocket.
     * Sử dụng optimistic UI: tin nhắn hiển thị ngay với trạng thái SENDING,
     * server xác nhận thì xóa khỏi pending, timeout thì đánh dấu FAILED.
     *
     * @param token JWT access token (hiện không dùng cho WebSocket send, chỉ validate)
     * @param text Nội dung tin nhắn
     * @param replyToId ID tin nhắn đang reply (nullable)
     */
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

    /**
     * Gửi lại tin nhắn đã thất bại.
     * Reset trạng thái về SENDING và gửi lại qua WebSocket.
     *
     * @param localId ID cục bộ của tin nhắn pending cần retry
     */
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

    /**
     * Lên lịch timeout cho tin nhắn pending.
     * Sau [SEND_TIMEOUT_MS] mà không nhận được xác nhận -> đánh dấu FAILED.
     */
    private fun scheduleSendTimeout(localId: String) {
        pendingTimeoutJobs[localId]?.cancel()
        pendingTimeoutJobs[localId] = viewModelScope.launch {
            delay(SEND_TIMEOUT_MS)
            markPendingFailed(localId)
        }
    }

    /**
     * Đánh dấu một tin nhắn pending là FAILED và hủy timeout job.
     */
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

    /**
     * Xác nhận tin nhắn đã được server nhận.
     * Tìm pending message theo text (vì server trả về text, không trả localId),
     * hủy timeout và xóa khỏi danh sách pending.
     */
    private fun confirmPendingSent(text: String) {
        val match = _uiState.value.pendingMessages
            .firstOrNull { it.status == SendStatus.SENDING && it.text == text }
            ?: return
        pendingTimeoutJobs.remove(match.localId)?.cancel()
        _uiState.update {
            it.copy(pendingMessages = it.pendingMessages.filter { p -> p.localId != match.localId })
        }
    }

    /**
     * Gửi sự kiện "đang nhập" qua WebSocket.
     * Sử dụng debounce: sau [TYPING_DEBOUNCE_MS] tự động gửi "ngừng nhập".
     *
     * @param token JWT access token
     */
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

    /** Xóa thông báo lỗi. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Hủy tất cả kết nối và job khi ViewModel bị destroy.
     */
    override fun onCleared() {
        super.onCleared()
        reconnectJob?.cancel()
        reconnectJob = null
        closeWebSocket()
    }

    /**
     * Tạo kết nối WebSocket mới.
     * Đóng kết nối cũ trước (closeWebSocket).
     * Authentication qua header Authorization: Bearer {token}.
     *
     * @param token JWT access token
     */
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

    /**
     * Lên lịch kết nối lại sau [RECONNECT_DELAY_MS].
     * Chỉ kết nối lại nếu server lỗi (>= 500) hoặc mất kết nối mạng.
     * Không kết nối lại nếu bị 401/403/409 (lỗi client).
     */
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

    /**
     * Đóng WebSocket hiện tại một cách graceful (code 1000).
     */
    private fun closeWebSocket() {
        val socket = webSocket ?: return
        webSocket = null
        runCatching { socket.close(1000, null) }
    }

    /**
     * Xử lý tin nhắn JSON đến từ WebSocket.
     *
     * Các loại message:
     * - "chat_history": Lịch sử tin nhắn + thông tin partner (khi mới kết nối)
     * - "chat_message": Tin nhắn mới (từ mình hoặc đối phương)
     * - "chat_error": Lừ server
     * - "typing": Đối phương đang nhập
     */
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

    /**
     * Parse mảng JSON messages thành danh sách ChatMessage.
     * Bỏ qua message không parse được (id blank).
     */
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

    /**
     * Parse một JSONObject thành ChatMessage.
     * @return ChatMessage hoặc null nếu id blank
     */
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

    /**
     * Gộp tin nhắn mới vào danh sách hiện có.
     * Loại bỏ trùng lặp theo [ChatMessage.id] và sắp xếp theo thời gian tăng dần.
     */
    private fun mergeMessages(incoming: List<ChatMessage>) {
        val merged = (_uiState.value.messages + incoming)
            .distinctBy { it.id }
            .sortedBy { it.createdAt.orEmpty() }

        _uiState.update { it.copy(messages = merged, errorMessage = null) }
    }

    /**
     * Ánh xạ lỗi WebSocket thành thông báo tiếng Việt thân thiện.
     * Dựa trên HTTP status code của response.
     */
    private fun mapWebSocketFailure(t: Throwable, response: Response?): String {
        val code = response?.code
        return when (code) {
            401 -> "Phiên đăng nhập hết hạn hoặc token không hợp lệ. Vui lòng đăng nhập lại."
            403 -> "Bạn không có quyền truy cập phòng chat này."
            409 -> "Bạn chưa ghép đôi với partner để chat."
            else -> t.message?.takeIf { it.isNotBlank() } ?: "Không thể kết nối tới chat."
        }
    }

    /**
     * Build URL WebSocket từ API base URL.
     * Chuyển đổi http/https -> ws/wss qua [ApiBaseUrlResolver].
     */
    private fun buildWebSocketUrl(): String {
        val wsBase = ApiBaseUrlResolver.resolveWebSocketBase(BuildConfig.API_BASE_URL)

        return "$wsBase/ws/chat"
    }

    companion object {
        /** Thời gian chờ trước khi kết nối lại WebSocket (3 giây). */
        private const val RECONNECT_DELAY_MS = 3000L
        /** Timeout cho tin nhắn pending: nếu server không xác nhận trong 5 giây -> FAILED. */
        const val SEND_TIMEOUT_MS = 5000L
        /** Debounce thời gian gửi typing stop event (1 giây sau lần nhập cuối). */
        private const val TYPING_DEBOUNCE_MS = 1000L
    }
}

