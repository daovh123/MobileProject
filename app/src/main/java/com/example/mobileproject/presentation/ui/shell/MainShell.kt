package com.example.mobileproject.presentation.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mobileproject.presentation.navigation.MainNavGraph
import com.example.mobileproject.presentation.navigation.Routes
import com.example.mobileproject.presentation.ui.shell.components.BottomNavBar

@Composable
fun MainShell(
    navController: NavHostController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        Routes.Dashboard,
        Routes.Health,
        Routes.Activity,
        Routes.Mentor,
        Routes.Treasury,
        Routes.Quests,
        Routes.Profile,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
        },
    ) { padding: PaddingValues ->
        // Apply Scaffold padding so content doesn't get covered by the bottom bar
        Box(Modifier.fillMaxSize().padding(padding)) {
            MainNavGraph(navController = navController)
        }
    }
}
