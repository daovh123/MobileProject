package com.example.mobileproject.presentation.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.components.core.AppSectionHeader
import com.example.mobileproject.presentation.ui.components.core.AppSurfaceCard
import com.example.mobileproject.presentation.ui.icons.LucideBell
import com.example.mobileproject.presentation.ui.icons.LucideChevronRight
import com.example.mobileproject.presentation.ui.icons.LucideInfo
import com.example.mobileproject.presentation.ui.icons.LucidePalette
import com.example.mobileproject.presentation.ui.icons.LucideShield

@Composable
fun SettingsScreen(
    accessToken: String,
    onOpenProfileEdit: (() -> Unit)? = null,
    onOpenPrivacy: (() -> Unit)? = null,
    onOpenNotifications: (() -> Unit)? = null,
    onOpenAppearance: (() -> Unit)? = null,
    onOpenHelp: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        AppSectionHeader(
            title = stringResource(R.string.settings_title),
            subtitle = "Chọn mục để mở cài đặt chi tiết riêng",
        )

        AppSurfaceCard {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsMenuItem(
                    title = "Bảo mật & Quyền riêng tư",
                    subtitle = "Quản lý đăng nhập, trạng thái và quyền hiển thị.",
                    icon = LucideShield,
                    onClick = onOpenPrivacy,
                )
                SettingsMenuItem(
                    title = stringResource(R.string.settings_notifications_section_title),
                    subtitle = "Thiết lập các nhóm thông báo bạn muốn nhận.",
                    icon = LucideBell,
                    onClick = onOpenNotifications,
                )
                SettingsMenuItem(
                    title = stringResource(R.string.settings_appearance_section_title),
                    subtitle = "Chọn chế độ nền theo sở thích của bạn.",
                    icon = LucidePalette,
                    onClick = onOpenAppearance,
                )
                SettingsMenuItem(
                    title = "Trung tâm trợ giúp",
                    subtitle = "Xem phiên bản, điều khoản và chính sách.",
                    icon = LucideInfo,
                    onClick = onOpenHelp,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun SettingsMenuItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)?,
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
