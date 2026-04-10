package com.example.mobileproject.presentation.notification

import com.example.mobileproject.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object ChatQuickReplySender {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    suspend fun send(accessToken: String, text: String): Boolean {
        val token = accessToken.trim()
        val trimmed = text.trim()
        if (token.isBlank() || trimmed.isBlank()) {
            return false
        }

        return suspendCancellableCoroutine { continuation ->
            val request = Request.Builder()
                .url(buildWebSocketUrl())
                .header("Authorization", "Bearer $token")
                .build()

            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    val payload = JSONObject()
                        .put("type", "chat_message")
                        .put("text", trimmed)
                        .toString()

                    val sent = runCatching { webSocket.send(payload) }.getOrDefault(false)
                    runCatching { webSocket.close(1000, null) }

                    if (continuation.isActive) {
                        continuation.resume(sent)
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }
            }

            val webSocket = okHttpClient.newWebSocket(request, listener)
            continuation.invokeOnCancellation {
                runCatching { webSocket.cancel() }
            }
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
}
