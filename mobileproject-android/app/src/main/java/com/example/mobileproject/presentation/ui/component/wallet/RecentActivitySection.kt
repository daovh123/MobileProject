package com.example.mobileproject.presentation.ui.component.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.entity.Transaction

@Composable
fun RecentActivitySection(
    transactions: List<Transaction>,
    goals: List<SavingGoal> = emptyList(),
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
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
                text = stringResource(R.string.wallet_recent_activity_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            TextButton(onClick = onSeeAllClick) {
                Text(text = stringResource(R.string.common_see_all), color = colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            Text(
                text = stringResource(R.string.wallet_recent_activity_empty),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant
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
