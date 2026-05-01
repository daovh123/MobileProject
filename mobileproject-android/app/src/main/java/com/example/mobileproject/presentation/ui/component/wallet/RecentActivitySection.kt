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
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.entity.Transaction

@Composable
fun RecentActivitySection(
    transactions: List<Transaction>,
    goals: List<SavingGoal> = emptyList(),
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )
            TextButton(onClick = onSeeAllClick) {
                Text(text = "See All", color = Color(0xFFFF8A80))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            Text(
                text = "No recent activities",
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Chỉ hiển thị 5 giao dịch gần nhất ở màn hình chính
                transactions.take(5).forEach { transaction ->
                    TransactionItem(transaction = transaction, goals = goals)
                }
            }
        }
    }
}
