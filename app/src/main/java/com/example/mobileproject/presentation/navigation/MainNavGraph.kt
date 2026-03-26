package com.example.mobileproject.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobileproject.presentation.ui.screen.activity.ActivityScreen
import com.example.mobileproject.presentation.ui.screen.auth.LoginScreen
import com.example.mobileproject.presentation.ui.screen.auth.SignupScreen
import com.example.mobileproject.presentation.ui.screen.dashboard.DashboardScreen
import com.example.mobileproject.presentation.ui.screen.health.HealthScreen
import com.example.mobileproject.presentation.ui.screen.mentor.MentorScreen
import com.example.mobileproject.presentation.ui.screen.onboarding.OnboardingScreen
import com.example.mobileproject.presentation.ui.screen.profile.ProfileScreen
import com.example.mobileproject.presentation.ui.screen.quests.QuestsScreen
import com.example.mobileproject.presentation.ui.screen.treasury.TreasuryScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.Onboarding,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onLogin = { navController.navigate(Routes.Login) },
                onSignup = { navController.navigate(Routes.Signup) },
                onContinue = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Login) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Signup) {
            SignupScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.Dashboard) { DashboardScreen() }
        composable(Routes.Health) { HealthScreen() }
        composable(Routes.Activity) { ActivityScreen() }
        composable(Routes.Mentor) { MentorScreen() }
        composable(Routes.Treasury) { TreasuryScreen() }
        composable(Routes.Quests) { QuestsScreen() }
        composable(Routes.Profile) { ProfileScreen() }
    }
}
