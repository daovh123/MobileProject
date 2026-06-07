package com.example.mobileproject.presentation.ui.screen.add_expense.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.R
import com.example.mobileproject.domain.model.ExpenseCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBottomSheet(
    selectedCategory: ExpenseCategory,
    onCategorySelected: (ExpenseCategory) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .heightIn(max = 600.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.category_sheet_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close), tint = colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text(stringResource(R.string.category_search_placeholder), color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                    focusedContainerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = colorScheme.primary
                ),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorScheme.onSurfaceVariant) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val allCategories = ExpenseCategory.getAll()
                val groups = ExpenseCategory.CategoryType.values()
                
                groups.forEach { type ->
                    val categoriesInGroup = allCategories.filter { it.type == type }
                    
                    if (categoriesInGroup.isNotEmpty()) {
                        val groupLabelRes = when (type) {
                            ExpenseCategory.CategoryType.ESSENTIALS -> R.string.category_group_essentials
                            ExpenseCategory.CategoryType.LIFESTYLE -> R.string.category_group_lifestyle
                            ExpenseCategory.CategoryType.FINANCIALS -> R.string.category_group_financials
                        }
                        item(key = "header_${type.name}") {
                            Text(
                                text = stringResource(groupLabelRes),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        
                        items(categoriesInGroup, key = { it.id }) { category ->
                            CategoryRowItem(
                                category = category,
                                isSelected = selectedCategory.id == category.id,
                                onSelect = {
                                    onCategorySelected(category)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRowItem(
    category: ExpenseCategory,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) colorScheme.primaryContainer else colorScheme.surfaceContainerLowest,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colorScheme.primary else colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = if (isSelected) colorScheme.onPrimary else colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = category.displayName.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colorScheme.primary
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, colorScheme.outlineVariant.copy(alpha = 0.3f), CircleShape)
                )
            }
        }
    }
}
