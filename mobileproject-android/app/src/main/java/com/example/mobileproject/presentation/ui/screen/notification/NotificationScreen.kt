package com.example.mobileproject.presentation.ui.screen.notification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mobileproject.R
import com.example.mobileproject.data.model.notification.AppNotificationDto
import com.example.mobileproject.presentation.ui.components.core.AppEmptyState
import com.example.mobileproject.presentation.ui.components.core.AppFullScreenLoading
import com.example.mobileproject.presentation.ui.icons.LucideBell
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideImage
import com.example.mobileproject.presentation.ui.icons.LucideReply
import com.example.mobileproject.presentation.ui.icons.LucideShoppingCart
import com.example.mobileproject.presentation.ui.icons.LucideWallet
import com.example.mobileproject.presentation.viewmodel.NotificationViewModel
import kotlinx.coroutines.flow.collectLatest

private data class NotificationVisual(
    val icon: ImageVector,
    val iconContainerColor: Color,
    val iconTint: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavHostController,
    viewModel: NotificationViewModel,
) {
    val notifState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()

    LaunchedEffect(notifState.unreadCount) {
        if (notifState.unreadCount > 0) {
            viewModel.markAllRead()
        }
    }

    LaunchedEffect(listState, notifState.hasNextPage, notifState.isLoading) {
        snapshotFlow { listState.layoutInfo }
            .collectLatest { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (totalItems > 0 && lastVisible >= totalItems - 3 && notifState.hasNextPage && !notifState.isLoading) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.action_notifications),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.surfaceContainerLowest.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.14f)),
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = LucideClose,
                                contentDescription = stringResource(R.string.cd_close),
                                tint = colorScheme.primary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
            )
        },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        if (notifState.isLoading && notifState.notifications.isEmpty()) {
            AppFullScreenLoading(
                modifier = Modifier.padding(paddingValues),
                message = "Đang tải thông báo...",
            )
        } else if (notifState.notifications.isEmpty() && !notifState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                AppEmptyState(
                    title = "Chưa có thông báo",
                    subtitle = "Khi có cập nhật mới về ví, mục tiêu hoặc cuộc trò chuyện, tụi mình sẽ hiện ở đây.",
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(notifState.notifications, key = { it.id }) { item ->
                    NotificationItem(item = item)
                }
                if (notifState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(item: AppNotificationDto) {
    val colorScheme = MaterialTheme.colorScheme
    val visual = resolveNotificationVisual(item.type, colorScheme)
    val cardColor = colorScheme.surfaceContainerLowest.copy(alpha = 0.95f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardColor,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.12f)),
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    shape = CircleShape,
                    color = visual.iconContainerColor,
                    border = BorderStroke(1.dp, visual.iconContainerColor.copy(alpha = 0.65f)),
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        tint = visual.iconTint,
                        modifier = Modifier
                            .padding(11.dp)
                            .size(20.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (!item.title.isNullOrBlank()) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (item.read) FontWeight.SemiBold else FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!item.body.isNullOrBlank()) {
                        Text(
                            text = item.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!item.createdAt.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = colorScheme.surfaceContainerHighest.copy(alpha = 0.78f),
                        ) {
                            Text(
                                text = formatNotificationTime(item.createdAt),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

             if (!item.read) {
                 Box(
                     modifier = Modifier
                         .align(Alignment.TopEnd)
                         .padding(top = 16.dp, end = 16.dp)
                         .size(10.dp)
                         .background(colorScheme.primary, CircleShape),
                 )
             }
        }
    }
}

private fun resolveNotificationVisual(
    itemType: String,
    colorScheme: ColorScheme,
): NotificationVisual {
    return when (itemType.uppercase()) {
        "PAYMENT" -> NotificationVisual(
            icon = LucideWallet,
            iconContainerColor = colorScheme.primary.copy(alpha = 0.10f),
            iconTint = colorScheme.primary,
        )
        "TRANSACTION" -> NotificationVisual(
            icon = LucideShoppingCart,
            iconContainerColor = colorScheme.primary.copy(alpha = 0.10f),
            iconTint = colorScheme.primary,
        )
        "GOAL_CREATED", "GOAL_UPDATED", "GOAL_COMPLETED" -> NotificationVisual(
            icon = LucideBell,
            iconContainerColor = colorScheme.tertiary.copy(alpha = 0.10f),
            iconTint = colorScheme.tertiary,
        )
        "CHAT_MESSAGE" -> NotificationVisual(
            icon = LucideReply,
            iconContainerColor = colorScheme.secondary.copy(alpha = 0.10f),
            iconTint = colorScheme.primary,
        )
        "PARTNER_MEMORY" -> NotificationVisual(
            icon = LucideImage,
            iconContainerColor = colorScheme.tertiary.copy(alpha = 0.10f),
            iconTint = colorScheme.tertiary,
        )
        else -> NotificationVisual(
            icon = LucideBell,
            iconContainerColor = colorScheme.onSurface.copy(alpha = 0.08f),
            iconTint = colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatNotificationTime(createdAt: String): String {
    return try {
        val instant = java.time.Instant.parse(createdAt)
        val now = java.time.Instant.now()
        val diffSeconds = java.time.Duration.between(instant, now).seconds
        when {
            diffSeconds < 60 -> "Vừa xong"
            diffSeconds < 3600 -> "${diffSeconds / 60} phút trước"
            diffSeconds < 86400 -> "${diffSeconds / 3600} giờ trước"
            diffSeconds < 604800 -> "${diffSeconds / 86400} ngày trước"
            else -> {
                val ldt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                "${ldt.dayOfMonth}/${ldt.monthValue}/${ldt.year}"
            }
        }
    } catch (_: Exception) {
        ""
    }
}
