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
import androidx.compose.ui.text.TextStyle
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

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
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
                        shape = RoundedCornerShape(14.dp),
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

                Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Donut Chart
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidthNormal = 20f
                                val strokeWidthSelected = 30f
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
                                    val percentageText = "%.2f".format(item.percentage * 100).replace(".", ",")
                                    Text(
                                        text = "$percentageText%",
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = item.category.uppercase(),
                                        style = TextStyle(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurfaceVariant
                                        ),
                                        maxLines = 1
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.wallet_total_label),
                                        style = TextStyle(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    )
                                    val total = spendingList.sumOf { it.totalAmount }
                                    Text(
                                        text = formatSimpleAmount(total), 
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Legend với Scroll
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
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
                                        .padding(vertical = 2.dp, horizontal = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isSelected) 10.dp else 6.dp)
                                            .background(colors.getOrElse(index % colors.size) { Color.LightGray }, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = item.category, 
                                            style = TextStyle(
                                                fontSize = if (isSelected) 11.sp else 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant
                                            ),
                                            maxLines = 1
                                        )
                                        if (isSelected) {
                                            Text(
                                                text = formatSimpleAmount(item.totalAmount),
                                                style = TextStyle(
                                                    fontSize = 9.sp,
                                                    color = colorScheme.primary
                                                )
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

        // Image overlay: 14.png (R.drawable.sticker_14) at the top-right corner of the Card
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.sticker_14),
            contentDescription = null,
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = (-20).dp)
        )
    }
}
