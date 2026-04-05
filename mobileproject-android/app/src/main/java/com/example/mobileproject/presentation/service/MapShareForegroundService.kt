package com.example.mobileproject.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MapShareForegroundService : Service() {

    private val serviceJob: Job = SupervisorJob()
    private val serviceScope: CoroutineScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null

    private var accessToken: String? = null
    private var coupleId: String? = null

    private var pendingLatitude: Double? = null
    private var pendingLongitude: Double? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val token = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty().trim()
                val coupleId = intent.getStringExtra(EXTRA_COUPLE_ID).orEmpty().trim()
                if (token.isBlank() || coupleId.isBlank()) {
                    stopSelf()
                    return START_NOT_STICKY
                }

                accessToken = token
                this.coupleId = coupleId

                startInForeground()
                startLocationUpdates()
                connectWebSocket()

                return START_STICKY
            }

            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopLocationUpdates()
        closeWebSocket()

        reconnectJob?.cancel()
        serviceJob.cancel()

        runCatching { okHttpClient.dispatcher.executorService.shutdown() }
        runCatching { okHttpClient.connectionPool.evictAll() }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startInForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location_24)
            .setContentTitle(getString(R.string.map_share_title))
            .setContentText(getString(R.string.map_share_notification_text))
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    private fun startLocationUpdates() {
        if (locationCallback != null) {
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MS,
        )
            .setMinUpdateIntervalMillis(LOCATION_INTERVAL_MS)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                val lat = location.latitude
                val lng = location.longitude

                pendingLatitude = lat
                pendingLongitude = lng

                broadcastLocation(ACTION_MY_LOCATION, lat, lng)
                sendLocation(lat, lng)
            }
        }

        locationCallback = callback

        runCatching {
            fusedLocationClient.requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper(),
            )
        }.onFailure {
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        val callback = locationCallback ?: return
        locationCallback = null
        runCatching { fusedLocationClient.removeLocationUpdates(callback) }
    }

    private fun connectWebSocket() {
        val token = accessToken?.trim().orEmpty()
        val coupleId = coupleId?.trim().orEmpty()
        if (token.isBlank() || coupleId.isBlank()) {
            return
        }

        closeWebSocket()

        val url = buildWebSocketUrl(coupleId)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectJob?.cancel()
                reconnectJob = null

                val lat = pendingLatitude
                val lng = pendingLongitude
                if (lat != null && lng != null) {
                    sendLocation(lat, lng)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncoming(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (reconnectJob != null) {
            return
        }

        reconnectJob = serviceScope.launch {
            delay(RECONNECT_DELAY_MS)
            reconnectJob = null
            connectWebSocket()
        }
    }

    private fun closeWebSocket() {
        val socket = webSocket ?: return
        webSocket = null
        runCatching { socket.close(1000, null) }
    }

    private fun sendLocation(latitude: Double, longitude: Double) {
        val socket = webSocket ?: return

        val payload = JSONObject()
            .put("type", "location")
            .put("latitude", latitude)
            .put("longitude", longitude)
            .toString()

        runCatching { socket.send(payload) }
    }

    private fun handleIncoming(text: String) {
        val obj = runCatching { JSONObject(text) }.getOrNull() ?: return
        when (obj.optString("type")) {
            "partner_location" -> {
                val lat = obj.optDouble("latitude", Double.NaN)
                val lng = obj.optDouble("longitude", Double.NaN)
                if (!lat.isNaN() && !lng.isNaN()) {
                    broadcastLocation(ACTION_PARTNER_LOCATION, lat, lng)
                }
            }
        }
    }

    private fun broadcastLocation(action: String, latitude: Double, longitude: Double) {
        val intent = Intent(action)
            .setPackage(packageName)
            .putExtra(EXTRA_LATITUDE, latitude)
            .putExtra(EXTRA_LONGITUDE, longitude)
        sendBroadcast(intent)
    }

    private fun buildWebSocketUrl(coupleId: String): String {
        val base = BuildConfig.API_BASE_URL.trim().removeSuffix("/")

        val wsBase = when {
            base.startsWith("https://") -> "wss://" + base.removePrefix("https://")
            base.startsWith("http://") -> "ws://" + base.removePrefix("http://")
            base.startsWith("wss://") || base.startsWith("ws://") -> base
            else -> "ws://$base"
        }

        return "$wsBase/ws/map/share/$coupleId"
    }

    companion object {
        private const val CHANNEL_ID = "map_share"
        private const val CHANNEL_NAME = "Map Share"
        private const val NOTIFICATION_ID = 9001

        private const val LOCATION_INTERVAL_MS = 3000L
        private const val RECONNECT_DELAY_MS = 3000L

        private const val ACTION_BASE = "com.example.mobileproject.mapshare"

        const val ACTION_START: String = "$ACTION_BASE.START"
        const val ACTION_STOP: String = "$ACTION_BASE.STOP"

        const val ACTION_MY_LOCATION: String = "$ACTION_BASE.MY_LOCATION"
        const val ACTION_PARTNER_LOCATION: String = "$ACTION_BASE.PARTNER_LOCATION"

        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
        const val EXTRA_COUPLE_ID: String = "extra_couple_id"

        const val EXTRA_LATITUDE: String = "extra_latitude"
        const val EXTRA_LONGITUDE: String = "extra_longitude"

        fun start(context: Context, accessToken: String, coupleId: String) {
            val intent = Intent(context, MapShareForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ACCESS_TOKEN, accessToken)
                putExtra(EXTRA_COUPLE_ID, coupleId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MapShareForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            runCatching { context.startService(intent) }
        }
    }
}
