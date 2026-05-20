package com.example.mobileproject.presentation.ui.screen.app.compose

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.example.mobileproject.presentation.ui.navigation.AppNavigationBar
import com.example.mobileproject.presentation.ui.navigation.NavigationConfig
import com.example.mobileproject.presentation.ui.screen.add_expense.AddExpenseScreen
import com.example.mobileproject.presentation.ui.screen.chat.ChatScreen
import com.example.mobileproject.presentation.ui.screen.explore.ExploreRoute
import com.example.mobileproject.presentation.ui.screen.home.AddFutureGoalScreen
import com.example.mobileproject.presentation.ui.screen.home.AddSavingGoalScreen
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.screen.home.HomeScreen
import com.example.mobileproject.presentation.ui.screen.memories.CaptureMomentScreen
import com.example.mobileproject.presentation.ui.screen.memories.LocketMemoriesScreen
import com.example.mobileproject.presentation.ui.screen.profile.ProfileScreen
import com.example.mobileproject.presentation.ui.screen.settings.SettingsScreen
import com.example.mobileproject.presentation.ui.screen.wallet.RecentTransactionsScreen
import com.example.mobileproject.presentation.ui.screen.wallet.SavingGoalsScreen
import com.example.mobileproject.presentation.ui.screen.wallet.FutureGoalsScreen
import com.example.mobileproject.presentation.ui.screen.wallet.TopUpScreen
import com.example.mobileproject.presentation.ui.screen.wallet.WalletScreen
import com.example.mobileproject.presentation.ui.icons.LucideBell
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideUser
import com.example.mobileproject.presentation.ui.screen.profile.ProfileEditScreen
import com.example.mobileproject.presentation.ui.components.core.DraggableChatFab
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.presentation.viewmodel.NotificationViewModel

object HomeRoutes {
    const val HOME: String = "home"
    const val WALLET: String = "wallet"
    const val EXPLORE: String = "explore"
    const val MEMORIES: String = "memories"
    const val MEMORIES_CAPTURE: String = "memories_capture"
    const val SETTINGS: String = "settings"
    const val PROFILE: String = "profile"
    const val PROFILE_EDIT: String = "profile_edit"
    const val CHAT: String = "chat"
    const val ADD_EXPENSE: String = "add_expense"
    const val TOP_UP: String = "top_up"
    const val RECENT_TRANSACTIONS: String = "recent_transactions"
    const val SAVING_GOALS: String = "saving_goals"
    const val ADD_SAVING_GOAL: String = "add_saving_goal"
    const val ADD_FUTURE_GOAL: String = "add_future_goal"
    const val FUTURE_GOALS: String = "future_goals"
    const val NOTIFICATIONS: String = "notifications"
}

