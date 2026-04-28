package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.component.wallet.BalanceSection
import com.example.mobileproject.presentation.ui.component.wallet.MonthlySpendingCard
import com.example.mobileproject.presentation.ui.component.wallet.RecentActivitySection
import com.example.mobileproject.presentation.ui.screen.home.components.ContributeGoalBottomSheet
import com.example.mobileproject.presentation.viewmodel.SavingGoalViewModel
import com.example.mobileproject.presentation.viewmodel.WalletViewModel
import java.text.DateFormatSymbols

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToTopUp: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel(),
    savingGoalViewModel: SavingGoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val savingGoalState by savingGoalViewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    
    var isMonthPickerVisible by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var isContributeSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        savingGoalViewModel.loadGoals()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFFFF0F0),
            floatingActionButton = { }
        ) { paddingValues ->
            if (uiState.isLoading && uiState.wallet == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF8A80))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        BalanceSection(
                            balance = uiState.wallet?.balance ?: 0L
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        MonthlySpendingCard(
                            spendingList = uiState.categoryBreakdown,
                            selectedMonth = selectedMonth,
                            onMonthClick = { isMonthPickerVisible = true }
                        )
                    }

                    item {
                        RecentActivitySection(
                            transactions = uiState.allTransactions,
                            onSeeAllClick = onSeeAllTransactions
                        )
                    }
                }
            }
        }

        if (isMonthPickerVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isMonthPickerVisible = false }
            )
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(280.dp)
                    .heightIn(max = 450.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Select Month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFFFF0F0))
                    val months = DateFormatSymbols().months
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        itemsIndexed(months.filter { it.isNotEmpty() }) { index, month ->
                            val monthNumber = index + 1
                            val isSelected = monthNumber == selectedMonth
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectMonth(monthNumber)
                                        isMonthPickerVisible = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                                    .background(if (isSelected) Color(0xFFFFF0F0) else Color.Transparent, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(text = month, color = if (isSelected) Color(0xFFFF8A80) else Color.DarkGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }

        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isFabExpanded = false }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.clickable { 
                            isFabExpanded = false
                            isContributeSheetVisible = true 
                        }
                    ) {
                        Text(
                            text = "Contribute to Goal",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FloatingActionButton(
                        onClick = { 
                            isFabExpanded = false
                            isContributeSheetVisible = true 
                        },
                        containerColor = Color.White,
                        contentColor = Color(0xFFFF8A80),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Savings, contentDescription = null)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.clickable { 
                            isFabExpanded = false
                            onNavigateToAddExpense() 
                        }
                    ) {
                        Text(
                            text = "Add Expense",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FloatingActionButton(
                        onClick = { 
                            isFabExpanded = false
                            onNavigateToAddExpense() 
                        },
                        containerColor = Color.White,
                        contentColor = Color(0xFFFF8A80),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_wallet_24), contentDescription = null)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.clickable { 
                            isFabExpanded = false
                            onNavigateToTopUp() 
                        }
                    ) {
                        Text(
                            text = "Top Up",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FloatingActionButton(
                        onClick = { 
                            isFabExpanded = false
                            onNavigateToTopUp() 
                        },
                        containerColor = Color.White,
                        contentColor = Color(0xFFFF8A80),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null)
                    }
                }
            }
        }

        val rotation by animateFloatAsState(if (isFabExpanded) 45f else 0f)
        FloatingActionButton(
            onClick = { isFabExpanded = !isFabExpanded },
            containerColor = if (isFabExpanded) Color(0xFFFF8A80).copy(alpha = 0.8f) else Color(0xFFFF8A80),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(rotation)
            )
        }

        if (isContributeSheetVisible) {
            ContributeGoalBottomSheet(
                selectedGoal = null,
                availableGoals = savingGoalState.goals,
                onDismiss = { isContributeSheetVisible = false },
                onConfirm = { goalId, amount, note, isDirect ->
                    savingGoalViewModel.contribute(
                        goalId = goalId,
                        amount = amount,
                        note = note,
                        contributorId = if (isDirect) "me" else null
                    )
                    isContributeSheetVisible = false
                }
            )
        }
    }
}
