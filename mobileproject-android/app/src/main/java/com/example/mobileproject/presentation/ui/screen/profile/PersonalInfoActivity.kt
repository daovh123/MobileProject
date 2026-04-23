package com.example.mobileproject.presentation.ui.screen.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
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
            MobileProjectTheme {
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
                text = stringResource(R.string.profile_setup_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.profile_setup_subtitle),
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
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                            color = colorResource(R.color.auth_pink),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
