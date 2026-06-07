@file:Suppress("SpellCheckingInspection")

package com.example.mobileproject.presentation.ui.screen.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.PartnerProfileSummary
import com.example.mobileproject.presentation.ui.icons.LucideBell
import com.example.mobileproject.presentation.ui.icons.LucideChevronRight
import com.example.mobileproject.presentation.ui.icons.LucideEdit
import com.example.mobileproject.presentation.ui.icons.LucideHeart
import com.example.mobileproject.presentation.ui.icons.LucideInfo
import com.example.mobileproject.presentation.ui.icons.LucideLink
import com.example.mobileproject.presentation.ui.icons.LucideLogOut
import com.example.mobileproject.presentation.ui.icons.LucidePalette
import com.example.mobileproject.presentation.ui.icons.LucideShield
import com.example.mobileproject.presentation.ui.theme.ThemeMode
import com.example.mobileproject.presentation.viewmodel.NotificationViewModel
import com.example.mobileproject.presentation.viewmodel.ProfileViewModel
import com.example.mobileproject.presentation.viewmodel.ThemeModeViewModel
import com.example.mobileproject.presentation.viewmodel.UserSettingsViewModel

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ProfileScreen(
    accessToken: String,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onInvitePartner: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = hiltViewModel()
    val themeViewModel: ThemeModeViewModel = hiltViewModel()
    val userSettingsViewModel: UserSettingsViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()
    val pushNotificationsEnabled by userSettingsViewModel.pushNotifications.collectAsState()
    val emailNotificationsEnabled by userSettingsViewModel.emailNotifications.collectAsState()
    val notifChat by notificationViewModel.notifChat.collectAsState()
    val notifPayment by notificationViewModel.notifPayment.collectAsState()
    val notifTransaction by notificationViewModel.notifTransaction.collectAsState()
    val notifGoal by notificationViewModel.notifGoal.collectAsState()
    val notifMemory by notificationViewModel.notifMemory.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(accessToken) {
        viewModel.loadProfile(accessToken)
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

        val isPaired = uiState.coupleStatus?.paired == true
        val username = uiState.savedProfile?.username.orEmpty()
        val displayName = uiState.fullName.ifBlank {
            uiState.nickName.ifBlank { username.ifBlank { stringResource(R.string.profile_username_fallback) } }
        }
        val themeLabel = when (themeMode) {
            ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
            ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
            ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileHeroCard(
                displayName = displayName,
                email = uiState.email.ifBlank { stringResource(R.string.profile_email_not_updated) },
                avatarBitmap = uiState.avatarBitmap,
                onEditProfile = onEditProfile,
            )

            AnniversaryCard(
                status = uiState.coupleStatus,
                partnerProfile = uiState.partnerProfile,
                isLoading = uiState.isLoadingCouple,
                coupleError = uiState.coupleError,
            )

            ProfileMenuCard(
                themeLabel = themeLabel,
                themeMode = themeMode,
                onThemeChange = themeViewModel::setThemeMode,
                pushNotificationsEnabled = pushNotificationsEnabled,
                onPushNotificationsChange = userSettingsViewModel::setPushNotifications,
                emailNotificationsEnabled = emailNotificationsEnabled,
                onEmailNotificationsChange = userSettingsViewModel::setEmailNotifications,
                notifChat = notifChat,
                onNotifChatChange = notificationViewModel::setNotifChat,
                notifPayment = notifPayment,
                onNotifPaymentChange = notificationViewModel::setNotifPayment,
                notifTransaction = notifTransaction,
                onNotifTransactionChange = notificationViewModel::setNotifTransaction,
                notifGoal = notifGoal,
                onNotifGoalChange = notificationViewModel::setNotifGoal,
                notifMemory = notifMemory,
                onNotifMemoryChange = notificationViewModel::setNotifMemory,
                onOpenTerms = {
                    val termsUrl = context.getString(R.string.url_terms_of_service)
                    if (termsUrl.isNotBlank()) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, termsUrl.toUri()))
                    }
                },
                onOpenPrivacyPolicy = {
                    val privacyUrl = context.getString(R.string.url_privacy_policy)
                    if (privacyUrl.isNotBlank()) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, privacyUrl.toUri()))
                    }
                },
                onOpenOpenSource = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_open_source_title),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Icon(
                    imageVector = LucideLogOut,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.profile_logout),
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            if (!isPaired) {
                Button(
                    onClick = onInvitePartner,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Icon(
                        imageVector = LucideLink,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.profile_invite_partner),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }

            Text(
                text = stringResource(R.string.profile_version_format, BuildConfig.VERSION_NAME),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ProfileHeroCard(
    displayName: String,
    email: String,
    avatarBitmap: Bitmap?,
    onEditProfile: () -> Unit,
) {
    val avatarText = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(106.dp)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.profile_avatar_cd),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = avatarText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEditProfile),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.profile_edit_personal_info),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = email,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Icon(
                            imageVector = LucideEdit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnniversaryCard(
    status: CoupleStatus?,
    partnerProfile: PartnerProfileSummary?,
    isLoading: Boolean,
    coupleError: String?,
) {
    val partnerName = partnerProfile?.fullName?.takeIf { it.isNotBlank() }
        ?: partnerProfile?.nickName?.takeIf { it.isNotBlank() }
        ?: partnerProfile?.username?.takeIf { it.isNotBlank() }
        ?: status?.partnerUsername
        ?: stringResource(R.string.home_pair_partner_unknown)
    val subtitle = when {
        isLoading -> stringResource(R.string.profile_anniversary_loading)
        status?.paired == true -> {
            val days = status.daysTogether ?: partnerProfile?.daysTogether ?: 0
            stringResource(R.string.home_days_together_format, days)
        }
        !coupleError.isNullOrBlank() -> coupleError
        !status?.outgoingRequestId.isNullOrBlank() -> stringResource(R.string.profile_couple_pending)
        !status?.incomingRequestId.isNullOrBlank() -> stringResource(R.string.profile_couple_incoming)
        else -> stringResource(R.string.profile_couple_none)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LucideHeart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (status?.paired == true) {
                        stringResource(R.string.profile_anniversary_title_with_partner, partnerName)
                    } else {
                        stringResource(R.string.profile_anniversary_title_shared)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = LucideChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileMenuCard(
    themeLabel: String,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    pushNotificationsEnabled: Boolean,
    onPushNotificationsChange: (Boolean) -> Unit,
    emailNotificationsEnabled: Boolean,
    onEmailNotificationsChange: (Boolean) -> Unit,
    notifChat: Boolean,
    onNotifChatChange: (Boolean) -> Unit,
    notifPayment: Boolean,
    onNotifPaymentChange: (Boolean) -> Unit,
    notifTransaction: Boolean,
    onNotifTransactionChange: (Boolean) -> Unit,
    notifGoal: Boolean,
    onNotifGoalChange: (Boolean) -> Unit,
    notifMemory: Boolean,
    onNotifMemoryChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenOpenSource: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        ) {
            ProfileMenuDropdownItem(
                title = stringResource(R.string.settings_privacy_section_title),
                icon = LucideShield,
                trailingText = stringResource(R.string.profile_privacy_rights_count),
            ) {
            }
            ProfileMenuDropdownItem(
                title = stringResource(R.string.settings_notifications_section_title),
                icon = LucideBell,
                trailingText = stringResource(R.string.profile_notifications_count),
            ) {
                SwitchInfoRow(
                    title = stringResource(R.string.settings_push_notifications_title),
                    subtitle = stringResource(R.string.settings_push_notifications_subtitle),
                    checked = pushNotificationsEnabled,
                    onCheckedChange = onPushNotificationsChange,
                )
                SwitchInfoRow(
                    title = stringResource(R.string.settings_email_notifications_title),
                    subtitle = stringResource(R.string.settings_email_notifications_subtitle),
                    checked = emailNotificationsEnabled,
                    onCheckedChange = onEmailNotificationsChange,
                )
                SwitchInfoRow(
                    title = stringResource(R.string.settings_notification_chat_title),
                    subtitle = stringResource(R.string.settings_notification_chat_subtitle),
                    checked = notifChat,
                    onCheckedChange = onNotifChatChange,
                )
                SwitchInfoRow(
                    title = stringResource(R.string.settings_notification_topup_title),
                    subtitle = stringResource(R.string.settings_notification_topup_subtitle),
                    checked = notifPayment,
                    onCheckedChange = onNotifPaymentChange,
                )
                SwitchInfoRow(
                    title = stringResource(R.string.settings_notification_expense_title),
                    subtitle = stringResource(R.string.settings_notification_expense_subtitle),
                    checked = notifTransaction,
                    onCheckedChange = onNotifTransactionChange,
                )
                SwitchInfoRow(
                    title = stringResource(R.string.settings_notification_goal_title),
                    subtitle = stringResource(R.string.settings_notification_goal_subtitle),
                    checked = notifGoal,
                    onCheckedChange = onNotifGoalChange,
                )
                SwitchInfoRow(
                    title = stringResource(R.string.settings_notification_memory_title),
                    subtitle = stringResource(R.string.settings_notification_memory_subtitle),
                    checked = notifMemory,
                    onCheckedChange = onNotifMemoryChange,
                )
            }
            ProfileMenuDropdownItem(
                title = stringResource(R.string.settings_appearance_section_title),
                icon = LucidePalette,
                trailingText = themeLabel,
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf(
                        ThemeMode.SYSTEM to R.string.settings_theme_system,
                        ThemeMode.LIGHT to R.string.settings_theme_light,
                        ThemeMode.DARK to R.string.settings_theme_dark,
                    )
                    options.forEachIndexed { index, option ->
                        val mode = option.first
                        val labelRes = option.second
                        SegmentedButton(
                            selected = themeMode == mode,
                            onClick = { onThemeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        ) {
                            Text(text = stringResource(labelRes))
                        }
                    }
                }
            }
            ProfileMenuDropdownItem(
                title = stringResource(R.string.profile_help_center_title),
                icon = LucideInfo,
            ) {
                Button(
                    onClick = onOpenTerms,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(text = stringResource(R.string.settings_terms_of_service_title))
                }
                Button(
                    onClick = onOpenPrivacyPolicy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(text = stringResource(R.string.settings_privacy_policy_title))
                }
                Button(
                    onClick = onOpenOpenSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(text = stringResource(R.string.settings_open_source_title))
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuDropdownItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingText: String? = null,
    content: @Composable () -> Unit,
) {
    var expanded by remember(title) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!trailingText.isNullOrBlank()) {
                        Text(
                            text = trailingText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        imageVector = LucideChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SwitchInfoRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
