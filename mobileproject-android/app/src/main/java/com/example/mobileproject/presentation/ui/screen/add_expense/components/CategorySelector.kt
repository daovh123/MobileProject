package com.example.mobileproject.presentation.ui.screen.add_expense.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.domain.model.ExpenseCategory

@Composable
fun CategorySelector(
    selectedCategory: ExpenseCategory,
    onCategorySelected: (ExpenseCategory) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickCategories = listOf(
        ExpenseCategory.FoodDrink,
        ExpenseCategory.Dating,
        ExpenseCategory.Others
    )
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            quickCategories.forEach { category ->
                CategoryItem(
                    category = category,
                    isSelected = selectedCategory.id == category.id,
                    onClick = { onCategorySelected(category) }
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(1.dp, colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                        .clickable { onMoreClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "More",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "MORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: ExpenseCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isSelected) colorScheme.primary else colorScheme.surfaceContainerLowest)
                .border(1.dp, if (isSelected) Color.Transparent else colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.displayName,
                tint = if (isSelected) colorScheme.onPrimary else colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = category.id.split("_").first(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
