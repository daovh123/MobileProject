package com.example.mobileproject.presentation.ui.screen.app.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.mobileproject.R
import com.example.mobileproject.data.model.notification.AppNotificationDto
import com.example.mobileproject.presentation.ui.icons.LucideBell
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideUser
import com.example.mobileproject.presentation.viewmodel.NotificationUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeTopBar(
    navController: NavHostController,
    currentRoute: String,
    isChatRoute: Boolean,
    isProfileRoute: Boolean,
    isProfileEditRoute: Boolean,
    isMemoriesRoute: Boolean,
    notifState: NotificationUiState,
    onMarkAllRead: () -> Unit,
    onLoadNextPage: () -> Unit,
    isHomeRoute: Boolean,
    isWalletRoute: Boolean,
    isExploreRoute: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    val unreadCount = notifState.unreadCount
    val hasUnread = unreadCount > 0
    val badgeText = when {
        unreadCount > 9 -> "9+"
        unreadCount > 0 -> unreadCount.toString()
        else -> null
    }
    var isNotificationsOpen by remember { mutableStateOf(false) }
    val shakeTransition = rememberInfiniteTransition(label = "bell-shake")
    val shakeRotation by shakeTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bell-rotation",
    )

    LaunchedEffect(isNotificationsOpen) {
        if (isNotificationsOpen && hasUnread) {
            onMarkAllRead()
        }
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = when {
                    isChatRoute -> stringResource(R.string.chat_title)
                    isProfileEditRoute -> stringResource(R.string.profile_edit_title)
                    isProfileRoute -> stringResource(R.string.profile_title)
                    isMemoriesRoute -> stringResource(R.string.memories_title)
                    isHomeRoute -> stringResource(id = R.string.cd_home)
                    isWalletRoute -> stringResource(id = R.string.cd_wallet)
                    isExploreRoute -> stringResource(id = R.string.cd_explore)
                    else -> ""
                },
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSurface,
            )
        },
        navigationIcon = {
            if (isChatRoute || isProfileEditRoute) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = LucideClose,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = colorScheme.primary,
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        navController.navigate(HomeRoutes.PROFILE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                ) {
                    Icon(
                        imageVector = LucideUser,
                        contentDescription = stringResource(R.string.cd_open_profile),
                        tint = colorScheme.primary,
                    )
                }
            }
        },
        actions = {
            if (!isChatRoute && !isProfileEditRoute) {
                IconButton(onClick = { isNotificationsOpen = true }) {
                    BadgedBox(
                        badge = {
                            if (badgeText != null) {
                                Badge {
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = LucideBell,
                            contentDescription = stringResource(R.string.action_notifications),
                            tint = colorScheme.primary,
                            modifier = Modifier.rotate(if (hasUnread) shakeRotation else 0f),
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.surface.copy(alpha = 0.98f),
            scrolledContainerColor = colorScheme.surfaceColorAtElevation(3.dp),
            titleContentColor = colorScheme.onSurface,
            navigationIconContentColor = colorScheme.primary,
            actionIconContentColor = colorScheme.primary,
        ),
        modifier = Modifier.padding(bottom = 4.dp),
    )

    if (isNotificationsOpen) {
        NotificationBottomSheet(
            notifState = notifState,
            onDismiss = { isNotificationsOpen = false },
            onLoadNextPage = onLoadNextPage,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationBottomSheet(
    notifState: NotificationUiState,
    onDismiss: () -> Unit,
    onLoadNextPage: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()

    // Infinite scroll: load next page when near bottom
    LaunchedEffect(listState, notifState.hasNextPage, notifState.isLoading) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (totalItems > 0 && lastVisible >= totalItems - 3 && notifState.hasNextPage && !notifState.isLoading) {
                    onLoadNextPage()
                }
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.action_notifications),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
            }

            if (notifState.notifications.isEmpty() && !notifState.isLoading) {
                Text(
                    text = stringResource(R.string.memories_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(notifState.notifications, key = { it.id }) { item ->
                        NotificationItem(item = item)
                    }
                    if (notifState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
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
