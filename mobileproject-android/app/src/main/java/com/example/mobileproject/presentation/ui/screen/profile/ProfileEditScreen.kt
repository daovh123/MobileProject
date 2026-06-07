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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.AvatarFrame
import com.example.mobileproject.presentation.ui.icons.LucideCamera
import com.example.mobileproject.presentation.viewmodel.ProfileViewModel
import java.time.Instant
import java.time.ZoneId

@Composable
fun ProfileEditScreen(
    accessToken: String,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(accessToken) {
        viewModel.ensureProfileLoaded(accessToken)
        viewModel.loadAvatarFrames(accessToken)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.takeIf { it.isNotBlank() }?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccessMessage) {
        uiState.saveSuccessMessage?.takeIf { it.isNotBlank() }?.let {
            snackbarHostState.showSnackbar(it)
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.profile_back),
                    )
                }
                Text(
                    text = "Sửa thông tin cá nhân",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            EditableAvatarCard(
                avatarBitmap = uiState.avatarBitmap,
                fallbackText = uiState.nickName.ifBlank {
                    uiState.fullName.ifBlank { stringResource(R.string.profile_username_fallback) }
                },
                avatarFrameId = uiState.avatarFrameId,
                showFrameSelector = uiState.showFrameSelector,
                availableFrames = uiState.availableFrames,
                isLoadingFrames = uiState.isLoadingFrames,
                onPickImage = { bytes, contentType ->
                    viewModel.uploadAvatar(accessToken, bytes, contentType)
                },
                onShowFrameSelector = { viewModel.showFrameSelector() },
                onHideFrameSelector = { viewModel.hideFrameSelector() },
                onSelectFrame = { frameId -> viewModel.selectFrame(accessToken, frameId) },
            )

            ProfileEditFields(
                nickName = uiState.nickName,
                email = uiState.email,
                birthDate = uiState.birthDate,
                onNickNameChange = { value ->
                    viewModel.updateDraft(
                        fullName = uiState.fullName,
                        nickName = value,
                        birthDate = uiState.birthDate,
                        gender = uiState.gender,
                        email = uiState.email,
                    )
                },
                onEmailChange = { value ->
                    viewModel.updateDraft(
                        fullName = uiState.fullName,
                        nickName = uiState.nickName,
                        birthDate = uiState.birthDate,
                        gender = uiState.gender,
                        email = value,
                    )
                },
                onBirthDateClick = { showDatePicker = true },
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Trạng thái tài khoản",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Đã xác minh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Thông tin hồ sơ của bạn được bảo mật và mã hóa trong không gian riêng tư của hai bạn.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                viewModel.updateDraft(
                                    fullName = uiState.fullName,
                                    nickName = uiState.nickName,
                                    birthDate = selectedDate.toString(),
                                    gender = uiState.gender,
                                    email = uiState.email,
                                )
                            }
                            showDatePicker = false
                        },
                    ) { Text(text = "Đồng ý") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(text = "Hủy")
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EditableAvatarCard(
    avatarBitmap: Bitmap?,
    fallbackText: String,
    avatarFrameId: String?,
    showFrameSelector: Boolean,
    availableFrames: List<AvatarFrame>,
    isLoadingFrames: Boolean,
    onPickImage: (ByteArray, String) -> Unit,
    onShowFrameSelector: () -> Unit,
    onHideFrameSelector: () -> Unit,
    onSelectFrame: (String?) -> Unit,
) {
    val context = LocalContext.current
    val avatarChar = fallbackText.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    val selectedFrame = availableFrames.firstOrNull { it.id == avatarFrameId }
    val frameColor = selectedFrame?.let {
        runCatching { Color(android.graphics.Color.parseColor(it.color)) }.getOrNull()
    } ?: MaterialTheme.colorScheme.primary

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@rememberLauncherForActivityResult
        onPickImage(bytes, mimeType)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showFrameSelector) {
        ModalBottomSheet(
            onDismissRequest = onHideFrameSelector,
            sheetState = sheetState,
        ) {
            EditFrameSelectorContent(
                frames = availableFrames,
                selectedFrameId = avatarFrameId,
                isLoading = isLoadingFrames,
                onSelectFrame = onSelectFrame,
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .border(3.dp, frameColor, CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.profile_avatar_cd),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(104.dp)
                                .clip(CircleShape),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(104.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = avatarChar,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                ) {
                    Icon(
                        imageVector = LucideCamera,
                        contentDescription = stringResource(R.string.profile_camera_cd),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cập nhật ảnh đại diện",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onShowFrameSelector) {
                Text(text = stringResource(R.string.profile_change_frame))
            }
        }
    }
}

@Composable
private fun EditFrameSelectorContent(
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
            text = stringResource(R.string.profile_choose_avatar_frame),
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
                item {
                    EditFrameItem(
                        label = stringResource(R.string.profile_avatar_frame_none),
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
                    EditFrameItem(
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
private fun EditFrameItem(
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
private fun ProfileEditFields(
    nickName: String,
    email: String,
    birthDate: String,
    onNickNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onBirthDateClick: () -> Unit,
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
    )
    val fieldShape = RoundedCornerShape(26.dp)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Biệt danh",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            value = nickName,
            onValueChange = onNickNameChange,
            singleLine = true,
            trailingIcon = { Icon(imageVector = Icons.Rounded.Edit, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape,
            colors = fieldColors,
        )

        Text(
            text = "Email",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            singleLine = true,
            trailingIcon = { Icon(imageVector = Icons.Rounded.Mail, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape,
            colors = fieldColors,
        )

        Text(
            text = "Số điện thoại",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            value = "Chưa cập nhật",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            trailingIcon = { Icon(imageVector = Icons.Rounded.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape,
            colors = fieldColors,
        )

        Text(
            text = "Ngày sinh",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            value = if (birthDate.isBlank()) "Chọn ngày sinh" else birthDate,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            trailingIcon = { Icon(imageVector = Icons.Rounded.CalendarMonth, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBirthDateClick),
            shape = fieldShape,
            colors = fieldColors,
        )
    }
}
