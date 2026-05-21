package com.example.mobileproject.presentation.ui.screen.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import dagger.hilt.android.AndroidEntryPoint
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.notification.FcmTokenRequestDto
import com.example.mobileproject.presentation.ui.screen.login.LoginActivity
import com.example.mobileproject.presentation.ui.screen.app.compose.HomeRoutes
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
import com.example.mobileproject.presentation.ui.theme.resolveDarkTheme
import com.example.mobileproject.presentation.viewmodel.ThemeModeViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) {
            Log.w("ChatFCM", "POST_NOTIFICATIONS permission denied")
        }
    }

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    @Inject
    lateinit var apiService: ApiService

    private var navController: NavHostController? = null
    private var accessToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        if (accessToken.isBlank()) {
            accessToken = authSessionStore.load()?.token.orEmpty()
        }

        if (accessToken.isBlank()) {
            val loginIntent = Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(EXTRA_OPEN_CHAT, intent.getBooleanExtra(EXTRA_OPEN_CHAT, false))
            }
            startActivity(loginIntent)
            finish()
            return
        }

        requestNotificationPermissionIfNeeded()
        syncFcmToken(accessToken)

        setContent {
            val themeViewModel: ThemeModeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val darkTheme = themeMode.resolveDarkTheme(isSystemInDarkTheme())

            MobileProjectTheme(darkTheme = darkTheme) {
                com.example.mobileproject.presentation.ui.screen.app.compose.HomeScaffold(
                    accessToken = accessToken,
                    apiService = apiService,
                    onNavControllerReady = { controller ->
                        navController = controller
                        maybeOpenChatFromIntent(intent)
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        maybeOpenChatFromIntent(intent)
    }

    private fun maybeOpenChatFromIntent(intent: Intent?) {
        val controller = navController ?: return
        if (intent?.getBooleanExtra(EXTRA_OPEN_CHAT, false) != true) {
            return
        }

        intent.removeExtra(EXTRA_OPEN_CHAT)
        controller.navigate(HomeRoutes.CHAT) {
            launchSingleTop = true
        }
    }

    private fun syncFcmToken(accessToken: String) {
        val bearerToken = accessToken.trim()
        if (bearerToken.isBlank()) {
            return
        }

        if (!isFirebaseAvailable()) {
            Log.i("ChatFCM", "Skip FCM sync because Firebase is not configured")
            return
        }

        val messaging = runCatching { FirebaseMessaging.getInstance() }
            .onFailure { error ->
                Log.w("ChatFCM", "Skip FCM sync because FirebaseMessaging is unavailable", error)
            }
            .getOrNull()
            ?: return

        messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("ChatFCM", "Không thể lấy FCM token", task.exception)
                return@addOnCompleteListener
            }

            val token = if (task.isSuccessful) task.result?.trim().orEmpty() else ""
            if (token.isBlank()) {
                Log.w("ChatFCM", "FCM token rỗng")
                return@addOnCompleteListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                runCatching {
                    apiService.registerFcmToken(
                        authorization = "Bearer $bearerToken",
                        request = FcmTokenRequestDto(token = token),
                    )
                }.onFailure { error ->
                    Log.w("ChatFCM", "Không thể đăng ký token lên máy chủ", error)
                }
            }
        }
    }

    private fun isFirebaseAvailable(): Boolean {
        val existingApps = runCatching { FirebaseApp.getApps(this) }
            .getOrDefault(emptyList())
        if (existingApps.isNotEmpty()) {
            return true
        }

        return runCatching { FirebaseApp.initializeApp(this) }
            .getOrNull() != null
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun switchTo(menuItemId: Int) {
        val route = when (menuItemId) {
            com.example.mobileproject.R.id.nav_home -> com.example.mobileproject.presentation.ui.screen.app.compose.HomeRoutes.HOME
            com.example.mobileproject.R.id.nav_wallet -> com.example.mobileproject.presentation.ui.screen.app.compose.HomeRoutes.WALLET
            com.example.mobileproject.R.id.nav_explore -> com.example.mobileproject.presentation.ui.screen.app.compose.HomeRoutes.EXPLORE
            com.example.mobileproject.R.id.nav_memories -> com.example.mobileproject.presentation.ui.screen.app.compose.HomeRoutes.MEMORIES
            com.example.mobileproject.R.id.nav_settings -> com.example.mobileproject.presentation.ui.screen.app.compose.HomeRoutes.SETTINGS
            else -> null
        } ?: return

        navController?.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun logoutAndOpenLogin() {
        authSessionStore.clear()
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
        const val EXTRA_OPEN_CHAT: String = "extra_open_chat"
        const val KEY_SELECTED_NAV_ITEM_ID: String = "selected_nav_item_id"
    }

    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

