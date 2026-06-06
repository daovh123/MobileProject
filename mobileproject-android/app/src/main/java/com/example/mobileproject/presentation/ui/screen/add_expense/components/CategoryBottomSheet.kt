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

/**
 * Bottom sheet chọn danh mục chi tiêu – hiển thị đầy đủ tất cả danh mục
 * được nhóm theo loại (Essentials/Lifestyle/Financials).
 *
 * Cấu trúc UI:
 * - Header: tiêu đề + nút đóng.
 * - SearchBar: ô tìm kiếm danh mục (placeholder, chưa filter).
 * - LazyColumn: danh mục nhóm theo [ExpenseCategory.CategoryType],
 *   mỗi nhóm có header label + các [CategoryRowItem].
 *
 * @param selectedCategory danh mục đang chọn.
 * @param onCategorySelected callback khi chọn danh mục.
 * @param onDismiss callback khi đóng bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBottomSheet(
    selectedCategory: ExpenseCategory,
    onCategorySelected: (ExpenseCategory) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .heightIn(max = 600.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.category_sheet_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color(0xFFFFF0F0), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close), tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text(stringResource(R.string.category_search_placeholder), color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFFFF0F0).copy(alpha = 0.5f),
                    focusedContainerColor = Color(0xFFFFF0F0).copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFFF8A80)
                ),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category Groups
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
                                color = Color.Gray,
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
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) Color(0xFFFFF0F0) else Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFF0F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFFFF8A80) else Color(0xFFFFF0F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color(0xFFFF8A80),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = category.displayName.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF2D2D2D) else Color.Gray,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFFFF8A80)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, Color.LightGray.copy(alpha = 0.3f), CircleShape)
                )
            }
        }
    }
}
