package com.example.mobileproject.presentation.ui.screen.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mobileproject.R
import com.example.mobileproject.data.model.notification.AppNotificationDto
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.viewmodel.NotificationViewModel

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
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (totalItems > 0 && lastVisible >= totalItems - 3 && notifState.hasNextPage && !notifState.isLoading) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = LucideClose,
                            contentDescription = stringResource(R.string.cd_close),
                            tint = colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.surface,
                )
            )
        },
        containerColor = colorScheme.surface,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (notifState.notifications.isEmpty() && !notifState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Không có thông báo nào",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
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
}

@Composable
private fun NotificationItem(item: AppNotificationDto) {
    val colorScheme = MaterialTheme.colorScheme
    val typeIcon = when (item.type.uppercase()) {
        "PAYMENT" -> "💰"
        "TRANSACTION" -> "💸"
        "GOAL_CREATED" -> "🎯"
        "GOAL_UPDATED" -> "📈"
        "GOAL_COMPLETED" -> "🏆"
        "CHAT_MESSAGE" -> "💬"
        "PARTNER_MEMORY" -> "📸"
        else -> "🔔"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (!item.read) {
            colorScheme.primaryContainer.copy(alpha = 0.55f)
        } else {
            colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(text = typeIcon, style = MaterialTheme.typography.titleLarge)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (!item.title.isNullOrBlank()) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!item.read) FontWeight.Bold else FontWeight.SemiBold,
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!item.createdAt.isNullOrBlank()) {
                    Text(
                        text = formatNotificationTime(item.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
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
