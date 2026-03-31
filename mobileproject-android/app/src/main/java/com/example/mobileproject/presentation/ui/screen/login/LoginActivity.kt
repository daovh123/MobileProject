package com.example.mobileproject.presentation.ui.screen.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.screen.couple.CoupleConnectActivity
import com.example.mobileproject.presentation.ui.screen.register.RegisterActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        setContent {
            MaterialTheme {
                LoginScreen(
                    onLogin = {
                        startActivity(Intent(this, CoupleConnectActivity::class.java))
                        finish()
                    },
                    onSignUp = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
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
    onLogin: () -> Unit,
    onSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                    Text(
                        text = stringResource(R.string.login_email_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryText,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
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
                        onValueChange = { password = it },
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
                        onClick = onLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = pink),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(text = stringResource(R.string.login_button), color = Color.White)
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
