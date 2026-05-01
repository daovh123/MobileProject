package com.example.mobileproject.presentation.ui.screen.app.compose

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import com.example.mobileproject.presentation.ui.icons.LucideBell
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideUser
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
import com.example.mobileproject.presentation.ui.navigation.AppNavigationBar
import com.example.mobileproject.presentation.ui.navigation.NavigationConfig
import com.example.mobileproject.presentation.ui.screen.add_expense.AddExpenseScreen
import com.example.mobileproject.presentation.ui.screen.chat.ChatScreen
import com.example.mobileproject.presentation.ui.screen.explore.ExploreRoute
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.screen.home.HomeScreen
import com.example.mobileproject.presentation.ui.screen.memories.MemoriesScreen
import com.example.mobileproject.presentation.ui.screen.profile.ProfileScreen
import com.example.mobileproject.presentation.ui.screen.settings.SettingsScreen
import com.example.mobileproject.presentation.ui.screen.wallet.RecentTransactionsScreen
import com.example.mobileproject.presentation.ui.screen.wallet.SavingGoalsScreen
import com.example.mobileproject.presentation.ui.screen.wallet.TopUpScreen
import com.example.mobileproject.presentation.ui.screen.wallet.WalletScreen

object HomeRoutes {
    const val HOME: String = "home"
    const val WALLET: String = "wallet"
    const val EXPLORE: String = "explore"
    const val MEMORIES: String = "memories"
    const val SETTINGS: String = "settings"
    const val PROFILE: String = "profile"
    const val CHAT: String = "chat"
    const val ADD_EXPENSE: String = "add_expense"
    const val TOP_UP: String = "top_up"
    const val RECENT_TRANSACTIONS: String = "recent_transactions"
    const val SAVING_GOALS: String = "saving_goals"
}

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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: HomeRoutes.HOME
    val currentItem = NavigationConfig.getItemByRoute(currentRoute)
        ?: NavigationConfig.navigationItems.first()
    val isMemoriesRoute = currentRoute == HomeRoutes.MEMORIES
    val isProfileRoute = currentRoute == HomeRoutes.PROFILE
    val isChatRoute = currentRoute == HomeRoutes.CHAT
    val isAddExpenseRoute = currentRoute == HomeRoutes.ADD_EXPENSE
    val isTopUpRoute = currentRoute == HomeRoutes.TOP_UP
    val isRecentTransactionsRoute = currentRoute == HomeRoutes.RECENT_TRANSACTIONS
    val isSavingGoalsRoute = currentRoute == HomeRoutes.SAVING_GOALS
    
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme
    val scaffoldBackgroundBrush = remember(colorScheme) {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.surface,
                colorScheme.surfaceVariant.copy(alpha = 0.95f),
                colorScheme.background,
            ),
        )
    }

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
        if (currentRoute == HomeRoutes.CHAT || currentRoute == HomeRoutes.ADD_EXPENSE || currentRoute == HomeRoutes.TOP_UP || currentRoute == HomeRoutes.RECENT_TRANSACTIONS || currentRoute == HomeRoutes.SAVING_GOALS) {
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
            if (!isAddExpenseRoute && !isTopUpRoute && !isRecentTransactionsRoute && !isSavingGoalsRoute) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when {
                                isChatRoute -> stringResource(R.string.chat_title)
                                isProfileRoute -> stringResource(R.string.profile_title)
                                isMemoriesRoute -> stringResource(R.string.memories_title)
                                else -> stringResource(currentItem.titleRes)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = colorScheme.onSurface,
                        )
                    },
                    navigationIcon = {
                        if (isChatRoute) {
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
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
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
                        if (!isChatRoute) {
                            IconButton(onClick = { /* TODO: notifications */ }) {
                                Icon(
                                    imageVector = LucideBell,
                                    contentDescription = stringResource(R.string.action_notifications),
                                    tint = colorScheme.primary,
                                )
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
            }
        },
        bottomBar = {
            if (!isChatRoute && !isProfileRoute && !isAddExpenseRoute && !isTopUpRoute && !isRecentTransactionsRoute && !isSavingGoalsRoute) {
                AppNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scaffoldBackgroundBrush),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(280.dp)
                    .offset(x = 90.dp, y = (-110).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colorScheme.primary.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(300.dp)
                    .offset(x = (-110).dp, y = 130.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colorScheme.tertiary.copy(alpha = 0.14f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )

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
                            onSeeAllGoals = {
                                navController.navigate(HomeRoutes.SAVING_GOALS)
                            }
                        )
                    }
                    composable(HomeRoutes.WALLET) {
                        WalletScreen(
                            onNavigateToAddExpense = {
                                navController.navigate(HomeRoutes.ADD_EXPENSE)
                            },
                            onNavigateToTopUp = {
                                navController.navigate(HomeRoutes.TOP_UP)
                            },
                            onSeeAllTransactions = {
                                navController.navigate(HomeRoutes.RECENT_TRANSACTIONS)
                            }
                        )
                    }
                    composable(HomeRoutes.ADD_EXPENSE) {
                        AddExpenseScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(HomeRoutes.TOP_UP) {
                        TopUpScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(HomeRoutes.RECENT_TRANSACTIONS) {
                        RecentTransactionsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(HomeRoutes.SAVING_GOALS) {
                        SavingGoalsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
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

                if (!isChatRoute && !isAddExpenseRoute && !isTopUpRoute && !isRecentTransactionsRoute && !isSavingGoalsRoute) {
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
}

@Composable
private fun DraggableChatButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val colorScheme = MaterialTheme.colorScheme
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
            shape = CircleShape,
            color = colorScheme.primaryContainer,
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f)),
            shadowElevation = 8.dp,
            tonalElevation = 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.chat_fab_label),
                    color = colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
