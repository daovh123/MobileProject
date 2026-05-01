package com.example.mobileproject.presentation.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.domain.model.ExpenseCategory
import com.example.mobileproject.presentation.ui.component.AppDatePicker
import com.example.mobileproject.presentation.ui.component.DateUtils
import com.example.mobileproject.presentation.ui.screen.add_expense.components.CategoryBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Long, category: String, deadline: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory>(ExpenseCategory.Others) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var deadline by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFFF0F0),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Goal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8A80)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Goal Name
            Text("Goal Name", fontWeight = FontWeight.Bold, color = Color(0xFF4A3434))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("e.g., Anniversary Trip", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFFF8A80)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Target Amount
            Text("Target Amount", fontWeight = FontWeight.Bold, color = Color(0xFF4A3434))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { if (it.all { char -> char.isDigit() }) targetAmount = it },
                prefix = { Text("$ ", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFFF8A80)
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category Selection
            Text("Category", fontWeight = FontWeight.Bold, color = Color(0xFF4A3434))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val displayCategories = listOf(ExpenseCategory.Travel, ExpenseCategory.Education, ExpenseCategory.Household)
                displayCategories.forEach { category ->
                    CategoryIconItem(
                        category = category,
                        isSelected = selectedCategory.id == category.id,
                        onClick = { selectedCategory = category }
                    )
                }
                
                // More button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { showCategorySheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                    }
                    Text("MORE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Target Date
            Text("Target Date", fontWeight = FontWeight.Bold, color = Color(0xFF4A3434))
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFFF8A80))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = DateUtils.formatToDisplay(deadline),
                        color = if (deadline == null) Color.LightGray else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = { 
                    if (name.isNotBlank() && targetAmount.isNotBlank()) {
                        onConfirm(name, targetAmount.toLong(), selectedCategory.id, deadline)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A80)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Goal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showCategorySheet) {
            CategoryBottomSheet(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onDismiss = { showCategorySheet = false }
            )
        }

        if (showDatePicker) {
            AppDatePicker(
                onDateSelected = { dateString ->
                    deadline = dateString
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun CategoryIconItem(
    category: ExpenseCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFFFF8A80) else Color.White)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFFFF8A80)
            )
        }
        Text(
            text = category.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFFFF8A80) else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
