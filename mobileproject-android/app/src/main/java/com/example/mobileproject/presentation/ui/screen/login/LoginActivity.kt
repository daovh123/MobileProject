package com.example.mobileproject.presentation.ui.screen.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.entity.PostLoginDestination
import com.example.mobileproject.domain.entity.resolvePostLoginDestination
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.screen.profile.PersonalInfoActivity
import com.example.mobileproject.presentation.ui.screen.register.RegisterActivity
import com.example.mobileproject.presentation.viewmodel.AuthViewModel
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

        val savedSession = authSessionStore.load()
        if (savedSession != null) {
            val intent = when (savedSession.resolvePostLoginDestination()) {
                PostLoginDestination.PROFILE -> Intent(this, PersonalInfoActivity::class.java)
                PostLoginDestination.HOME -> Intent(this, HomeActivity::class.java)
            }.apply {
                putExtra(PersonalInfoActivity.EXTRA_ACCESS_TOKEN, savedSession.token)
                putExtra(HomeActivity.EXTRA_ACCESS_TOKEN, savedSession.token)
            }
            startActivity(intent)
            finish()
            return
        }

        setContent {
            MaterialTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    onLoginSuccess = { session ->
                        val intent = when (session.resolvePostLoginDestination()) {
                            PostLoginDestination.PROFILE -> Intent(this, PersonalInfoActivity::class.java)
                            PostLoginDestination.HOME -> Intent(this, HomeActivity::class.java)
                        }.apply {
                            putExtra(PersonalInfoActivity.EXTRA_ACCESS_TOKEN, session.token)
                            putExtra(HomeActivity.EXTRA_ACCESS_TOKEN, session.token)
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
    var email by remember(prefilledEmail) { mutableStateOf(prefilledEmail) }
    var password by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.authSession) {
        val session = uiState.authSession
        if (session != null) {
            onLoginSuccess(session)
            authViewModel.consumeAuthSuccess()
        }
    }

    val primaryText = colorResource(R.color.auth_text_primary)
    val secondaryText = colorResource(R.color.auth_text_secondary)
    val pink = colorResource(R.color.auth_pink)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.auth_bg_top),
                        colorResource(R.color.auth_bg_bottom)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = pink,
                shadowElevation = 0.dp,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "❤", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.login_welcome_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))
            Surface(
                shape = RoundedCornerShape(36.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (showRegistrationSuccess) {
                        Text(
                            text = stringResource(R.string.login_after_register_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(R.color.auth_pink),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = stringResource(R.string.login_email_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryText,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            authViewModel.clearError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.login_email_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.login_password_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryText,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            authViewModel.clearError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.login_password_hint)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp)
                    )

                    TextButton(
                        onClick = {},
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(R.string.login_forgot_password),
                            color = pink,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            authViewModel.login(
                                usernameOrEmail = email,
                                password = password,
                            )
                        },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = pink),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = if (uiState.isLoading) "Dang dang nhap..." else stringResource(R.string.login_button),
                            color = Color.White,
                        )
                    }

                    if (!uiState.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = colorResource(R.color.auth_pink),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.login_no_account), color = primaryText)
                        Text(
                            text = stringResource(R.string.login_sign_up),
                            color = pink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable(onClick = onSignUp)
                        )
                    }
                }
            }
        }
    }
}
