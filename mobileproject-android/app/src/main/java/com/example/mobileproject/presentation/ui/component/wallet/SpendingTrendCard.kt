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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobileproject.domain.entity.SpendingTrend

@Composable
fun SpendingTrendCard(
    trends: List<SpendingTrend>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Income & Expense Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (trends.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No trend data", color = Color.Gray)
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
                        drawPath(incomePath, color = Color(0xFF4CAF50), style = Stroke(width = 4f))

                        // Vẽ đường Expense (Đỏ)
                        val expensePath = Path()
                        trends.forEachIndexed { i, trend ->
                            val x = i * spaceX
                            val y = size.height - (trend.totalExpense.toFloat() / maxVal * size.height)
                            if (i == 0) expensePath.moveTo(x, y) else expensePath.lineTo(x, y)
                        }
                        drawPath(expensePath, color = Color(0xFFFF8A80), style = Stroke(width = 4f))
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Jan", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = "Dec", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}
