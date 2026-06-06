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

/**
 * WelcomeActivity - Activity host cho màn hình Welcome.
 *
 * Mục đích:
 * - Điểm vào đầu tiên của ứng dụng, kiểm tra phiên đăng nhập đã lưu.
 * - Nếu đã có session → chuyển thẳng đến [PersonalInfoActivity] (nếu chưa hoàn tất profile)
 *   hoặc [HomeActivity] (nếu đã hoàn tất).
 * - Nếu chưa có session → hiển thị [WelcomeScreen].
 *
 * Chế độ hiển thị:
 * - Chạy ở chế độ immersive (ẩn status bar) để tạo trải nghiệm toàn màn hình.
 *
 * Dependency Injection:
 * - Sử dụng Hilt (@AndroidEntryPoint) để inject [AuthSessionStore].
 * - Sử dụng [ThemeModeViewModel] để đọc theme mode (sáng/tối/hệ thống).
 *
 * Navigation:
 * - "Bắt đầu" → [RegisterActivity]
 * - "Đăng nhập" → [LoginActivity]
 * - Session hợp lệ → [PersonalInfoActivity] hoặc [HomeActivity]
 */
@AndroidEntryPoint
class WelcomeActivity : ComponentActivity() {

    /** AuthSessionStore: đọc/ghi session đăng nhập từ local storage */
    @Inject
    lateinit var authSessionStore: AuthSessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bật chế độ immersive: ẩn status bar để toàn màn hình
        enableImmersiveMode()

        // Đọc extra từ intent: có yêu cầu mở chat từ notification không
        val openChat = intent.getBooleanExtra(HomeActivity.EXTRA_OPEN_CHAT, false)
        // Kiểm tra session đã lưu: nếu có token hợp lệ thì bỏ qua WelcomeScreen
        val savedSession = authSessionStore.load()
        if (savedSession != null) {
            // Quyết định đích đến dựa trên trạng thái profile
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
            // Lấy ThemeModeViewModel qua Hilt để đọc tùy chọn theme (sáng/tối/hệ thống)
            val themeViewModel: ThemeModeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            // Resolve theme mode thành boolean darkTheme dựa trên setting + system theme
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

    /**
     * Bật chế độ immersive: ẩn status bar, chỉ hiện lại khi vuốt.
     * Tạo trải nghiệm toàn màn hình cho welcome/login screens.
     */
    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
