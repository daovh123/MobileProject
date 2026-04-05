package com.example.mobileproject.presentation.ui.screen.app.compose

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.presentation.ui.screen.explore.ExploreRoute
import com.example.mobileproject.presentation.ui.screen.home.HomeScreen
import com.example.mobileproject.presentation.ui.screen.memories.MemoriesScreen
import com.example.mobileproject.presentation.ui.screen.settings.SettingsScreen
import com.example.mobileproject.presentation.ui.screen.wallet.WalletScreen

object HomeRoutes {
    const val HOME: String = "home"
    const val WALLET: String = "wallet"
    const val EXPLORE: String = "explore"
    const val MEMORIES: String = "memories"
    const val SETTINGS: String = "settings"
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

    val context = LocalContext.current
    val activity = context as? Activity

    BackHandler {
        if (currentRoute != HomeRoutes.HOME) {
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(currentItem.titleRes),
                        color = colorResource(R.color.md3_primary),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate(HomeRoutes.SETTINGS) {
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
                },
                actions = {
                    IconButton(onClick = { /* TODO: notifications */ }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_notifications_24),
                            contentDescription = stringResource(R.string.action_notifications),
                            tint = colorResource(R.color.md3_primary),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.md3_surface),
                ),
            )
        },
        bottomBar = {
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
        },
        containerColor = colorResource(R.color.md3_surface_variant),
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoutes.HOME,
            modifier = Modifier.padding(innerPadding),
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
        }
    }
}
