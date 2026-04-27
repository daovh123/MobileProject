package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.presentation.ui.component.wallet.BalanceSection
import com.example.mobileproject.presentation.ui.component.wallet.MonthlySpendingCard
import com.example.mobileproject.presentation.ui.component.wallet.RecentActivitySection
import com.example.mobileproject.presentation.viewmodel.WalletViewModel
import java.text.DateFormatSymbols

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    
    var isMonthPickerVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFFFF0F0),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* TODO: Add Transaction */ },
                    containerColor = Color(0xFFFF8A80),
                    contentColor = Color.White,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }
        ) { paddingValues ->
            if (uiState.isLoading) {
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
                            transactions = uiState.recentTransactions,
                            onSeeAllClick = { /* TODO: Navigate to history */ }
                        )
                    }
                }
            }
        }

        // Month Picker Overlay
        if (isMonthPickerVisible) {
            // Darken background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isMonthPickerVisible = false }
            )

            // Popup Picker
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
                    Text(
                        text = "Select Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    HorizontalDivider(color = Color(0xFFFFF0F0))

                    val months = DateFormatSymbols().months
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
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
                                    .background(
                                        if (isSelected) Color(0xFFFFF0F0) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = month,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) Color(0xFFFF8A80) else Color.DarkGray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
