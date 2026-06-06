/**
 * Component hiển thị danh sách mục tiêu tiết kiệm.
 *
 * Cung cấp [GoalSection] composable hiển thị tối đa 2 mục tiêu
 * với nút "Xem tất cả" để điều hướng đến danh sách đầy đủ.
 */
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
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.presentation.ui.screen.home.components.GoalCard

/**
 * Hiển thị danh sách mục tiêu tiết kiệm trên màn hình ví.
 *
 * Component này render tối đa 2 mục tiêu tiết kiệm, sắp xếp theo:
 * 1. Mục tiêu chưa đạt được hiển thị trước (đã đạt ở dưới cùng).
 * 2. Trong cùng trạng thái, mục tiêu có hạn gần nhất hiển thị trước.
 *
 * Nếu danh sách trống, hiển thị thông báo "Chưa có mục tiêu tiết kiệm".
 * Có nút "Xem tất cả" để điều hướng đến danh sách đầy đủ.
 *
 * @param goals Danh sách [SavingGoal] cần hiển thị.
 * @param onSeeAllClick Callback khi người dùng nhấn "Xem tất cả".
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
fun GoalSection(
    goals: List<SavingGoal>,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
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
                text = stringResource(R.string.wallet_saving_goals_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            TextButton(onClick = onSeeAllClick) {
                Text(text = stringResource(R.string.common_see_all), color = colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (sortedGoals.isEmpty()) {
            Text(
                text = stringResource(R.string.wallet_no_saving_goals),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant
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
