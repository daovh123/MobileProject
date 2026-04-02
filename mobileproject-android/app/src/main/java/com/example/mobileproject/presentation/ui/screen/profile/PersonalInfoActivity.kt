package com.example.mobileproject.presentation.ui.screen.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
            MaterialTheme {
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
    var fullName by remember { mutableStateOf("") }
    var nickName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val uiState by profileViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedProfile) {
        if (uiState.savedProfile != null) {
            onContinue(accessToken)
            profileViewModel.consumeSaveSuccess()
        }
    }

    val primaryText = colorResource(R.color.auth_text_primary)
    val secondaryText = colorResource(R.color.auth_text_secondary)
    val pink = colorResource(R.color.auth_pink)
    val requiredFieldsMessage = stringResource(R.string.auth_required_fields)

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
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            validationError = null
                            profileViewModel.clearError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.register_name_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = nickName,
                        onValueChange = {
                            nickName = it
                            validationError = null
                            profileViewModel.clearError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.register_nickname_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = {
                            birthDate = it
                            validationError = null
                            profileViewModel.clearError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.register_birthdate_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Text(
                        text = stringResource(R.string.register_gender_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = primaryText,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GenderOptionButton(
                            label = stringResource(R.string.register_gender_male),
                            isSelected = gender == "MALE",
                            onClick = {
                                gender = "MALE"
                                validationError = null
                                profileViewModel.clearError()
                            },
                            modifier = Modifier.weight(1f),
                        )
                        GenderOptionButton(
                            label = stringResource(R.string.register_gender_female),
                            isSelected = gender == "FEMALE",
                            onClick = {
                                gender = "FEMALE"
                                validationError = null
                                profileViewModel.clearError()
                            },
                            modifier = Modifier.weight(1f),
                        )
                        GenderOptionButton(
                            label = stringResource(R.string.register_gender_other),
                            isSelected = gender == "OTHER",
                            onClick = {
                                gender = "OTHER"
                                validationError = null
                                profileViewModel.clearError()
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    val errorMessage = validationError ?: uiState.errorMessage
                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            color = colorResource(R.color.auth_pink),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            if (fullName.isBlank() || birthDate.isBlank() || gender.isNullOrBlank()) {
                                validationError = requiredFieldsMessage
                            } else {
                                validationError = null
                                profileViewModel.saveProfile(
                                    token = accessToken,
                                    fullName = fullName,
                                    nickName = nickName,
                                    birthDate = birthDate,
                                    gender = gender ?: "",
                                )
                            }
                        },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = pink),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = if (uiState.isLoading) {
                                stringResource(R.string.profile_saving)
                            } else {
                                stringResource(R.string.profile_save_continue)
                            },
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GenderOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pink = colorResource(R.color.auth_pink)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) pink.copy(alpha = 0.14f) else Color.Transparent,
            contentColor = if (isSelected) pink else Color.Gray,
        ),
    ) {
        Text(text = label)
    }
}
