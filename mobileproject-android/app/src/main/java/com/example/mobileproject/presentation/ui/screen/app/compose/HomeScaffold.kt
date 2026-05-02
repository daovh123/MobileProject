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

object HomeRoutes {
    const val HOME: String = "home"
    const val WALLET: String = "wallet"
    const val EXPLORE: String = "explore"
    const val MEMORIES: String = "memories"
    const val SETTINGS: String = "settings"
    const val PROFILE: String = "profile"
    const val PROFILE_EDIT: String = "profile_edit"
    const val CHAT: String = "chat"
}

@Composable
fun HomeScaffold(
    accessToken: String,
    apiService: ApiService,
    onNavControllerReady: (NavHostController) -> Unit,
) {
    val navController = rememberNavController()
    DisposableEffect(navController) { onNavControllerReady(navController); onDispose { } }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: HomeRoutes.HOME
    val isMemoriesRoute = currentRoute == HomeRoutes.MEMORIES
    val isProfileEditRoute = currentRoute == HomeRoutes.PROFILE_EDIT
    val isProfileRoute = currentRoute == HomeRoutes.PROFILE || isProfileEditRoute
    val isChatRoute = currentRoute == HomeRoutes.CHAT

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
        when {
            currentRoute == HomeRoutes.CHAT -> navController.popBackStack()
            currentRoute != HomeRoutes.HOME -> navController.navigate(HomeRoutes.HOME) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true; restoreState = true
            }
            else -> (context as? Activity)?.finish()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            HomeTopBar(
                navController = navController,
                currentRoute = currentRoute,
                isChatRoute = isChatRoute,
                isProfileRoute = isProfileRoute,
                isProfileEditRoute = isProfileEditRoute,
                isMemoriesRoute = isMemoriesRoute,
            )
        },
        bottomBar = {
            if (!isChatRoute && !isProfileRoute) {
                AppNavigationBar(currentRoute = currentRoute, onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true; restoreState = true
                    }
                })
            }
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(bgBrush))
            Box(Modifier.align(Alignment.TopEnd).size(280.dp).offset(90.dp, (-110).dp)
                .background(Brush.radialGradient(listOf(colorScheme.primary.copy(alpha = 0.18f), Color.Transparent)), CircleShape))
            Box(Modifier.align(Alignment.BottomStart).size(300.dp).offset((-110).dp, 130.dp)
                .background(Brush.radialGradient(listOf(colorScheme.tertiary.copy(alpha = 0.14f), Color.Transparent)), CircleShape))
            Box(Modifier.padding(innerPadding).fillMaxSize()) {
                HomeNavHost(navController, accessToken, apiService, context, isChatRoute, isProfileEditRoute)
            }
        }
    }
}
