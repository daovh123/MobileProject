package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.component.wallet.BalanceSection
import com.example.mobileproject.presentation.ui.component.wallet.MonthlySpendingCard
import com.example.mobileproject.presentation.ui.component.wallet.RecentActivitySection
import com.example.mobileproject.presentation.ui.screen.home.components.ContributeGoalBottomSheet
import com.example.mobileproject.presentation.viewmodel.SavingGoalViewModel
import com.example.mobileproject.presentation.viewmodel.WalletViewModel
import java.text.DateFormatSymbols

@Composable
fun WalletScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToTopUp: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onNavigateToTransferMoney: () -> Unit,
    onNavigateToQRScanner: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel(),
    savingGoalViewModel: SavingGoalViewModel = hiltViewModel(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    val savingGoalState by savingGoalViewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    var isMonthPickerVisible by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var isContributeSheetVisible by remember { mutableStateOf(false) }
    var isTransferOptionsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        savingGoalViewModel.loadGoals()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = colorScheme.surfaceContainerLow, floatingActionButton = {}) { paddingValues ->
            if (uiState.isLoading && uiState.wallet == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    item { BalanceSection(balance = uiState.wallet?.balance ?: 0L) }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        MonthlySpendingCard(
                            spendingList = uiState.categoryBreakdown,
                            selectedMonth = selectedMonth,
                            onMonthClick = { isMonthPickerVisible = true },
                        )
                    }
                    item {
                        RecentActivitySection(
                            transactions = uiState.allTransactions,
                            onSeeAllClick = onSeeAllTransactions,
                        )
                    }
                }
            }
        }

        if (isMonthPickerVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable { isMonthPickerVisible = false },
            )
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(280.dp)
                    .heightIn(max = 450.dp)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLowest),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chọn tháng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colorScheme.surfaceVariant)
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
                                    .background(
                                        if (isSelected) colorScheme.surfaceVariant else Color.Transparent,
                                        RoundedCornerShape(8.dp),
                                    ),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(
                                    text = month,
                                    color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
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
                    .background(colorScheme.scrim.copy(alpha = 0.4f))
                    .clickable { isFabExpanded = false },
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = colorScheme.surfaceContainerLowest,
                        modifier = Modifier.clickable {
                            isFabExpanded = false
                            isTransferOptionsVisible = true
                        },
                    ) {
                        Text(
                            text = "Chuyển tiền",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FloatingActionButton(
                        onClick = {
                            isFabExpanded = false
                            isTransferOptionsVisible = true
                        },
                        containerColor = colorScheme.surfaceContainerLowest,
                        contentColor = colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                    ) { Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null) }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = colorScheme.surfaceContainerLowest,
                        modifier = Modifier.clickable {
                            isFabExpanded = false
                            isContributeSheetVisible = true
                        },
                    ) {
                        Text(
                            text = "Đóng góp vào mục tiêu",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FloatingActionButton(
                        onClick = {
                            isFabExpanded = false
                            isContributeSheetVisible = true
                        },
                        containerColor = colorScheme.surfaceContainerLowest,
                        contentColor = colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                    ) { Icon(imageVector = Icons.Default.Savings, contentDescription = null) }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = colorScheme.surfaceContainerLowest,
                        modifier = Modifier.clickable {
                            isFabExpanded = false
                            onNavigateToAddExpense()
                        },
                    ) {
                        Text(
                            text = "Thêm chi tiêu",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FloatingActionButton(
                        onClick = {
                            isFabExpanded = false
                            onNavigateToAddExpense()
                        },
                        containerColor = colorScheme.surfaceContainerLowest,
                        contentColor = colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                    ) { Icon(painter = painterResource(R.drawable.ic_wallet_24), contentDescription = null) }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = colorScheme.surfaceContainerLowest,
                        modifier = Modifier.clickable {
                            isFabExpanded = false
                            onNavigateToTopUp()
                        },
                    ) {
                        Text(
                            text = "Nạp tiền",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FloatingActionButton(
                        onClick = {
                            isFabExpanded = false
                            onNavigateToTopUp()
                        },
                        containerColor = colorScheme.surfaceContainerLowest,
                        contentColor = colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                    ) { Icon(imageVector = Icons.Default.Bolt, contentDescription = null) }
                }
            }
        }

        val rotation by animateFloatAsState(if (isFabExpanded) 45f else 0f, label = "wallet-fab-rotation")
        FloatingActionButton(
            onClick = { isFabExpanded = !isFabExpanded },
            containerColor = if (isFabExpanded) colorScheme.primary.copy(alpha = 0.8f) else colorScheme.primary,
            contentColor = colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(32.dp).rotate(rotation),
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
                        contributorId = if (isDirect) "me" else null,
                    )
                    isContributeSheetVisible = false
                },
            )
        }

        if (isTransferOptionsVisible) {
            TransferOptionsBottomSheet(
                onDismiss = { isTransferOptionsVisible = false },
                onManualTransfer = {
                    isTransferOptionsVisible = false
                    onNavigateToTransferMoney()
                },
                onScanQr = {
                    isTransferOptionsVisible = false
                    onNavigateToQRScanner()
                },
            )
        }
    }
}
