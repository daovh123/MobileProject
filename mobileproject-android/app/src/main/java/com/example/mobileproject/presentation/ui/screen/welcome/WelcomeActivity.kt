package com.example.mobileproject.presentation.ui.screen.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.PostLoginDestination
import com.example.mobileproject.domain.entity.resolvePostLoginDestination
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.screen.login.LoginActivity
import com.example.mobileproject.presentation.ui.screen.profile.PersonalInfoActivity
import com.example.mobileproject.presentation.ui.screen.register.RegisterActivity
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
import com.example.mobileproject.presentation.ui.theme.resolveDarkTheme
import com.example.mobileproject.presentation.viewmodel.ThemeModeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeActivity : ComponentActivity() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()

        val openChat = intent.getBooleanExtra(HomeActivity.EXTRA_OPEN_CHAT, false)
        val savedSession = authSessionStore.load()
        if (savedSession != null) {
            val destinationIntent = when (savedSession.resolvePostLoginDestination()) {
                PostLoginDestination.PROFILE -> Intent(this, PersonalInfoActivity::class.java)
                PostLoginDestination.HOME -> Intent(this, HomeActivity::class.java)
            }.apply {
                putExtra(PersonalInfoActivity.EXTRA_ACCESS_TOKEN, savedSession.token)
                putExtra(HomeActivity.EXTRA_ACCESS_TOKEN, savedSession.token)
                putExtra(HomeActivity.EXTRA_OPEN_CHAT, openChat)
            }
            startActivity(destinationIntent)
            finish()
            return
        }

        setContent {
            val themeViewModel: ThemeModeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val darkTheme = themeMode.resolveDarkTheme(isSystemInDarkTheme())

            MobileProjectTheme(darkTheme = darkTheme, dynamicColor = false) {
                WelcomeScreen(
                    onGetStartedClick = {
                        startActivity(Intent(this@WelcomeActivity, RegisterActivity::class.java))
                    },
                    onLoginClick = {
                        startActivity(
                            Intent(this@WelcomeActivity, LoginActivity::class.java).apply {
                                putExtra(HomeActivity.EXTRA_OPEN_CHAT, openChat)
                            }
                        )
                    },
                )
            }
        }
    }

    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
