package com.example.mobileproject.presentation.ui.screen.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavHostController
import dagger.hilt.android.AndroidEntryPoint
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.presentation.ui.screen.login.LoginActivity
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    @Inject
    lateinit var apiService: ApiService

    private var navController: NavHostController? = null
    private var accessToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        if (accessToken.isBlank()) {
            accessToken = authSessionStore.load()?.token.orEmpty()
        }

        setContent {
            MaterialTheme {
                com.example.mobileproject.presentation.ui.screen.app.compose.HomeScaffold(
                    accessToken = accessToken,
                    apiService = apiService,
                    onNavControllerReady = { controller ->
                        navController = controller
                    },
                )
            }
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
        const val KEY_SELECTED_NAV_ITEM_ID: String = "selected_nav_item_id"
    }
}
