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
                IconButton(onClick = { navController.navigate(HomeRoutes.NOTIFICATIONS) }) {
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
            containerColor = colorScheme.surface.copy(alpha = 0.95f),
            scrolledContainerColor = colorScheme.surfaceColorAtElevation(3.dp),
            titleContentColor = colorScheme.onSurface,
            navigationIconContentColor = colorScheme.primary,
            actionIconContentColor = colorScheme.primary,
        ),
        modifier = Modifier.padding(bottom = 2.dp),
    )
}

