package com.example.mobileproject.utils

import android.os.Build
import android.util.Log
import java.net.URI

object ApiBaseUrlResolver {

    private const val TAG = "ApiBaseUrlResolver"
    private const val EMULATOR_HOST = "10.0.2.2"
    private val lanIpv4Regex = Regex("192\\.168\\.\\d+\\.\\d+")

    fun resolveHttpBase(rawBaseUrl: String): String {
        val raw = rawBaseUrl.trim().removeSurrounding("\"")
        val isEmulator = isProbablyAnEmulator()

        var resolved = runCatching {
            rewriteWithUri(raw, isEmulator)
        }.getOrElse {
            rewriteWithStringFallback(raw, isEmulator)
        }

        if (!resolved.endsWith("/")) {
            resolved += "/"
        }

        Log.i(TAG, "HTTP base raw='$raw' resolved='$resolved' emulator=$isEmulator")
        return resolved
    }

    fun resolveWebSocketBase(rawBaseUrl: String): String {
        val base = resolveHttpBase(rawBaseUrl).removeSuffix("/")
        return when {
            base.startsWith("https://") -> "wss://" + base.removePrefix("https://")
            base.startsWith("http://") -> "ws://" + base.removePrefix("http://")
            base.startsWith("wss://") || base.startsWith("ws://") -> base
            else -> "ws://$base"
        }
    }

    private fun rewriteWithUri(url: String, isEmulator: Boolean): String {
        val uri = URI(url)
        val host = uri.host?.lowercase()
        val rewrittenHost = when {
            host == null -> null
            host == "localhost" || host == "127.0.0.1" || host == "::1" -> EMULATOR_HOST
            isEmulator && host.matches(lanIpv4Regex) -> EMULATOR_HOST
            else -> null
        } ?: return url

        val port = if (uri.port == -1) -1 else uri.port
        return URI(uri.scheme, uri.userInfo, rewrittenHost, port, uri.path, uri.query, uri.fragment)
            .toString()
    }

    private fun rewriteWithStringFallback(url: String, isEmulator: Boolean): String {
        var rewritten = url
        rewritten = rewritten
            .replace("http://localhost", "http://$EMULATOR_HOST", ignoreCase = true)
            .replace("https://localhost", "https://$EMULATOR_HOST", ignoreCase = true)
            .replace("localhost", EMULATOR_HOST, ignoreCase = true)
            .replace("127.0.0.1", EMULATOR_HOST)
            .replace("[::1]", EMULATOR_HOST)

        if (isEmulator) {
            rewritten = rewritten.replace(lanIpv4Regex, EMULATOR_HOST)
        }

        return rewritten
    }

    private fun isProbablyAnEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            Build.BRAND.startsWith("generic") ||
            Build.DEVICE.startsWith("generic") ||
            Build.PRODUCT.contains("sdk") ||
            Build.HARDWARE.contains("ranchu")
    }
}
