package com.example.mobileproject.presentation.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MobileProjectThemeSmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersBasicComposableInsideTheme() {
        composeTestRule.setContent {
            MobileProjectTheme(dynamicColor = false) {
                Text(text = "Theme smoke content")
            }
        }

        composeTestRule.onNodeWithText("Theme smoke content").assertIsDisplayed()
    }
}
