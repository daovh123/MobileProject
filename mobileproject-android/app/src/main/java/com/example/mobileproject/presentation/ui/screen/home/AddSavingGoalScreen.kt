package com.example.mobileproject.presentation.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.domain.model.ExpenseCategory
import com.example.mobileproject.presentation.ui.screen.add_expense.components.CategoryBottomSheet
import com.example.mobileproject.presentation.viewmodel.GoalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingGoalScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory>(ExpenseCategory.Travel) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    val datePickerState = rememberDatePickerState()
    
    val pinkBackground = Color(0xFFFFF0F0)
    val primaryPink = Color(0xFFFF8A80)
    val darkText = Color(0xFF4A3434)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = primaryPink) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCategorySheet) {
        CategoryBottomSheet(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            onDismiss = { showCategorySheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Saving Goal", color = primaryPink, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryPink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pinkBackground)
            )
        },
        containerColor = pinkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Goal Name
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Goal Name", fontWeight = FontWeight.Bold, color = darkText)
                TextField(
                    value = name,
                    onValueChange = { if (it.length <= 50) name = it },
                    placeholder = { Text("e.g., Buy a car", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    supportingText = {
                        Text(
                            text = "${name.length}/50",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }

            // Target Amount
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Target Amount", fontWeight = FontWeight.Bold, color = darkText)
                TextField(
                    value = targetAmount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) targetAmount = it },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { 
                        Text("$ ", color = primaryPink, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold) 
                    },
                    placeholder = {
                        Text("1000", color = primaryPink.copy(alpha = 0.3f), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = primaryPink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Category
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Category", fontWeight = FontWeight.Bold, color = darkText)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    val quickCategories = listOf(ExpenseCategory.Travel, ExpenseCategory.Education, ExpenseCategory.Household)
                    quickCategories.forEach { cat ->
                        CategoryCircle(
                            category = cat,
                            isSelected = selectedCategory.id == cat.id,
                            onClick = { selectedCategory = cat }
                        )
                    }
                    
                    // More button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.2f), CircleShape)
                                .clickable { showCategorySheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "More", tint = Color.LightGray, modifier = Modifier.size(32.dp))
                        }
                        Text("MORE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Target Date
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Target Date (Required)", fontWeight = FontWeight.Bold, color = darkText)
                Surface(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().border(
                        width = 1.dp, 
                        color = if (selectedDateMillis == null) primaryPink.copy(alpha = 0.5f) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = primaryPink)
                        val dateText = selectedDateMillis?.let {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                        } ?: "Select a date"
                        Text(
                            text = dateText,
                            color = if (selectedDateMillis == null) Color.LightGray else Color.Black,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isFormValid = name.isNotBlank() && targetAmount.isNotBlank() && selectedDateMillis != null

            Button(
                onClick = {
                    if (isFormValid) {
                        val deadlineStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateMillis!!))
                        viewModel.createSavingGoal(
                            name = name,
                            category = selectedCategory.id,
                            targetAmount = targetAmount.toLongOrNull() ?: 0L,
                            deadline = deadlineStr
                        )
                        onNavigateBack()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryPink,
                    disabledContainerColor = primaryPink.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Create Saving Goal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CategoryCircle(
    category: ExpenseCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, if (isSelected) Color(0xFFFF8A80) else Color.Transparent, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFFFF8A80) else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = category.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFFFF8A80) else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
