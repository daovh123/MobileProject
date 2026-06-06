package com.example.mobileproject.utils

import android.os.Build
import android.util.Log
import java.net.URI

/**
 * Resolves the API base URL at runtime so the app works correctly in three environments:
 *   1. **Localhost / emulator** – rewrites `localhost`, `127.0.0.1`, `[::1]`, or LAN IPs
 *      to the Android emulator's host alias `10.0.2.2`.
 *   2. **Real device on same LAN** – keeps the original LAN IP intact so the phone can
 *      reach the dev server over Wi-Fi.
 *   3. **Production** – returns the URL as-is (e.g. `https://api.example.com`).
 *
 * Also provides [resolveWebSocketBase] for upgrading HTTP(S) schemes to WS/WSS.
 *
 * Used by [NetworkModule.provideRetrofit] with the value of [BuildConfig.API_BASE_URL].
 */
object ApiBaseUrlResolver {

    private const val TAG = "ApiBaseUrlResolver"

    /** Android emulator loopback alias that maps to the host machine's localhost. */
    private const val EMULATOR_HOST = "10.0.2.2"

    /** Regex matching typical private LAN addresses (192.168.x.x). */
    private val lanIpv4Regex = Regex("192\\.168\\.\\d+\\.\\d+")

    /**
     * Resolves and normalizes the HTTP base URL:
     * - Trims whitespace and surrounding quotes from the raw value.
     * - Attempts a proper [java.net.URI]-based rewrite first; falls back to string replacement
     *   if the URL is malformed.
     * - Ensures the result always ends with `/` (required by Retrofit).
     *
     * @param rawBaseUrl the raw URL string (typically from [BuildConfig.API_BASE_URL]).
     * @return a normalized HTTP base URL safe for [Retrofit.Builder.baseUrl].
     */
    fun resolveHttpBase(rawBaseUrl: String): String {
        val raw = rawBaseUrl.trim().removeSurrounding("\"")
        val isEmulator = isProbablyAnEmulator()

        // Try the structured URI rewrite first; fall back to string replacement on parse failure.
        var resolved = runCatching {
            rewriteWithUri(raw, isEmulator)
        }.getOrElse {
            rewriteWithStringFallback(raw, isEmulator)
        }

        // Retrofit requires the base URL to end with a trailing slash.
        if (!resolved.endsWith("/")) {
            resolved += "/"
        }

        Log.i(TAG, "HTTP base raw='$raw' resolved='$resolved' emulator=$isEmulator")
        return resolved
    }

    /**
     * Converts an HTTP base URL to its WebSocket equivalent:
     * `http://` → `ws://`, `https://` → `wss://`. Passes through existing WS/WSS URLs unchanged.
     *
     * @param rawBaseUrl the raw URL string.
     * @return a WebSocket URL suitable for OkHttp or STOMP client connections.
     */
    fun resolveWebSocketBase(rawBaseUrl: String): String {
        val base = resolveHttpBase(rawBaseUrl).removeSuffix("/")
        return when {
            base.startsWith("https://") -> "wss://" + base.removePrefix("https://")
            base.startsWith("http://") -> "ws://" + base.removePrefix("http://")
            base.startsWith("wss://") || base.startsWith("ws://") -> base
            else -> "ws://$base"
        }
    }

    /**
     * Structured rewrite using [java.net.URI]. Replaces localhost/loopback hosts with the
     * emulator alias. On emulators, also rewrites LAN IPs (192.168.x.x) so the emulator
     * can reach the host machine.
     *
     * @return the rewritten URL, or the original if no rewrite was needed.
     */
    private fun rewriteWithUri(url: String, isEmulator: Boolean): String {
        val uri = URI(url)
        val host = uri.host?.lowercase()
        val rewrittenHost = when {
            host == null -> null
            // Loopback addresses always map to the emulator host gateway.
            host == "localhost" || host == "127.0.0.1" || host == "::1" -> EMULATOR_HOST
            // On emulators, LAN IPs also need rewriting since the emulator has its own network.
            isEmulator && host.matches(lanIpv4Regex) -> EMULATOR_HOST
            else -> null
        } ?: return url  // No rewrite needed; return original.

        val port = if (uri.port == -1) -1 else uri.port
        return URI(uri.scheme, uri.userInfo, rewrittenHost, port, uri.path, uri.query, uri.fragment)
            .toString()
    }

    /**
     * Fallback string-based rewrite for URLs that fail [java.net.URI] parsing.
     * Uses simple find-and-replace for localhost and loopback addresses.
     */
    private fun rewriteWithStringFallback(url: String, isEmulator: Boolean): String {
        var rewritten = url
        rewritten = rewritten
            .replace("http://localhost", "http://$EMULATOR_HOST", ignoreCase = true)
            .replace("https://localhost", "https://$EMULATOR_HOST", ignoreCase = true)
            .replace("localhost", EMULATOR_HOST, ignoreCase = true)
            .replace("127.0.0.1", EMULATOR_HOST)
            .replace("[::1]", EMULATOR_HOST)

        // On emulators, replace LAN IPs with the emulator host alias.
        if (isEmulator) {
            rewritten = rewritten.replace(lanIpv4Regex, EMULATOR_HOST)
        }

        return rewritten
    }

    /**
     * Heuristic check using [Build] properties to detect if the app is running on an
     * Android emulator (AVD, Genymotion, or SDK images). Covers common emulator fingerprints
     * without requiring additional libraries.
     */
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
