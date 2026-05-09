package com.example.mobileproject.presentation.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.components.core.AppSectionHeader
import com.example.mobileproject.presentation.ui.components.core.AppSurfaceCard
import com.example.mobileproject.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileEditScreen(
    accessToken: String,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(accessToken) {
        viewModel.loadProfile(accessToken)
    }

    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccessMessage) {
        val successMessage = uiState.saveSuccessMessage
        if (!successMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(successMessage)
            viewModel.consumeSaveSuccess()
        }
    }

    LaunchedEffect(uiState.sessionExpired) {
        if (uiState.sessionExpired) {
            viewModel.clearSessionExpired()
            onLogout()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppSectionHeader(
                title = stringResource(R.string.profile_edit_title),
                subtitle = stringResource(R.string.profile_edit_subtitle),
            )

            AppSurfaceCard {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { newEmail ->
                            viewModel.updateDraft(
                                fullName = uiState.fullName,
                                nickName = uiState.nickName,
                                birthDate = uiState.birthDate,
                                gender = uiState.gender,
                                email = newEmail,
                            )
                        },
                        label = { Text(text = stringResource(R.string.profile_email)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                        ),
                        shape = MaterialTheme.shapes.large,
                    )

                    ProfileFormContent(
                        fullName = uiState.fullName,
                        nickName = uiState.nickName,
                        birthDate = uiState.birthDate,
                        gender = uiState.gender,
                        isSaving = uiState.isSaving,
                        onFullNameChange = {
                            viewModel.updateDraft(
                                fullName = it,
                                nickName = uiState.nickName,
                                birthDate = uiState.birthDate,
                                gender = uiState.gender,
                                email = uiState.email,
                            )
                        },
                        onNickNameChange = {
                            viewModel.updateDraft(
                                fullName = uiState.fullName,
                                nickName = it,
                                birthDate = uiState.birthDate,
                                gender = uiState.gender,
                                email = uiState.email,
                            )
                        },
                        onBirthDateChange = {
                            viewModel.updateDraft(
                                fullName = uiState.fullName,
                                nickName = uiState.nickName,
                                birthDate = it,
                                gender = uiState.gender,
                                email = uiState.email,
                            )
                        },
                        onGenderChange = {
                            viewModel.updateDraft(
                                fullName = uiState.fullName,
                                nickName = uiState.nickName,
                                birthDate = uiState.birthDate,
                                gender = it,
                                email = uiState.email,
                            )
                        },
                        onSave = { viewModel.saveProfile(accessToken) },
                        saveEnabled = uiState.isDirty,
                        showSaveButton = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Button(
                onClick = { viewModel.saveProfile(accessToken) },
                enabled = uiState.isDirty && !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = stringResource(R.string.profile_save))
                }
            }

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.profile_back))
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
