package com.example.mobileproject.presentation.ui.screen.app.compose

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.presentation.notification.ChatInAppNotificationBus
import com.example.mobileproject.presentation.notification.ChatNotificationGate
import com.example.mobileproject.presentation.ui.screen.chat.ChatScreen
import com.example.mobileproject.presentation.ui.screen.explore.ExploreRoute
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.screen.home.HomeScreen
import com.example.mobileproject.presentation.ui.screen.memories.MemoriesScreen
import com.example.mobileproject.presentation.ui.screen.profile.ProfileScreen
import com.example.mobileproject.presentation.ui.screen.settings.SettingsScreen
import com.example.mobileproject.presentation.ui.screen.wallet.WalletScreen

object HomeRoutes {
    const val HOME: String = "home"
    const val WALLET: String = "wallet"
    const val EXPLORE: String = "explore"
    const val MEMORIES: String = "memories"
    const val SETTINGS: String = "settings"
    const val PROFILE: String = "profile"
    const val CHAT: String = "chat"
}

private data class HomeBottomItem(
    val route: String,
    val titleRes: Int,
    val iconRes: Int,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScaffold(
    accessToken: String,
    apiService: ApiService,
    onNavControllerReady: (NavHostController) -> Unit,
) {
    val navController = rememberNavController()

    DisposableEffect(navController) {
        onNavControllerReady(navController)
        onDispose { }
    }

    val items = remember {
        listOf(
            HomeBottomItem(HomeRoutes.HOME, R.string.page_home, R.drawable.ic_home_24),
            HomeBottomItem(HomeRoutes.WALLET, R.string.page_wallet, R.drawable.ic_wallet_24),
            HomeBottomItem(HomeRoutes.EXPLORE, R.string.page_explore, R.drawable.ic_explore_24),
            HomeBottomItem(HomeRoutes.MEMORIES, R.string.page_memories, R.drawable.ic_memories_24),
            HomeBottomItem(HomeRoutes.SETTINGS, R.string.settings_title, R.drawable.ic_menu_24),
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: HomeRoutes.HOME
    val currentItem = items.firstOrNull { it.route == currentRoute } ?: items.first()
    val isMemoriesRoute = currentRoute == HomeRoutes.MEMORIES
    val isProfileRoute = currentRoute == HomeRoutes.PROFILE
    val isChatRoute = currentRoute == HomeRoutes.CHAT
    val snackbarHostState = remember { SnackbarHostState() }

    val openChatActionLabel = stringResource(R.string.chat_in_app_open_action)
    val inAppMessageFormat = stringResource(R.string.chat_in_app_message_format)

    LaunchedEffect(navController, openChatActionLabel, inAppMessageFormat) {
        ChatInAppNotificationBus.events.collect { event ->
            val preview = event.messageText.trim().replace(Regex("\\s+"), " ")
            val message = inAppMessageFormat.format(
                event.senderUsername.ifBlank { event.conversationTitle },
                preview,
            )

            val snackbarResult = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = openChatActionLabel,
                withDismissAction = true,
            )

            if (snackbarResult == SnackbarResult.ActionPerformed) {
                navController.navigate(HomeRoutes.CHAT) {
                    launchSingleTop = true
                }
            }
        }
    }

    DisposableEffect(isChatRoute) {
        ChatNotificationGate.setChatRouteActive(isChatRoute)
        onDispose {
            ChatNotificationGate.setChatRouteActive(false)
        }
    }

    val context = LocalContext.current
    val activity = context as? Activity

    BackHandler {
        if (currentRoute == HomeRoutes.CHAT) {
            navController.popBackStack()
        } else if (currentRoute != HomeRoutes.HOME) {
            navController.navigate(HomeRoutes.HOME) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            activity?.finish()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    if (isChatRoute) {
                        Text(
                            text = stringResource(R.string.chat_title),
                            color = colorResource(R.color.md3_primary),
                        )
                    } else if (isProfileRoute) {
                        Text(
                            text = stringResource(R.string.profile_title),
                            color = colorResource(R.color.md3_primary),
                        )
                    } else if (!isMemoriesRoute) {
                        Text(
                            text = stringResource(currentItem.titleRes),
                            color = colorResource(R.color.md3_primary),
                        )
                    }
                },
                navigationIcon = {
                    if (isChatRoute) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close_24),
                                contentDescription = stringResource(R.string.cd_close),
                                tint = colorResource(R.color.md3_primary),
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                navController.navigate(HomeRoutes.PROFILE) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_avatar_24),
                                contentDescription = stringResource(R.string.cd_open_profile),
                                tint = colorResource(R.color.md3_primary),
                            )
                        }
                    }
                },
                actions = {
                    if (!isChatRoute) {
                        IconButton(onClick = { /* TODO: notifications */ }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_notifications_24),
                                contentDescription = stringResource(R.string.action_notifications),
                                tint = colorResource(R.color.md3_primary),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isMemoriesRoute) {
                        colorResource(R.color.auth_bg_top)
                    } else {
                        colorResource(R.color.md3_surface)
                    },
                ),
            )
        },
        bottomBar = {
            if (!isChatRoute && !isProfileRoute) {
                Card(
                    modifier = Modifier
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp, top = 6.dp)
                        .navigationBarsPadding(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.nav_glass_surface),
                    ),
                    border = BorderStroke(1.dp, colorResource(R.color.nav_glass_stroke)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 9.dp),
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                    ) {
                        items.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(item.iconRes),
                                        contentDescription = stringResource(item.titleRes),
                                    )
                                },
                                label = null,
                                alwaysShowLabel = false,
                            )
                        }
                    }
                }
            }
        },
        containerColor = colorResource(R.color.md3_surface_variant),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            NavHost(
                navController = navController,
                startDestination = HomeRoutes.HOME,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(HomeRoutes.HOME) {
                    HomeScreen(
                        accessToken = accessToken,
                        apiService = apiService,
                    )
                }
                composable(HomeRoutes.WALLET) {
                    WalletScreen()
                }
                composable(HomeRoutes.EXPLORE) {
                    ExploreRoute(accessToken = accessToken)
                }
                composable(HomeRoutes.MEMORIES) {
                    MemoriesScreen(
                        accessToken = accessToken,
                        onNavigateToExplore = {
                            navController.navigate(HomeRoutes.EXPLORE) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
                composable(HomeRoutes.SETTINGS) {
                    SettingsScreen(accessToken = accessToken)
                }
                composable(HomeRoutes.PROFILE) {
                    ProfileScreen(
                        accessToken = accessToken,
                        onNavigateBack = { navController.popBackStack() },
                        onLogout = {
                            (context as? HomeActivity)?.logoutAndOpenLogin()
                        },
                    )
                }
                composable(HomeRoutes.CHAT) {
                    ChatScreen(accessToken = accessToken)
                }
            }

            if (!isChatRoute) {
                DraggableChatButton(
                    onClick = {
                        navController.navigate(HomeRoutes.CHAT) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun DraggableChatButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val buttonSize = 56.dp
        val buttonSizePx = with(density) { buttonSize.toPx() }
        val marginPx = with(density) { 18.dp.toPx() }

        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val maxX = (maxWidthPx - buttonSizePx).coerceAtLeast(0f)
        val maxY = (maxHeightPx - buttonSizePx).coerceAtLeast(0f)

        val initialX = (maxX - marginPx).coerceAtLeast(0f)
        val initialY = (maxY - marginPx).coerceAtLeast(0f)

        var offsetX by rememberSaveable { mutableStateOf(Float.NaN) }
        var offsetY by rememberSaveable { mutableStateOf(Float.NaN) }

        LaunchedEffect(maxX, maxY, initialX, initialY) {
            if (offsetX.isNaN() || offsetY.isNaN()) {
                offsetX = initialX
                offsetY = initialY
            } else {
                offsetX = offsetX.coerceIn(0f, maxX)
                offsetY = offsetY.coerceIn(0f, maxY)
            }
        }

        Surface(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                .size(buttonSize)
                .pointerInput(maxX, maxY) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY)
                    }
                }
                .clickable(onClick = onClick),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = colorResource(R.color.md3_primary),
            shadowElevation = 10.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.chat_fab_label),
                    color = colorResource(R.color.md3_on_primary),
                )
            }
        }
    }
}
