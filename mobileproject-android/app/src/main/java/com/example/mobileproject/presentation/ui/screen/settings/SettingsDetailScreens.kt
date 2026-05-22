package com.example.mobileproject.presentation.ui.screen.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import com.example.mobileproject.presentation.ui.theme.ThemeMode
import com.example.mobileproject.presentation.viewmodel.NotificationViewModel
import com.example.mobileproject.presentation.viewmodel.ThemeModeViewModel
import com.example.mobileproject.presentation.viewmodel.UserSettingsViewModel

@Composable
fun SettingsPrivacyScreen(
    accessToken: String,
    onLogout: () -> Unit,
) {
    val settingsViewModel: UserSettingsViewModel = hiltViewModel()
    val showActivityStatus by settingsViewModel.showActivityStatus.collectAsState()
    val searchableByEmail by settingsViewModel.searchableByEmail.collectAsState()

    SettingsSectionLayout(
        title = "Bảo mật & Quyền riêng tư",
        subtitle = "Thông tin đăng nhập và quyền hiển thị của bạn",
    ) {
        AppSurfaceCard {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                DropdownSettingItem(
                    title = stringResource(R.string.settings_show_activity_title),
                    summary = stringResource(R.string.settings_show_activity_subtitle),
                    icon = LucideShield,
                    defaultExpanded = true,
                ) {
                    SwitchRow(
                        title = "Cho phép hiển thị",
                        checked = showActivityStatus,
                        onCheckedChange = settingsViewModel::setShowActivityStatus,
                    )
                }

                DropdownSettingItem(
                    title = stringResource(R.string.settings_searchable_by_email_title),
                    summary = stringResource(R.string.settings_searchable_by_email_subtitle),
                    icon = LucideShield,
                ) {
                    SwitchRow(
                        title = "Cho phép tìm kiếm",
                        checked = searchableByEmail,
                        onCheckedChange = settingsViewModel::setSearchableByEmail,
                    )
                }

                DropdownSettingItem(
                    title = stringResource(R.string.settings_logout_button),
                    summary = stringResource(R.string.settings_logout_hint),
                    icon = LucideLogOut,
                ) {
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(text = stringResource(R.string.profile_logout))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsNotificationsScreen() {
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val settingsViewModel: UserSettingsViewModel = hiltViewModel()

    val notifChat by notificationViewModel.notifChat.collectAsState()
    val notifPayment by notificationViewModel.notifPayment.collectAsState()
    val notifTransaction by notificationViewModel.notifTransaction.collectAsState()
    val notifGoal by notificationViewModel.notifGoal.collectAsState()
    val notifMemory by notificationViewModel.notifMemory.collectAsState()
    val pushNotificationsEnabled by settingsViewModel.pushNotifications.collectAsState()
    val emailNotificationsEnabled by settingsViewModel.emailNotifications.collectAsState()

    SettingsSectionLayout(
        title = stringResource(R.string.settings_notifications_section_title),
        subtitle = "Chọn nhóm thông báo bạn muốn nhận",
    ) {
        AppSurfaceCard {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                DropdownSettingItem(
                    title = stringResource(R.string.settings_push_notifications_title),
                    summary = stringResource(R.string.settings_push_notifications_subtitle),
                    icon = LucideBell,
                    defaultExpanded = true,
                ) {
                    SwitchRow(
                        title = "Bật thông báo đẩy",
                        checked = pushNotificationsEnabled,
                        onCheckedChange = settingsViewModel::setPushNotifications,
                    )
                }
                DropdownSettingItem(
                    title = stringResource(R.string.settings_email_notifications_title),
                    summary = stringResource(R.string.settings_email_notifications_subtitle),
                    icon = LucideBell,
                ) {
                    SwitchRow(
                        title = "Bật thông báo email",
                        checked = emailNotificationsEnabled,
                        onCheckedChange = settingsViewModel::setEmailNotifications,
                    )
                }
                DropdownSettingItem(
                    title = "Tin nhắn",
                    summary = "Thông báo khi partner gửi tin nhắn mới",
                    icon = LucideBell,
                ) {
                    SwitchRow(
                        title = "Bật thông báo tin nhắn",
                        checked = notifChat,
                        onCheckedChange = notificationViewModel::setNotifChat,
                    )
                }
                DropdownSettingItem(
                    title = "Nạp tiền",
                    summary = "Thông báo khi có giao dịch nạp tiền",
                    icon = LucideBell,
                ) {
                    SwitchRow(
                        title = "Bật thông báo nạp tiền",
                        checked = notifPayment,
                        onCheckedChange = notificationViewModel::setNotifPayment,
                    )
                }
                DropdownSettingItem(
                    title = "Chi tiêu",
                    summary = "Thông báo khi có giao dịch chi tiêu mới",
                    icon = LucideBell,
                ) {
                    SwitchRow(
                        title = "Bật thông báo chi tiêu",
                        checked = notifTransaction,
                        onCheckedChange = notificationViewModel::setNotifTransaction,
                    )
                }
                DropdownSettingItem(
                    title = "Mục tiêu",
                    summary = "Thông báo tạo và hoàn thành mục tiêu",
                    icon = LucideBell,
                ) {
                    SwitchRow(
                        title = "Bật thông báo mục tiêu",
                        checked = notifGoal,
                        onCheckedChange = notificationViewModel::setNotifGoal,
                    )
                }
                DropdownSettingItem(
                    title = "Kỷ niệm",
                    summary = "Thông báo khi có kỷ niệm mới",
                    icon = LucideBell,
                ) {
                    SwitchRow(
                        title = "Bật thông báo kỷ niệm",
                        checked = notifMemory,
                        onCheckedChange = notificationViewModel::setNotifMemory,
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsAppearanceScreen() {
    val themeViewModel: ThemeModeViewModel = hiltViewModel()
    val themeMode by themeViewModel.themeMode.collectAsState()

    SettingsSectionLayout(
        title = stringResource(R.string.settings_appearance_section_title),
        subtitle = stringResource(R.string.settings_appearance_section_subtitle),
    ) {
        AppSurfaceCard {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                DropdownSettingItem(
                    title = stringResource(R.string.settings_theme_title),
                    summary = stringResource(R.string.settings_theme_subtitle),
                    icon = LucidePalette,
                    defaultExpanded = true,
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
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
                                onClick = { themeViewModel.setThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                            ) {
                                Text(text = stringResource(labelRes))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsHelpScreen() {
    val context = LocalContext.current
    val termsUrl = stringResource(R.string.url_terms_of_service)
    val privacyUrl = stringResource(R.string.url_privacy_policy)
    val openSourceTitle = stringResource(R.string.settings_open_source_title)

    SettingsSectionLayout(
        title = "Trung tâm trợ giúp",
        subtitle = "Thông tin ứng dụng và tài liệu liên quan",
    ) {
        AppSurfaceCard {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                DropdownSettingItem(
                    title = stringResource(R.string.settings_app_version_title),
                    summary = BuildConfig.VERSION_NAME,
                    icon = LucideInfo,
                    defaultExpanded = true,
                ) {
                    Text(
                        text = "Phiên bản hiện tại của ứng dụng trên thiết bị này.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                DropdownSettingItem(
                    title = stringResource(R.string.settings_terms_of_service_title),
                    summary = stringResource(R.string.settings_terms_of_service_subtitle),
                    icon = LucideInfo,
                ) {
                    Button(
                        onClick = { if (termsUrl.isNotBlank()) context.startActivity(Intent(Intent.ACTION_VIEW, termsUrl.toUri())) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text(text = "Mở điều khoản dịch vụ")
                    }
                }
                DropdownSettingItem(
                    title = stringResource(R.string.settings_privacy_policy_title),
                    summary = stringResource(R.string.settings_privacy_policy_subtitle),
                    icon = LucideShield,
                ) {
                    Button(
                        onClick = { if (privacyUrl.isNotBlank()) context.startActivity(Intent(Intent.ACTION_VIEW, privacyUrl.toUri())) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text(text = "Mở chính sách quyền riêng tư")
                    }
                }
                DropdownSettingItem(
                    title = openSourceTitle,
                    summary = stringResource(R.string.settings_open_source_subtitle),
                    icon = LucideInfo,
                ) {
                    Button(
                        onClick = { Toast.makeText(context, openSourceTitle, Toast.LENGTH_SHORT).show() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text(text = "Xem thông tin")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionLayout(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppSectionHeader(
            title = title,
            subtitle = subtitle,
        )
        content()
    }
}

@Composable
private fun DropdownSettingItem(
    title: String,
    summary: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    defaultExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by remember(title) { mutableStateOf(defaultExpanded) }
    val chevron = if (expanded) "Thu gọn" else "Mở rộng"

    Column(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(text = title) },
            supportingContent = { Text(text = summary) },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expanded = if (expanded) false else true },
                ) {
                    Text(
                        text = chevron,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        imageVector = LucideChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = if (expanded) false else true },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
