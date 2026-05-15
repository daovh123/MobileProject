package com.example.mobileproject.presentation.ui.component.wallet

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.CategoryBreakdown
import java.text.DecimalFormat
import java.text.DateFormatSymbols

@Composable
fun MonthlySpendingCard(
    spendingList: List<CategoryBreakdown>,
    selectedMonth: Int,
    onMonthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val monthName = DateFormatSymbols().months[selectedMonth - 1]
    var selectedCategoryIndex by remember { mutableStateOf(-1) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.wallet_monthly_spending_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onMonthClick() }
                ) {
                    Text(
                        text = monthName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (spendingList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.wallet_monthly_spending_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Chart
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidthNormal = 35f
                            val strokeWidthSelected = 50f
                            var startAngle = -90f
                            val colors = listOf(
                                colorScheme.primary,
                                colorScheme.secondary,
                                colorScheme.tertiary,
                                colorScheme.primaryContainer,
                                colorScheme.secondaryContainer,
                                colorScheme.tertiaryContainer
                            )
                            
                            spendingList.forEachIndexed { index, item ->
                                val sweepAngle = (item.percentage * 360f).coerceAtLeast(1f)
                                val isSelected = index == selectedCategoryIndex
                                
                                drawArc(
                                    color = colors.getOrElse(index % colors.size) { Color.LightGray },
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = if (isSelected) strokeWidthSelected else strokeWidthNormal)
                                )
                                startAngle += sweepAngle
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (selectedCategoryIndex != -1 && selectedCategoryIndex < spendingList.size) {
                                val item = spendingList[selectedCategoryIndex]
                                // Hiển thị 2 chữ số thập phân sau dấu ","
                                val percentageText = "%.2f".format(item.percentage * 100).replace(".", ",")
                                Text(
                                    text = "$percentageText%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = colorScheme.primary
                                )
                                Text(
                                    text = item.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            } else {
                                Text(text = stringResource(R.string.wallet_total_label), style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                                val total = spendingList.sumOf { it.totalAmount }
                                Text(
                                    text = formatSimpleAmount(total), 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legend với Scroll
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(spendingList) { index, item ->
                            val isSelected = index == selectedCategoryIndex
                            val colors = listOf(
                                colorScheme.primary,
                                colorScheme.secondary,
                                colorScheme.tertiary,
                                colorScheme.primaryContainer,
                                colorScheme.secondaryContainer,
                                colorScheme.tertiaryContainer
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        selectedCategoryIndex = if (isSelected) -1 else index 
                                    }
                                    .background(
                                        if (isSelected) colorScheme.surfaceVariant else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 12.dp else 8.dp)
                                        .background(colors.getOrElse(index % colors.size) { Color.LightGray }, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.category, 
                                        style = if (isSelected) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall, 
                                        color = if (isSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = formatSimpleAmount(item.totalAmount),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
