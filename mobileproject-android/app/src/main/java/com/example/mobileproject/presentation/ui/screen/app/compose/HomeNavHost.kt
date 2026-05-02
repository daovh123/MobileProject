package com.example.mobileproject.presentation.ui.screen.app.compose

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.presentation.ui.components.core.DraggableChatFab
import com.example.mobileproject.presentation.ui.screen.chat.ChatScreen
import com.example.mobileproject.presentation.ui.screen.explore.ExploreRoute
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.screen.home.HomeScreen
import com.example.mobileproject.presentation.ui.screen.memories.MemoriesScreen
import com.example.mobileproject.presentation.ui.screen.profile.ProfileEditScreen
import com.example.mobileproject.presentation.ui.screen.profile.ProfileScreen
import com.example.mobileproject.presentation.ui.screen.settings.SettingsScreen
import com.example.mobileproject.presentation.ui.screen.wallet.WalletScreen

@Composable
fun HomeNavHost(
    navController: NavHostController,
    accessToken: String,
    apiService: ApiService,
    context: Context,
    isChatRoute: Boolean,
    isProfileEditRoute: Boolean,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoutes.HOME,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(HomeRoutes.HOME) {
            HomeScreen(accessToken = accessToken, apiService = apiService)
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
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        composable(HomeRoutes.SETTINGS) {
            SettingsScreen(
                accessToken = accessToken,
                onOpenProfileEdit = {
                    navController.navigate(HomeRoutes.PROFILE_EDIT) { launchSingleTop = true }
                },
            )
        }
        composable(HomeRoutes.PROFILE) {
            ProfileScreen(
                accessToken = accessToken,
                onNavigateBack = { navController.popBackStack() },
                onLogout = { (context as? HomeActivity)?.logoutAndOpenLogin() },
                onEditProfile = {
                    navController.navigate(HomeRoutes.PROFILE_EDIT) { launchSingleTop = true }
                },
                onOpenSettings = {
                    navController.navigate(HomeRoutes.SETTINGS) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        composable(HomeRoutes.PROFILE_EDIT) {
            ProfileEditScreen(
                accessToken = accessToken,
                onNavigateBack = { navController.popBackStack() },
                onLogout = { (context as? HomeActivity)?.logoutAndOpenLogin() },
            )
        }
        composable(HomeRoutes.CHAT) {
            ChatScreen(accessToken = accessToken)
        }
    }

    if (!isChatRoute && !isProfileEditRoute) {
        DraggableChatFab(
            onClick = {
                navController.navigate(HomeRoutes.CHAT) { launchSingleTop = true }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
