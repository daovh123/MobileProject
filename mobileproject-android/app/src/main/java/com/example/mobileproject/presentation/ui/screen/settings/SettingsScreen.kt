package com.example.mobileproject.presentation.ui.screen.settings

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.components.core.AppSectionHeader
import com.example.mobileproject.presentation.ui.components.core.AppSurfaceCard
import com.example.mobileproject.presentation.ui.icons.LucideBell
import com.example.mobileproject.presentation.ui.icons.LucideChevronRight
import com.example.mobileproject.presentation.ui.icons.LucideInfo
import com.example.mobileproject.presentation.ui.icons.LucideLogOut
import com.example.mobileproject.presentation.ui.icons.LucidePalette
import com.example.mobileproject.presentation.ui.icons.LucideShield
import com.example.mobileproject.presentation.ui.icons.LucideUser
import com.example.mobileproject.presentation.ui.screen.login.LoginActivity
import com.example.mobileproject.presentation.ui.theme.ThemeMode
import com.example.mobileproject.presentation.viewmodel.AuthViewModel
import com.example.mobileproject.presentation.viewmodel.ThemeModeViewModel
import com.example.mobileproject.presentation.viewmodel.UserSettingsViewModel


@Composable
fun SettingsScreen(
    accessToken: String,
    onOpenProfileEdit: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()

    val themeViewModel: ThemeModeViewModel = hiltViewModel()
    val themeMode by themeViewModel.themeMode.collectAsState()

    var logoutRequested by remember { mutableStateOf(false) }
    var logoutStatusText by remember { mutableStateOf<String?>(null) }
    var logoutStatusIsError by remember { mutableStateOf(false) }

    val settingsViewModel: UserSettingsViewModel = hiltViewModel()
    val pushNotificationsEnabled by settingsViewModel.pushNotifications.collectAsState()
    val emailNotificationsEnabled by settingsViewModel.emailNotifications.collectAsState()
    val showActivityStatus by settingsViewModel.showActivityStatus.collectAsState()
    val searchableByEmail by settingsViewModel.searchableByEmail.collectAsState()

    fun navigateToLogin() {
        val intent = Intent(context, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        activity?.finish()
    }

    LaunchedEffect(authState.logoutCompleted) {
        if (authState.logoutCompleted) {
            authViewModel.consumeLogoutSuccess()
            logoutRequested = false
            logoutStatusText = null
            logoutStatusIsError = false
            Toast.makeText(context, context.getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    LaunchedEffect(authState.isLoading, logoutRequested) {
        if (logoutRequested && authState.isLoading) {
            logoutStatusText = context.getString(R.string.settings_logout_processing)
            logoutStatusIsError = false
        }
    }

    LaunchedEffect(authState.errorMessage) {
        val message = authState.errorMessage
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        AppSectionHeader(
            title = stringResource(R.string.settings_account_section_title),
            subtitle = stringResource(R.string.settings_account_section_subtitle),
        )

        AppSurfaceCard {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                if (onOpenProfileEdit != null) {
                    SettingsListItem(
                        title = stringResource(R.string.settings_profile_edit_title),
                        subtitle = stringResource(R.string.settings_profile_edit_subtitle),
                        icon = LucideUser,
                        onClick = onOpenProfileEdit,
                    )
                }

                SettingsListItem(
                    title = stringResource(R.string.settings_logout_button),
                    subtitle = stringResource(R.string.settings_logout_hint),
                    icon = LucideLogOut,
                    onClick = {
                        if (logoutRequested) {
                            return@SettingsListItem
                        }
                        if (accessToken.isBlank()) {
                            navigateToLogin()
                            return@SettingsListItem
                        }
                        logoutRequested = true
                        logoutStatusText = null
                        logoutStatusIsError = false
                        authViewModel.logout(accessToken)
                    },
                )

                val statusText = logoutStatusText
                if (!statusText.isNullOrBlank()) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (logoutStatusIsError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
            }
        }

        AppSectionHeader(
            title = stringResource(R.string.settings_appearance_section_title),
            subtitle = stringResource(R.string.settings_appearance_section_subtitle),
        )

        AppSurfaceCard {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                SettingsListItem(
                    title = stringResource(R.string.settings_theme_title),
                    subtitle = stringResource(R.string.settings_theme_subtitle),
                    icon = LucidePalette,
                )

                ThemeModeSelector(
                    selected = themeMode,
                    onSelected = themeViewModel::setThemeMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }

        AppSectionHeader(
            title = stringResource(R.string.settings_notifications_section_title),
            subtitle = stringResource(R.string.settings_notifications_section_subtitle),
        )

        AppSurfaceCard {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsToggleItem(
                    title = stringResource(R.string.settings_push_notifications_title),
                    subtitle = stringResource(R.string.settings_push_notifications_subtitle),
                    icon = LucideBell,
                    checked = pushNotificationsEnabled,
                    onCheckedChange = settingsViewModel::setPushNotifications,
                )
                SettingsToggleItem(
                    title = stringResource(R.string.settings_email_notifications_title),
                    subtitle = stringResource(R.string.settings_email_notifications_subtitle),
                    icon = LucideBell,
                    checked = emailNotificationsEnabled,
                    onCheckedChange = settingsViewModel::setEmailNotifications,
                )
            }
        }

        AppSectionHeader(
            title = stringResource(R.string.settings_privacy_section_title),
            subtitle = stringResource(R.string.settings_privacy_section_subtitle),
        )

        AppSurfaceCard {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsToggleItem(
                    title = stringResource(R.string.settings_show_activity_title),
                    subtitle = stringResource(R.string.settings_show_activity_subtitle),
                    icon = LucideShield,
                    checked = showActivityStatus,
                    onCheckedChange = settingsViewModel::setShowActivityStatus,
                )
                SettingsToggleItem(
                    title = stringResource(R.string.settings_searchable_by_email_title),
                    subtitle = stringResource(R.string.settings_searchable_by_email_subtitle),
                    icon = LucideShield,
                    checked = searchableByEmail,
                    onCheckedChange = settingsViewModel::setSearchableByEmail,
                )
            }
        }

        AppSectionHeader(
            title = stringResource(R.string.settings_about_section_title),
            subtitle = stringResource(R.string.settings_about_section_subtitle),
        )

        AppSurfaceCard {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsListItem(
                    title = stringResource(R.string.settings_app_version_title),
                    subtitle = BuildConfig.VERSION_NAME,
                    icon = LucideInfo,
                )
                val termsUrl = stringResource(R.string.url_terms_of_service)
                SettingsListItem(
                    title = stringResource(R.string.settings_terms_of_service_title),
                    subtitle = stringResource(R.string.settings_terms_of_service_subtitle),
                    icon = LucideInfo,
                    onClick = if (termsUrl.isNotBlank()) {
                        { context.startActivity(Intent(Intent.ACTION_VIEW, termsUrl.toUri())) }
                    } else null,
                )
                val privacyUrl = stringResource(R.string.url_privacy_policy)
                SettingsListItem(
                    title = stringResource(R.string.settings_privacy_policy_title),
                    subtitle = stringResource(R.string.settings_privacy_policy_subtitle),
                    icon = LucideShield,
                    onClick = if (privacyUrl.isNotBlank()) {
                        { context.startActivity(Intent(Intent.ACTION_VIEW, privacyUrl.toUri())) }
                    } else null,
                )
                val openSourceTitle = stringResource(R.string.settings_open_source_title)
                SettingsListItem(
                    title = openSourceTitle,
                    subtitle = stringResource(R.string.settings_open_source_subtitle),
                    icon = LucideInfo,
                    onClick = {
                        Toast.makeText(context, openSourceTitle, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun SettingsListItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null,
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        Modifier.fillMaxWidth()
    }

    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        trailingContent = {
            if (onClick != null) {
                Icon(
                    imageVector = LucideChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        modifier = modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        ThemeMode.SYSTEM to R.string.settings_theme_system,
        ThemeMode.LIGHT to R.string.settings_theme_light,
        ThemeMode.DARK to R.string.settings_theme_dark,
    )

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, option ->
            val mode = option.first
            val labelRes = option.second
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) {
                Text(text = stringResource(labelRes))
            }
        }
    }
}
