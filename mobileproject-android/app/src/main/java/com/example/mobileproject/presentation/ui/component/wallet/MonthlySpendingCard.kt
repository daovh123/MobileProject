package com.example.mobileproject.presentation.ui.component.wallet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.domain.entity.CategoryBreakdown
import java.text.DateFormatSymbols

@Composable
fun MonthlySpendingCard(
    spendingList: List<CategoryBreakdown>,
    selectedMonth: Int,
    onMonthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthName = DateFormatSymbols().months[selectedMonth - 1]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Spending",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF0F0),
                    modifier = Modifier.clickable { onMonthClick() }
                ) {
                    Text(
                        text = monthName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFF8A80),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (spendingList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transaction in this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Chart
                    Box(
                        modifier = Modifier.size(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 35f
                            var startAngle = -90f
                            val colors = listOf(
                                Color(0xFFFF8A80), 
                                Color(0xFFFFAB91), 
                                Color(0xFFFFCCBC), 
                                Color(0xFFFBE9E7),
                                Color(0xFFE91E63)
                            )
                            
                            spendingList.forEachIndexed { index, item ->
                                val sweepAngle = (item.percentage * 360f).coerceAtLeast(1f)
                                drawArc(
                                    color = colors.getOrElse(index % colors.size) { Color.LightGray },
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                                startAngle += sweepAngle
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "TOTAL", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            val total = spendingList.sumOf { it.totalAmount }
                            Text(
                                text = formatKAmount(total), 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Legend
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val colors = listOf(
                            Color(0xFFFF8A80), Color(0xFFFFAB91), Color(0xFFFFCCBC), Color(0xFFFBE9E7), Color(0xFFE91E63)
                        )
                        spendingList.take(5).forEachIndexed { index, item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(colors.getOrElse(index % colors.size) { Color.LightGray }, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.category, 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatKAmount(amount: Long): String {
    return if (amount >= 1000) {
        "$${(amount.toDouble() / 1000.0).let { "%.1f".format(it) }}k"
    } else {
        "$${amount}"
    }
}
