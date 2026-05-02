package com.example.mobileproject.presentation.ui.screen.app.compose

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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
import com.example.mobileproject.presentation.ui.components.core.DraggableChatFab

object HomeRoutes {
    const val HOME: String = "home"
    const val WALLET: String = "wallet"
    const val EXPLORE: String = "explore"
    const val MEMORIES: String = "memories"
    const val SETTINGS: String = "settings"
    const val PROFILE: String = "profile"
    const val PROFILE_EDIT: String = "profile_edit"
    const val CHAT: String = "chat"
    const val ADD_EXPENSE: String = "add_expense"
    const val TOP_UP: String = "top_up"
    const val RECENT_TRANSACTIONS: String = "recent_transactions"
    const val SAVING_GOALS: String = "saving_goals"
}

@Composable
fun HomeScaffold(
    accessToken: String,
    apiService: ApiService,
    onNavControllerReady: (NavHostController) -> Unit,
) {
    val navController = rememberNavController()


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: HomeRoutes.HOME
    val isMemoriesRoute = currentRoute == HomeRoutes.MEMORIES
    val isProfileEditRoute = currentRoute == HomeRoutes.PROFILE_EDIT
    val isProfileRoute = currentRoute == HomeRoutes.PROFILE || isProfileEditRoute
    val isChatRoute = currentRoute == HomeRoutes.CHAT
    val isAddExpenseRoute = currentRoute == HomeRoutes.ADD_EXPENSE
    val isTopUpRoute = currentRoute == HomeRoutes.TOP_UP
    val isRecentTransactionsRoute = currentRoute == HomeRoutes.RECENT_TRANSACTIONS
    val isSavingGoalsRoute = currentRoute == HomeRoutes.SAVING_GOALS
    
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme
    val bgBrush = remember(colorScheme) {
        Brush.verticalGradient(
            listOf(colorScheme.surface, colorScheme.surfaceVariant.copy(alpha = 0.95f), colorScheme.background),
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
        if (currentRoute == HomeRoutes.CHAT || currentRoute == HomeRoutes.ADD_EXPENSE || currentRoute == HomeRoutes.TOP_UP || currentRoute == HomeRoutes.RECENT_TRANSACTIONS || currentRoute == HomeRoutes.SAVING_GOALS || currentRoute == HomeRoutes.PROFILE_EDIT) {
            navController.popBackStack()
        } else if (currentRoute != HomeRoutes.HOME) {
            navController.navigate(HomeRoutes.HOME) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            else -> (context as? Activity)?.finish()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!isAddExpenseRoute && !isTopUpRoute && !isRecentTransactionsRoute && !isSavingGoalsRoute) {
                HomeTopBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    isChatRoute = isChatRoute,
                    isProfileRoute = isProfileRoute,
                    isProfileEditRoute = isProfileEditRoute,
                    isMemoriesRoute = isMemoriesRoute,
                )
            }
        },
        bottomBar = {
            if (!isChatRoute && !isProfileRoute && !isProfileEditRoute && !isAddExpenseRoute && !isTopUpRoute && !isRecentTransactionsRoute && !isSavingGoalsRoute) {
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
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(bgBrush))

            Box(
                modifier = Modifier.align(Alignment.TopEnd).size(280.dp).offset(x = 90.dp, y = (-110).dp)
                    .background(
                        brush = Brush.radialGradient(colors = listOf(colorScheme.primary.copy(alpha = 0.18f), Color.Transparent)),
                        shape = CircleShape,
                    ),
            )

            Box(
                modifier = Modifier.align(Alignment.BottomStart).size(300.dp).offset(x = (-110).dp, y = 130.dp)
                    .background(
                        brush = Brush.radialGradient(colors = listOf(colorScheme.tertiary.copy(alpha = 0.14f), Color.Transparent)),
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

                DisposableEffect(navController) {
                    onNavControllerReady(navController)
                    onDispose { }
                }

                if (!isChatRoute && !isProfileEditRoute && !isAddExpenseRoute && !isTopUpRoute && !isRecentTransactionsRoute && !isSavingGoalsRoute) {
                    DraggableChatFab(
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
