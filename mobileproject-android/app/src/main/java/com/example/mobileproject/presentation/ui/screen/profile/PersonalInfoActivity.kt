package com.example.mobileproject.presentation.ui.screen.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.components.auth.AuthBackdrop
import com.example.mobileproject.presentation.ui.components.auth.AuthBrandMark
import com.example.mobileproject.presentation.ui.components.auth.AuthFormSurface
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
import com.example.mobileproject.presentation.ui.theme.resolveDarkTheme
import com.example.mobileproject.presentation.viewmodel.ThemeModeViewModel
import com.example.mobileproject.presentation.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonalInfoActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        enableImmersiveMode()
        setContent {
            val themeViewModel: ThemeModeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val darkTheme = themeMode.resolveDarkTheme(isSystemInDarkTheme())

            MobileProjectTheme(darkTheme = darkTheme) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                PersonalInfoScreen(
                    accessToken = accessToken,
                    profileViewModel = profileViewModel,
                    onContinue = { token ->
                        startActivity(
                            Intent(this, HomeActivity::class.java)
                                .putExtra(HomeActivity.EXTRA_ACCESS_TOKEN, token)
                        )
                        finish()
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
private fun PersonalInfoScreen(
    accessToken: String,
    profileViewModel: ProfileViewModel,
    onContinue: (String) -> Unit,
) {
    var submitRequested by remember { mutableStateOf(false) }
    val uiState by profileViewModel.uiState.collectAsState()

    LaunchedEffect(accessToken) {
        if (accessToken.isNotBlank()) {
            profileViewModel.loadProfile(accessToken)
        }
    }

    LaunchedEffect(uiState.savedProfile, uiState.isSaving, submitRequested) {
        if (submitRequested && !uiState.isSaving && uiState.savedProfile != null && uiState.errorMessage.isNullOrBlank()) {
            submitRequested = false
            onContinue(accessToken)
            profileViewModel.consumeSaveSuccess()
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    AuthBackdrop(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            AuthBrandMark()
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.profile_setup_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.profile_setup_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))
            AuthFormSurface {
                ProfileFormContent(
                    fullName = uiState.fullName,
                    nickName = uiState.nickName,
                    birthDate = uiState.birthDate,
                    gender = uiState.gender,
                    isSaving = uiState.isSaving,
                    onFullNameChange = {
                        profileViewModel.updateDraft(
                            fullName = it,
                            nickName = uiState.nickName,
                            birthDate = uiState.birthDate,
                            gender = uiState.gender,
                        )
                        profileViewModel.clearError()
                    },
                    onNickNameChange = {
                        profileViewModel.updateDraft(
                            fullName = uiState.fullName,
                            nickName = it,
                            birthDate = uiState.birthDate,
                            gender = uiState.gender,
                        )
                        profileViewModel.clearError()
                    },
                    onBirthDateChange = {
                        profileViewModel.updateDraft(
                            fullName = uiState.fullName,
                            nickName = uiState.nickName,
                            birthDate = it,
                            gender = uiState.gender,
                        )
                        profileViewModel.clearError()
                    },
                    onGenderChange = {
                        profileViewModel.updateDraft(
                            fullName = uiState.fullName,
                            nickName = uiState.nickName,
                            birthDate = uiState.birthDate,
                            gender = it,
                        )
                        profileViewModel.clearError()
                    },
                    onSave = {
                        submitRequested = true
                        profileViewModel.saveProfile(token = accessToken)
                    },
                    saveEnabled = !uiState.isSaving,
                )

                val errorMessage = uiState.errorMessage
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
