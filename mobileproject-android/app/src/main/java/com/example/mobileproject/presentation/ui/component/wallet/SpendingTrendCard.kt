/**
 * Component hiển thị xu hướng chi tiêu bằng biểu đồ đường.
 *
 * Cung cấp [SpendingTrendCard] composable với 2 đường biểu thị
 * thu nhập (xanh lá) và chi tiêu (đỏ) qua các tháng.
 */
package com.example.mobileproject.presentation.ui.component.wallet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.SpendingTrend

/**
 * Card hiển thị xu hướng chi tiêu qua các tháng bằng biểu đồ đường.
 *
 * Component này render:
 * - Tiêu đề "Xu hướng chi tiêu".
 * - Biểu đồ đường với 2 đường: thu nhập (xanh lá - tertiary) và chi tiêu (đỏ - primary).
 * - Trục X biểu thị thời gian, trục Y biểu thị số tiền.
 * - Nhãn tháng đầu và tháng cuối ở dưới cùng.
 *
 * Nếu danh sách rỗng, hiển thị thông báo "Chưa có dữ liệu xu hướng".
 *
 * @param trends Danh sách [SpendingTrend] chứa dữ liệu thu nhập/chi tiêu theo tháng.
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
fun SpendingTrendCard(
    trends: List<SpendingTrend>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.wallet_trend_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (trends.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.wallet_trend_empty), color = colorScheme.onSurfaceVariant)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxVal = (trends.maxOfOrNull { maxOf(it.totalIncome, it.totalExpense) } ?: 1L).coerceAtLeast(1L)
                        val spaceX = size.width / (trends.size - 1).coerceAtLeast(1)
                        
                        // Vẽ đường Income (Xanh)
                        val incomePath = Path()
                        trends.forEachIndexed { i, trend ->
                            val x = i * spaceX
                            val y = size.height - (trend.totalIncome.toFloat() / maxVal * size.height)
                            if (i == 0) incomePath.moveTo(x, y) else incomePath.lineTo(x, y)
                        }
                        drawPath(incomePath, color = colorScheme.tertiary, style = Stroke(width = 4f))

                        // Vẽ đường Expense (Đỏ)
                        val expensePath = Path()
                        trends.forEachIndexed { i, trend ->
                            val x = i * spaceX
                            val y = size.height - (trend.totalExpense.toFloat() / maxVal * size.height)
                            if (i == 0) expensePath.moveTo(x, y) else expensePath.lineTo(x, y)
                        }
                        drawPath(expensePath, color = colorScheme.primary, style = Stroke(width = 4f))
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.wallet_trend_month_start), style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    Text(text = stringResource(R.string.wallet_trend_month_end), style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
