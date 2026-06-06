package com.example.mobileproject.presentation.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.presentation.ui.icons.LucideBell
import com.example.mobileproject.presentation.viewmodel.NotificationViewModel

/**
 * Phần cài đặt thông báo theo nhóm – component tái sử dụng hiển thị
 * các toggle cho từng loại thông báo.
 *
 * Bao gồm 5 nhóm: Tin nhắn, Nạp tiền, Chi tiêu, Mục tiêu, Kỷ niệm.
 * Mỗi nhóm dùng [NotifGroupToggle] (ListItem + Switch).
 *
 * ViewModel: [NotificationViewModel] – notifChat/Payment/Transaction/Goal/Memory states.
 */
@Composable
fun NotificationSettingsSection() {
    val viewModel: NotificationViewModel = hiltViewModel()

    val notifChat by viewModel.notifChat.collectAsState()
    val notifPayment by viewModel.notifPayment.collectAsState()
    val notifTransaction by viewModel.notifTransaction.collectAsState()
    val notifGoal by viewModel.notifGoal.collectAsState()
    val notifMemory by viewModel.notifMemory.collectAsState()

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        NotifGroupToggle(
            title = "Tin nhắn",
            subtitle = "Thông báo khi partner gửi tin nhắn mới",
            icon = LucideBell,
            checked = notifChat,
            onCheckedChange = viewModel::setNotifChat,
        )
        NotifGroupToggle(
            title = "Nạp tiền",
            subtitle = "Thông báo khi có giao dịch nạp tiền vào ví",
            icon = LucideBell,
            checked = notifPayment,
            onCheckedChange = viewModel::setNotifPayment,
        )
        NotifGroupToggle(
            title = "Chi tiêu",
            subtitle = "Thông báo khi tạo giao dịch chi tiêu mới",
            icon = LucideBell,
            checked = notifTransaction,
            onCheckedChange = viewModel::setNotifTransaction,
        )
        NotifGroupToggle(
            title = "Mục tiêu",
            subtitle = "Thông báo tạo, cập nhật và hoàn thành mục tiêu",
            icon = LucideBell,
            checked = notifGoal,
            onCheckedChange = viewModel::setNotifGoal,
        )
        NotifGroupToggle(
            title = "Kỷ niệm",
            subtitle = "Thông báo khi partner chia sẻ kỷ niệm mới",
            icon = LucideBell,
            checked = notifMemory,
            onCheckedChange = viewModel::setNotifMemory,
        )
    }
}

@Composable
private fun NotifGroupToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
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
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
