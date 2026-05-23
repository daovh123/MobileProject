package com.example.mobileproject.presentation.ui.explore

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobileproject.presentation.ui.screen.explore.ExploreBudgetPlannerCard
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
import com.example.mobileproject.presentation.viewmodel.ExploreBudgetSource
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExploreBudgetPlannerCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun plannerCardAcceptsManualBudgetAndTriggersAction() {
        val clicked = mutableStateOf(false)

        composeTestRule.setContent {
            MobileProjectTheme(dynamicColor = false) {
                val manualBudget = remember { mutableStateOf("") }
                val peopleCount = remember { mutableStateOf("2") }
                val desiredStops = remember { mutableStateOf("2") }

                ExploreBudgetPlannerCard(
                    budgetSource = ExploreBudgetSource.MANUAL,
                    walletBalance = null,
                    walletLoading = false,
                    manualBudgetInput = manualBudget.value,
                    peopleCountInput = peopleCount.value,
                    desiredStopsInput = desiredStops.value,
                    isPlanLoading = false,
                    planErrorMessage = null,
                    lowBalanceMessage = null,
                    onBudgetSourceChanged = {},
                    onManualBudgetChanged = { manualBudget.value = it },
                    onPeopleCountChanged = { peopleCount.value = it },
                    onDesiredStopsChanged = { desiredStops.value = it },
                    onBuildPlan = { clicked.value = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Gợi ý theo budget").assertIsDisplayed()
        composeTestRule.onNodeWithText("Budget khả dụng").performTextInput("120000")
        composeTestRule.onNodeWithText("Số người").performTextInput("3")
        composeTestRule.onNodeWithText("Gợi ý lịch trình").performClick()
        composeTestRule.runOnIdle {
            assert(clicked.value)
        }
    }
}
