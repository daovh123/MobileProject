package com.example.mobileproject.presentation.ui.screen.settings

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.screen.login.LoginActivity
import com.example.mobileproject.presentation.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    accessToken: String,
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    var logoutRequested by remember { mutableStateOf(false) }
    var logoutStatusText by remember { mutableStateOf<String?>(null) }
    var logoutStatusIsError by remember { mutableStateOf(false) }

    fun navigateToLogin() {
        val intent = Intent(context, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        activity?.finish()
    }

    LaunchedEffect(uiState.logoutCompleted) {
        if (uiState.logoutCompleted) {
            authViewModel.consumeLogoutSuccess()
            logoutRequested = false
            logoutStatusText = null
            logoutStatusIsError = false
            Toast.makeText(context, context.getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    LaunchedEffect(uiState.isLoading, logoutRequested) {
        if (logoutRequested && uiState.isLoading) {
            logoutStatusText = context.getString(R.string.settings_logout_processing)
            logoutStatusIsError = false
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (logoutRequested && !message.isNullOrBlank()) {
            logoutRequested = false
            logoutStatusText = message
            logoutStatusIsError = true
            authViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.md3_surface_variant))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = stringResource(R.string.settings_account_section_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorResource(R.color.md3_primary),
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_account_section_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.md3_on_surface_variant),
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (logoutRequested) {
                            return@Button
                        }

                        if (accessToken.isBlank()) {
                            navigateToLogin()
                            return@Button
                        }

                        logoutRequested = true
                        logoutStatusText = null
                        logoutStatusIsError = false
                        authViewModel.logout(accessToken)
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.auth_pink),
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_logout_button),
                        color = androidx.compose.ui.graphics.Color.White,
                    )
                }

                val statusText = logoutStatusText
                if (!statusText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (logoutStatusIsError) {
                            colorResource(R.color.auth_pink)
                        } else {
                            colorResource(R.color.md3_on_surface_variant)
                        },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.settings_logout_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.md3_on_surface_variant),
                )
            }
        }
    }
}
