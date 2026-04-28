package com.example.mobileproject.presentation.ui.screen.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.utils.formatSimpleAmount

@Composable
fun GoalCard(goal: SavingGoal) {
    val progress = if (goal.targetAmount > 0) goal.currentAmount.toFloat() / goal.targetAmount else 0f
    val percentage = (progress * 100).toInt()
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = goal.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A3434)
            )
            Text(
                text = "${formatSimpleAmount(goal.currentAmount)} / ${formatSimpleAmount(goal.targetAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = Color(0xFFFF8A80),
                trackColor = Color(0xFFFFF0F0),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "You're $percentage% done! Just ${formatSimpleAmount(remaining)} more to go.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
