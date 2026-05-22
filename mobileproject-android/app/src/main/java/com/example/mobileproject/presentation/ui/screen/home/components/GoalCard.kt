package com.example.mobileproject.presentation.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.mobileproject.domain.entity.FutureGoal
import com.example.mobileproject.domain.entity.Goal
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.utils.formatSimpleAmount

@Composable
fun GoalCard(
    goal: Goal,
    onTaskToggle: (String, String) -> Unit = { _, _ -> }
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Surface(
                    color = colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = goal.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            when (goal) {
                is SavingGoal -> SavingGoalContent(goal)
                is FutureGoal -> FutureGoalContent(goal, onTaskToggle)
            }
        }
    }
}

@Composable
private fun SavingGoalContent(goal: SavingGoal) {
    val colorScheme = MaterialTheme.colorScheme
    val progress = if (goal.targetAmount > 0) goal.currentAmount.toFloat() / goal.targetAmount else 0f
    val percentage = (progress * 100).toInt()
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0)

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "${formatSimpleAmount(goal.currentAmount)} / ${formatSimpleAmount(goal.targetAmount)}",
        style = MaterialTheme.typography.bodyMedium,
        color = colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp),
        color = colorScheme.primary,
        trackColor = colorScheme.surfaceContainer,
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Text(
        text = "You're $percentage% done! Just ${formatSimpleAmount(remaining)} more to go.",
        style = MaterialTheme.typography.bodySmall,
        color = colorScheme.onSurfaceVariant
    )
}

@Composable
private fun FutureGoalContent(goal: FutureGoal, onTaskToggle: (String, String) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val percentage = goal.progress.toInt()
    val completedTasks = goal.tasks.count { it.isCompleted }
    val totalTasks = goal.tasks.size
    
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "$completedTasks / $totalTasks tasks completed",
        style = MaterialTheme.typography.bodyMedium,
        color = colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    LinearProgressIndicator(
        progress = { (goal.progress / 100.0).toFloat().coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp),
        color = colorScheme.secondary,
        trackColor = colorScheme.secondaryContainer,
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    Spacer(modifier = Modifier.height(12.dp))
    
    Text(
        text = "You're $percentage% done!",
        style = MaterialTheme.typography.bodySmall,
        color = colorScheme.onSurfaceVariant
    )

    if (goal.tasks.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            goal.tasks.take(3).forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTaskToggle(goal.id, task.taskId) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (task.isCompleted) colorScheme.secondary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = task.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.isCompleted) colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
            if (goal.tasks.size > 3) {
                Text(
                    text = "+ ${goal.tasks.size - 3} more tasks",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 36.dp)
                )
            }
        }
    }
}
