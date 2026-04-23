package com.example.mobileproject.presentation.ui.screen.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
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
        containerColor = colorResource(R.color.md3_surface_variant),
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineMedium,
                color = colorResource(R.color.md3_primary),
            )

            HeaderCard(
                username = uiState.savedProfile?.username.orEmpty(),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
                shape = RoundedCornerShape(20.dp),
            ) {
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
                        )
                    },
                    onNickNameChange = {
                        viewModel.updateDraft(
                            fullName = uiState.fullName,
                            nickName = it,
                            birthDate = uiState.birthDate,
                            gender = uiState.gender,
                        )
                    },
                    onBirthDateChange = {
                        viewModel.updateDraft(
                            fullName = uiState.fullName,
                            nickName = uiState.nickName,
                            birthDate = it,
                            gender = uiState.gender,
                        )
                    },
                    onGenderChange = {
                        viewModel.updateDraft(
                            fullName = uiState.fullName,
                            nickName = uiState.nickName,
                            birthDate = uiState.birthDate,
                            gender = it,
                        )
                    },
                    onSave = { viewModel.saveProfile(accessToken) },
                    modifier = Modifier.padding(16.dp),
                    showSaveButton = false,
                )
            }

            CoupleStatusCard(
                isLoading = uiState.isLoadingCouple,
                status = uiState.coupleStatus,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorResource(R.color.auth_pink),
                        ),
                    ) {
                        Text(text = stringResource(R.string.profile_logout))
                    }

                    Button(
                        onClick = { viewModel.saveProfile(accessToken) },
                        enabled = uiState.isDirty && !uiState.isSaving,
                        modifier = Modifier.weight(1f),
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                            )
                        } else {
                            Text(text = stringResource(R.string.profile_save))
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.profile_back))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HeaderCard(
    username: String,
) {
    val displayUsername = username.ifBlank { "User" }
    val avatarText = displayUsername.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(colorResource(R.color.md3_primary), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = avatarText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorResource(R.color.md3_on_primary),
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = displayUsername,
                style = MaterialTheme.typography.titleLarge,
                color = colorResource(R.color.md3_on_surface),
            )
            Text(
                text = "@$displayUsername",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.md3_on_surface_variant),
            )
        }
    }
}

@Composable
private fun CoupleStatusCard(
    isLoading: Boolean,
    status: CoupleStatus?,
) {
    val statusText = when {
        isLoading -> stringResource(R.string.explore_updating)
        status == null -> stringResource(R.string.profile_couple_none)
        status.paired -> {
            val partner = status.partnerUsername ?: stringResource(R.string.home_pair_partner_unknown)
            val days = status.daysTogether ?: 0
            "$partner • ${stringResource(R.string.profile_couple_days_together, days.toInt())}"
        }
        !status.outgoingRequestId.isNullOrBlank() -> stringResource(R.string.profile_couple_pending)
        !status.incomingRequestId.isNullOrBlank() -> stringResource(R.string.profile_couple_incoming)
        else -> stringResource(R.string.profile_couple_none)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_couple_status),
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(R.color.md3_on_surface),
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.md3_on_surface_variant),
            )
        }
    }
}
