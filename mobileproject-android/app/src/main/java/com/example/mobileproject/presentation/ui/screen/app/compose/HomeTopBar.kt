package com.example.mobileproject.presentation.ui.screen.app.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.icons.LucideBell
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideUser

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeTopBar(
    navController: NavHostController,
    currentRoute: String,
    isChatRoute: Boolean,
    isProfileRoute: Boolean,
    isProfileEditRoute: Boolean,
    isMemoriesRoute: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = when {
                    isChatRoute -> stringResource(R.string.chat_title)
                    isProfileEditRoute -> stringResource(R.string.profile_edit_title)
                    isProfileRoute -> stringResource(R.string.profile_title)
                    isMemoriesRoute -> stringResource(R.string.memories_title)
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
                IconButton(onClick = { navController.navigate(HomeRoutes.SETTINGS) }) {
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
