package com.example.mobileproject.presentation.ui.screen.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.example.mobileproject.domain.entity.PostLoginDestination
import com.example.mobileproject.domain.entity.resolvePostLoginDestination
import com.example.mobileproject.presentation.ui.icons.LucideEye
import com.example.mobileproject.presentation.ui.icons.LucideEyeOff
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.screen.profile.PersonalInfoActivity
import com.example.mobileproject.presentation.ui.screen.register.RegisterActivity
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
import com.example.mobileproject.presentation.ui.theme.resolveDarkTheme
import com.example.mobileproject.presentation.viewmodel.AuthViewModel
import com.example.mobileproject.presentation.viewmodel.ThemeModeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    companion object {
        const val EXTRA_PREFILLED_EMAIL = "extra_prefilled_email"
        const val EXTRA_REGISTERED_SUCCESS = "extra_registered_success"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        val prefilledEmail = intent.getStringExtra(EXTRA_PREFILLED_EMAIL).orEmpty()
        val showRegistrationSuccess = intent.getBooleanExtra(EXTRA_REGISTERED_SUCCESS, false)
        val openChat = intent.getBooleanExtra(HomeActivity.EXTRA_OPEN_CHAT, false)

        val savedSession = authSessionStore.load()
        if (savedSession != null) {
            val intent = when (savedSession.resolvePostLoginDestination()) {
                PostLoginDestination.PROFILE -> Intent(this, PersonalInfoActivity::class.java)
                PostLoginDestination.HOME -> Intent(this, HomeActivity::class.java)
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
            val darkTheme = themeMode.resolveDarkTheme(isSystemInDarkTheme())

            MobileProjectTheme(darkTheme = darkTheme, dynamicColor = false) {
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    onLoginSuccess = { session ->
                        val intent = when (session.resolvePostLoginDestination()) {
                            PostLoginDestination.PROFILE -> Intent(this, PersonalInfoActivity::class.java)
                            PostLoginDestination.HOME -> Intent(this, HomeActivity::class.java)
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

@Composable
private fun LoginScreen(
    onLoginSuccess: (AuthSession) -> Unit,
    onSignUp: () -> Unit,
    prefilledEmail: String,
    showRegistrationSuccess: Boolean,
    authViewModel: AuthViewModel,
) {
    var email by rememberSaveable(prefilledEmail) { mutableStateOf(prefilledEmail) }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val uiState by authViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.authSession) {
        val session = uiState.authSession
        if (session != null) {
            onLoginSuccess(session)
            authViewModel.consumeAuthSuccess()
        }
    }

    val onSubmitLogin: () -> Unit = {
        authViewModel.login(
            usernameOrEmail = email.trim(),
            password = password,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBFB))
    ) {
        // FOOTER ILLUSTRATIONS LAYER (BOTTOM ABSOLUTE POSITIONING)
        // Left Mascot (img_register_and_login_rabbit)
        Image(
            painter = painterResource(id = R.drawable.img_register_and_login_rabbit),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(220.dp)
                .height(296.dp),
            contentScale = ContentScale.Fit
        )

        // Right Mascot (img_register_and_login_fox)
        Image(
            painter = painterResource(id = R.drawable.img_register_and_login_fox),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(232.dp)
                .height(462.dp),
            contentScale = ContentScale.Fit
        )

        // INPUT FORM AREA (TOP TO MIDDLE)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Floating Card wrapping the form content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White.copy(alpha = 0.55f))
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title: Welcome Back
                Text(
                    text = "Welcome Back",
                    color = Color(0xFF3D1F1F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Registration Success Message
                AnimatedVisibility(visible = showRegistrationSuccess) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFE3E5).copy(alpha = 0.9f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.login_after_register_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF3D1F1F),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }

                // EMAIL ADDRESS Field
                LoginInputField(
                    value = email,
                    onValueChange = {
                        email = it
                        authViewModel.clearError()
                    },
                    label = "EMAIL ADDRESS",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = null,
                            tint = Color(0xFF3D1F1F).copy(alpha = 0.6f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PASSWORD Field
                LoginInputField(
                    value = password,
                    onValueChange = {
                        password = it
                        authViewModel.clearError()
                    },
                    label = "PASSWORD",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color(0xFF3D1F1F).copy(alpha = 0.6f)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) LucideEyeOff else LucideEye,
                                contentDescription = null,
                                tint = Color(0xFF3D1F1F).copy(alpha = 0.6f)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onSubmitLogin()
                        }
                    )
                )

                // Forgot Password link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, end = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = stringResource(R.string.login_forgot_password),
                        color = Color(0xFF944649),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { /* Reset password action or dummy */ }
                    )
                }

                // Primary Button: Login
                Button(
                    onClick = onSubmitLogin,
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF944649),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Eco,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (uiState.isLoading) "SIGNING IN..." else stringResource(R.string.login_button),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Error display
                AnimatedVisibility(visible = !uiState.errorMessage.isNullOrBlank()) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Redirect to Sign Up link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.login_no_account),
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Text(
                        text = stringResource(R.string.login_sign_up),
                        color = Color(0xFF944649),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .clickable(onClick = onSignUp),
                    )
                }
            }

            // Bottom spacer for Z-index overlapping prevention
            Spacer(modifier = Modifier.height(220.dp))
        }
    }
}

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
            color = Color(0xFF3D1F1F),
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
                focusedContainerColor = Color(0xFFFFE3E5),
                unfocusedContainerColor = Color(0xFFFFE3E5),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = Color(0xFF3D1F1F),
                focusedTextColor = Color(0xFF3D1F1F),
                unfocusedTextColor = Color(0xFF3D1F1F),
                unfocusedLeadingIconColor = Color(0xFF3D1F1F).copy(alpha = 0.6f),
                focusedLeadingIconColor = Color(0xFF3D1F1F),
                unfocusedTrailingIconColor = Color(0xFF3D1F1F).copy(alpha = 0.6f),
                focusedTrailingIconColor = Color(0xFF3D1F1F)
            ),
            shape = RoundedCornerShape(50.dp)
        )
    }
}
