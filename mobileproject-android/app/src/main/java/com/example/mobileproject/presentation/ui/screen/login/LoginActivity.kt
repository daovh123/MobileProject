/**
 * LoginActivity - Activity host cho màn hình đăng nhập.
 *
 * Mục đích:
 * - Cho phép người dùng đăng nhập bằng email/username và mật khẩu.
 * - Hỗ trợ pre-fill email từ màn hình đăng ký.
 * - Hiển thị thông báo đăng ký thành công nếu chuyển từ RegisterActivity.
 *
 * Layout:
 * - BoxWithConstraints responsive: màn hình nhỏ (< 760dp) sẽ bật scroll.
 * - Floating card (bo tròn 36dp, nền semi-transparent) chứa form.
 * - Hình ảnh trang trí thỏ + cáo ở góc dưới.
 *
 * Logic kiểm tra session:
 * - Nếu đã có session hợp lệ → chuyển đến [PersonalInfoActivity] hoặc [HomeActivity].
 *
 * ViewModels:
 * - [AuthViewModel]: xử lý login, quan sát uiState (loading, error, authSession).
 * - [ThemeModeViewModel]: đọc theme mode (sáng/tối/hệ thống).
 *
 * Navigation:
 * - Đăng nhập thành công → [HomeActivity] hoặc [PersonalInfoActivity] (tùy profileCompleted).
 * - "Đăng ký" → [RegisterActivity].
 *
 * Chế độ hiển thị: Immersive (ẩn status bar).
 */
package com.example.mobileproject.presentation.ui.screen.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.screen.profile.PersonalInfoActivity
import com.example.mobileproject.presentation.ui.screen.register.RegisterActivity
import com.example.mobileproject.presentation.viewmodel.AuthViewModel
import com.example.mobileproject.presentation.viewmodel.ThemeModeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * LoginActivity - Activity host cho màn hình đăng nhập.
 *
 * Mục đích:
 * - Cho phép người dùng đăng nhập bằng email/username và mật khẩu.
 * - Hỗ trợ pre-fill email từ màn hình đăng ký.
 * - Hiển thị thông báo đăng ký thành công nếu chuyển từ RegisterActivity.
 *
 * Logic kiểm tra session:
 * - Nếu đã có session hợp lệ → chuyển đến [PersonalInfoActivity] hoặc [HomeActivity].
 *
 * Dependency Injection:
 * - Hilt (@AndroidEntryPoint), inject [AuthSessionStore] và tạo [AuthViewModel].
 *
 * Navigation:
 * - Đăng nhập thành công → [HomeActivity] hoặc [PersonalInfoActivity] (tùy profileCompleted).
 * - "Đăng ký" → [RegisterActivity].
 */
@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    /** AuthSessionStore: kiểm tra session đã lưu trước khi hiển thị form đăng nhập */
    @Inject
    lateinit var authSessionStore: AuthSessionStore

    companion object {
        /** Extra key để pre-fill email từ RegisterActivity */
        const val EXTRA_PREFILLED_EMAIL = "extra_prefilled_email"
        /** Extra key đánh dấu vừa đăng ký thành công, hiển thị banner提示 */
        const val EXTRA_REGISTERED_SUCCESS = "extra_registered_success"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        // Đọc extra từ intent: email pre-fill, trạng thái đăng ký, yêu cầu mở chat
        val prefilledEmail = intent.getStringExtra(EXTRA_PREFILLED_EMAIL).orEmpty()
        val showRegistrationSuccess = intent.getBooleanExtra(EXTRA_REGISTERED_SUCCESS, false)
        val openChat = intent.getBooleanExtra(HomeActivity.EXTRA_OPEN_CHAT, false)

        // Kiểm tra session đã lưu → bypass login nếu hợp lệ
        val savedSession = authSessionStore.load()
        if (savedSession != null) {
            val intent = if (!savedSession.profileCompleted) {
                Intent(this, PersonalInfoActivity::class.java)
            } else {
                Intent(this, HomeActivity::class.java)
            }.apply {
                putExtra(PersonalInfoActivity.EXTRA_ACCESS_TOKEN, savedSession.token)
                putExtra(HomeActivity.EXTRA_ACCESS_TOKEN, savedSession.token)
                putExtra(HomeActivity.EXTRA_OPEN_CHAT, openChat)
            }
            startActivity(intent)
            finish()
            return
        }

        setContent {
            val themeViewModel: ThemeModeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                com.example.mobileproject.presentation.ui.theme.ThemeMode.SYSTEM -> isSystemInDarkTheme()
                com.example.mobileproject.presentation.ui.theme.ThemeMode.LIGHT -> false
                com.example.mobileproject.presentation.ui.theme.ThemeMode.DARK -> true
            }

            com.example.mobileproject.presentation.ui.theme.MobileProjectTheme(
                darkTheme = darkTheme,
                dynamicColor = false
            ) {
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    onLoginSuccess = { session ->
                        val intent = if (!session.profileCompleted) {
                            Intent(this, PersonalInfoActivity::class.java)
                        } else {
                            Intent(this, HomeActivity::class.java)
                        }.apply {
                            putExtra(PersonalInfoActivity.EXTRA_ACCESS_TOKEN, session.token)
                            putExtra(HomeActivity.EXTRA_ACCESS_TOKEN, session.token)
                            putExtra(HomeActivity.EXTRA_OPEN_CHAT, openChat)
                        }
                        startActivity(intent)
                        finish()
                    },
                    onSignUp = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    prefilledEmail = prefilledEmail,
                    showRegistrationSuccess = showRegistrationSuccess,
                    authViewModel = authViewModel,
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

/**
 * Composable trường input dùng chung cho màn hình Login.
 *
 * Hiển thị label phía trên + OutlinedTextField với leading/trailing icon.
 * Style: bo tròn 50dp (pill shape), nền surfaceContainer.
 *
 * @param value Giá trị hiện tại của field.
 * @param onValueChange Callback khi giá trị thay đổi.
 * @param label Nhãn hiển thị phía trên field (viết hoa).
 * @param leadingIcon Icon bên trái (ví dụ: Email, Lock).
 * @param trailingIcon Icon bên phải (ví dụ: nút ẩn/hiện mật khẩu).
 * @param visualTransformation Transform hiển thị (ẩn/hiện password).
 * @param keyboardOptions Cấu hình bàn phím (loại, IME action).
 * @param keyboardActions Xử lý action bàn phím (Done → submit).
 */
@Composable
private fun LoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            singleLine = true,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(50.dp)
        )
    }
}