data class AppNotification(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestampLabel: String,
    val isUnread: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(
    accessToken: String,
    apiService: ApiService,
    onNavControllerReady: (NavHostController) -> Unit,
) {
    val navController = rememberNavController()
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: HomeRoutes.HOME
    val currentItem = NavigationConfig.getItemByRoute(currentRoute)
        ?: NavigationConfig.navigationItems.first()
    val isProfileEditRoute = currentRoute == HomeRoutes.PROFILE_EDIT
    val isChatRoute = currentRoute == HomeRoutes.CHAT
    val isAddExpenseRoute = currentRoute == HomeRoutes.ADD_EXPENSE
    val isTopUpRoute = currentRoute == HomeRoutes.TOP_UP
    val isRecentTransactionsRoute = currentRoute == HomeRoutes.RECENT_TRANSACTIONS
    val isSavingGoalsRoute = currentRoute == HomeRoutes.SAVING_GOALS
    val isAddSavingGoalRoute = currentRoute == HomeRoutes.ADD_SAVING_GOAL
    val isAddFutureGoalRoute = currentRoute == HomeRoutes.ADD_FUTURE_GOAL
    val isFutureGoalsRoute = currentRoute == HomeRoutes.FUTURE_GOALS
    val isMemoriesCaptureRoute = currentRoute == HomeRoutes.MEMORIES_CAPTURE
    val isNotificationsRoute = currentRoute == HomeRoutes.NOTIFICATIONS

    val hideTopAndBottomBar = isChatRoute || isAddExpenseRoute || isTopUpRoute ||
        isRecentTransactionsRoute || isSavingGoalsRoute ||
        isAddSavingGoalRoute || isAddFutureGoalRoute || isFutureGoalsRoute ||
        isProfileEditRoute || isMemoriesCaptureRoute || isNotificationsRoute

    val snackbarHostState = remember { SnackbarHostState() }
    val notifState by notificationViewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val bgBrush = remember(colorScheme) {
        Brush.verticalGradient(
            listOf(
                colorScheme.surface,
                colorScheme.surfaceContainerLow,
                colorScheme.surfaceContainer.copy(alpha = 0.85f),
            ),
        )
    }

    val openLabel = stringResource(R.string.chat_in_app_open_action)
    val msgFormat = stringResource(R.string.chat_in_app_message_format)
    LaunchedEffect(navController, openLabel, msgFormat) {
        ChatInAppNotificationBus.events.collect { event ->
            val msg = msgFormat.format(
                event.senderUsername.ifBlank { event.conversationTitle },
                event.messageText.trim().replace(Regex("\\s+"), " "),
            )
            if (snackbarHostState.showSnackbar(msg, openLabel, withDismissAction = true) == SnackbarResult.ActionPerformed) {
                navController.navigate(HomeRoutes.CHAT) { launchSingleTop = true }
            }
        }
    }

    DisposableEffect(isChatRoute) {
        ChatNotificationGate.setChatRouteActive(isChatRoute)
        onDispose { ChatNotificationGate.setChatRouteActive(false) }
    }

    val context = LocalContext.current
    BackHandler {
        if (hideTopAndBottomBar) {
            navController.popBackStack()
        } else if (currentRoute != HomeRoutes.HOME) {
            navController.navigate(HomeRoutes.HOME) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!hideTopAndBottomBar) {
                HomeTopBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    isChatRoute = isChatRoute,
                    isProfileRoute = currentRoute == HomeRoutes.PROFILE,
                    isProfileEditRoute = isProfileEditRoute,
                    isMemoriesRoute = currentRoute == HomeRoutes.MEMORIES,
                    notifState = notifState,
                    onMarkAllRead = { notificationViewModel.markAllRead() },
                    onLoadNextPage = { notificationViewModel.loadNextPage() },
                    isHomeRoute = currentRoute == HomeRoutes.HOME,
                    isWalletRoute = currentRoute == HomeRoutes.WALLET,
                    isExploreRoute = currentRoute == HomeRoutes.EXPLORE,
                )
            }
        },
        bottomBar = {
            if (!hideTopAndBottomBar) {
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
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgBrush),
            )

            Box(
                modifier = Modifier.align(Alignment.TopEnd).size(280.dp).offset(x = 90.dp, y = (-110).dp)
                    .background(
                        brush = Brush.radialGradient(colors = listOf(colorScheme.primary.copy(alpha = 0.12f), Color.Transparent)),
                        shape = CircleShape,
                    ),
            )

            Box(
                modifier = Modifier.align(Alignment.BottomStart).size(300.dp).offset(x = (-110).dp, y = 130.dp)
                    .background(
                        brush = Brush.radialGradient(colors = listOf(colorScheme.tertiary.copy(alpha = 0.08f), Color.Transparent)),
                        shape = CircleShape,
                    ),
            )

            NavHost(
                navController = navController,
                startDestination = HomeRoutes.HOME,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(HomeRoutes.HOME) {
                    HomeScreen(
                        accessToken = accessToken,
                        apiService = apiService,
                        onSeeAllGoals = { navController.navigate(HomeRoutes.SAVING_GOALS) },
                        onSeeAllFutureGoals = { navController.navigate(HomeRoutes.FUTURE_GOALS) },
                        onNavigateToAddSavingGoal = { navController.navigate(HomeRoutes.ADD_SAVING_GOAL) },
                        onNavigateToAddFutureGoal = { navController.navigate(HomeRoutes.ADD_FUTURE_GOAL) },
                        onNavigateToChat = {
                            navController.navigate(HomeRoutes.CHAT) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                
                composable(HomeRoutes.PROFILE_EDIT) {
                    ProfileEditScreen(
                        accessToken = accessToken,
                        onNavigateBack = { navController.popBackStack() },
                        onLogout = {
                            (context as? HomeActivity)?.logoutAndOpenLogin()
                        }
                    )
                }
                composable(HomeRoutes.WALLET) {
                    WalletScreen(
                        onNavigateToAddExpense = { navController.navigate(HomeRoutes.ADD_EXPENSE) },
                        onNavigateToTopUp = { navController.navigate(HomeRoutes.TOP_UP) },
                        onSeeAllTransactions = { navController.navigate(HomeRoutes.RECENT_TRANSACTIONS) }
                    )
                }
                composable(HomeRoutes.ADD_EXPENSE) {
                    AddExpenseScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable(HomeRoutes.TOP_UP) {
                    TopUpScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable(HomeRoutes.RECENT_TRANSACTIONS) {
                    RecentTransactionsScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable(HomeRoutes.SAVING_GOALS) {
                    SavingGoalsScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable(HomeRoutes.FUTURE_GOALS) {
                    FutureGoalsScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable(HomeRoutes.ADD_SAVING_GOAL) {
                    AddSavingGoalScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable(HomeRoutes.ADD_FUTURE_GOAL) {
                    AddFutureGoalScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable(HomeRoutes.EXPLORE) {
                    ExploreRoute(accessToken = accessToken)
                }
                composable(HomeRoutes.MEMORIES) {
                    LocketMemoriesScreen(
                        accessToken = accessToken,
                        onOpenCapture = { navController.navigate(HomeRoutes.MEMORIES_CAPTURE) },
                    )
                }
                composable(HomeRoutes.MEMORIES_CAPTURE) {
                    CaptureMomentScreen(
                        accessToken = accessToken,
                        onClose = { navController.popBackStack() }
                    )
                }
                composable(HomeRoutes.SETTINGS) {
                    SettingsScreen(accessToken = accessToken)
                }
                composable(HomeRoutes.PROFILE) {
                    ProfileScreen(
                        accessToken = accessToken,
                        onNavigateBack = { navController.popBackStack() },
                        onLogout = { (context as? HomeActivity)?.logoutAndOpenLogin() },
                        onEditProfile = {
                            navController.navigate(HomeRoutes.PROFILE_EDIT)
                        },
                        onOpenSettings = {
                            navController.navigate(HomeRoutes.SETTINGS)
                        },
                    )
                }
                composable(HomeRoutes.CHAT) {
                    ChatScreen(accessToken = accessToken)
                }
                composable(HomeRoutes.NOTIFICATIONS) {
                    com.example.mobileproject.presentation.ui.screen.notification.NotificationScreen(
                        navController = navController,
                        viewModel = notificationViewModel
                    )
                }
            }

            DisposableEffect(navController) {
                onNavControllerReady(navController)
                onDispose { }
            }

            // Chat FAB is now integrated into HomeScreen's speed-dial FAB
        }
    }
}
