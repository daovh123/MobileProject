package com.example.mobileproject.presentation.ui.component.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.presentation.ui.screen.home.components.GoalCard

@Composable
fun GoalSection(
    goals: List<SavingGoal>,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedGoals = goals.sortedWith(
        compareBy<SavingGoal> { it.status == GoalStatus.ACHIEVED } // Achieved at bottom
            .thenBy { it.deadline ?: "9999-99-99" } // Near deadline first
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saving Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )
            TextButton(onClick = onSeeAllClick) {
                Text(text = "See All", color = Color(0xFFFF8A80))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (sortedGoals.isEmpty()) {
            Text(
                text = "No saving goals yet",
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Hiển thị tối đa 2 mục như yêu cầu
                sortedGoals.take(2).forEach { goal ->
                    GoalCard(goal = goal)
                }
            }
        }
    }
}
