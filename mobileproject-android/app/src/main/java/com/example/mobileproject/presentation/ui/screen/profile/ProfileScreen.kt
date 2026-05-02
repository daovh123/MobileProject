package com.example.mobileproject.presentation.ui.screen.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.AvatarFrame
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
        viewModel.loadAvatarFrames(accessToken)
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

    LaunchedEffect(uiState.avatarUploadError) {
        val avatarError = uiState.avatarUploadError
        if (!avatarError.isNullOrBlank()) {
            snackbarHostState.showSnackbar(avatarError)
            viewModel.clearAvatarError()
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
                avatarBitmap = uiState.avatarBitmap,
                avatarFrameId = uiState.avatarFrameId,
                isUploadingAvatar = uiState.isUploadingAvatar,
                showFrameSelector = uiState.showFrameSelector,
                availableFrames = uiState.availableFrames,
                isLoadingFrames = uiState.isLoadingFrames,
                onPickImage = { imageBytes, contentType ->
                    viewModel.uploadAvatar(accessToken, imageBytes, contentType)
                },
                onShowFrameSelector = {
                    viewModel.showFrameSelector()
                },
                onHideFrameSelector = { viewModel.hideFrameSelector() },
                onSelectFrame = { frameId -> viewModel.selectFrame(accessToken, frameId) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeaderCard(
    username: String,
    avatarBitmap: Bitmap?,
    avatarFrameId: String?,
    isUploadingAvatar: Boolean,
    showFrameSelector: Boolean,
    availableFrames: List<AvatarFrame>,
    isLoadingFrames: Boolean,
    onPickImage: (ByteArray, String) -> Unit,
    onShowFrameSelector: () -> Unit,
    onHideFrameSelector: () -> Unit,
    onSelectFrame: (String?) -> Unit,
) {
    val displayUsername = username.ifBlank { "User" }
    val avatarText = displayUsername.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    val context = LocalContext.current

    val selectedFrame = availableFrames.firstOrNull { it.id == avatarFrameId }
    val frameColor = selectedFrame?.let {
        runCatching { Color(android.graphics.Color.parseColor(it.color)) }.getOrNull()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@rememberLauncherForActivityResult
        onPickImage(bytes, mimeType)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showFrameSelector) {
        ModalBottomSheet(
            onDismissRequest = onHideFrameSelector,
            sheetState = sheetState,
        ) {
            FrameSelectorContent(
                frames = availableFrames,
                selectedFrameId = avatarFrameId,
                isLoading = isLoadingFrames,
                onSelectFrame = onSelectFrame,
            )
        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar with camera-icon overlay
            Box(contentAlignment = Alignment.BottomEnd) {
                // Outer frame border + avatar
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .then(
                            if (frameColor != null) {
                                Modifier.border(4.dp, frameColor, CircleShape)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isUploadingAvatar) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(colorResource(R.color.md3_primary), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = colorResource(R.color.md3_on_primary),
                                strokeWidth = 3.dp,
                            )
                        }
                    } else if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap.asImageBitmap(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
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
                    }
                }

                // Camera edit overlay button
                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                ) {
                    Text(
                        text = "📷",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
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

            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onShowFrameSelector) {
                Text(text = "Change Frame")
            }
        }
    }
}

@Composable
private fun FrameSelectorContent(
    frames: List<AvatarFrame>,
    selectedFrameId: String?,
    isLoading: Boolean,
    onSelectFrame: (String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Choose Avatar Frame",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 12.dp),
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // "No Frame" option
                item {
                    FrameItem(
                        label = "None",
                        borderColor = MaterialTheme.colorScheme.outline,
                        isSelected = selectedFrameId == null,
                        showNoFrame = true,
                        onClick = { onSelectFrame(null) },
                    )
                }
                items(frames, key = { it.id }) { frame ->
                    val color = runCatching {
                        Color(android.graphics.Color.parseColor(frame.color))
                    }.getOrElse { MaterialTheme.colorScheme.primary }
                    FrameItem(
                        label = frame.name,
                        borderColor = color,
                        isSelected = selectedFrameId == frame.id,
                        showNoFrame = false,
                        onClick = { onSelectFrame(frame.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FrameItem(
    label: String,
    borderColor: Color,
    isSelected: Boolean,
    showNoFrame: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .border(
                    width = if (isSelected) 3.dp else 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else borderColor,
                    shape = CircleShape,
                )
                .background(
                    color = borderColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else if (showNoFrame) {
                Text(
                    text = "⊘",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
