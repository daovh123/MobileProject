package com.example.mobileproject.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationSmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun navigatesFromHomeToDetails() {
        composeTestRule.setContent {
            MobileProjectTheme(dynamicColor = false) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home",
                ) {
                    composable("home") {
                        Column {
                            Text(text = "Home route")
                            Button(onClick = { navController.navigate("details") }) {
                                Text(text = "Open details")
                            }
                        }
                    }

                    composable("details") {
                        Text(text = "Details route")
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Home route").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open details").performClick()
        composeTestRule.onNodeWithText("Details route").assertIsDisplayed()
    }
}
