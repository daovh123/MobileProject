package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.entity.Transaction
import com.example.mobileproject.domain.entity.TransactionType
import com.example.mobileproject.presentation.viewmodel.AnalyticsViewModel
import com.example.mobileproject.presentation.viewmodel.GoalViewModel
import com.example.mobileproject.presentation.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    coupleId: String = "5e85766e-7659-4403-afcb-f7db316b6f21",
    transactionViewModel: TransactionViewModel = hiltViewModel(),
    goalViewModel: GoalViewModel = hiltViewModel(),
    analyticsViewModel: AnalyticsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var showAddGoalSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val transactionState by transactionViewModel.uiState.collectAsState()
    val goalState by goalViewModel.uiState.collectAsState()
    val analyticsState by analyticsViewModel.uiState.collectAsState()

    LaunchedEffect(coupleId) {
        transactionViewModel.loadTransactions(coupleId)
        goalViewModel.loadGoals(coupleId)
    }

    val tabs = listOf("Ví", "Mục tiêu", "Thống kê")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> showAddTransactionSheet = true
                        1 -> showAddGoalSheet = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> WalletTab(
                    totalBalance = transactionState.totalBalance,
                    isLoading = transactionState.isLoading,
                    transactions = transactionState.transactions
                )
                1 -> GoalsTab(
                    goals = goalState.goals,
                    isLoading = goalState.isLoading,
                    onContribute = { goalId, amount ->
                        goalViewModel.contributeFromWallet(goalId, amount, coupleId, null)
                    }
                )
                2 -> AnalyticsTab(
                    categoryBreakdown = analyticsState.categoryBreakdown,
                    isLoading = analyticsState.isLoading,
                    coupleId = coupleId,
                    onLoadAnalytics = { startDate, endDate ->
                        analyticsViewModel.loadCategoryBreakdown(coupleId, startDate, endDate)
                    }
                )
            }
        }
    }

    if (showAddTransactionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddTransactionSheet = false },
            sheetState = sheetState
        ) {
            AddTransactionSheet(
                onDismiss = {
                    scope.launch { sheetState.hide() }
                    showAddTransactionSheet = false
                },
                onAddExpense = { amount, category, note ->
                    transactionViewModel.createTransaction(coupleId, amount, "EXPENSE", category, note)
                    scope.launch { sheetState.hide() }
                    showAddTransactionSheet = false
                },
                onAddIncome = { amount, targetType, goalId, note ->
                    transactionViewModel.processIncome(coupleId, amount, targetType, goalId, note)
                    scope.launch { sheetState.hide() }
                    showAddTransactionSheet = false
                }
            )
        }
    }

    if (showAddGoalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddGoalSheet = false },
            sheetState = sheetState
        ) {
            AddGoalSheet(
                onDismiss = {
                    scope.launch { sheetState.hide() }
                    showAddGoalSheet = false
                },
                onCreateGoal = { name, targetAmount, deadline ->
                    goalViewModel.createGoal(coupleId, name, targetAmount, deadline)
                    scope.launch { sheetState.hide() }
                    showAddGoalSheet = false
                }
            )
        }
    }
}

@Composable
private fun WalletTab(
    totalBalance: Long,
    isLoading: Boolean,
    transactions: List<Transaction>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Số dư ví",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        text = formatCurrency(totalBalance),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Giao dịch gần đây",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Text(
                text = "Chưa có giao dịch nào",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions.take(10)) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val isExpense = transaction.type == TransactionType.EXPENSE

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isExpense) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isExpense) "↓" else "↑",
                        color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = transaction.note.ifBlank { if (isExpense) "Chi tiêu" else "Thu nhập" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "${if (isExpense) "-" else "+"}${formatCurrency(transaction.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun GoalsTab(
    goals: List<SavingGoal>,
    isLoading: Boolean,
    onContribute: (String, Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (goals.isEmpty()) {
            item {
                Text(
                    text = "Chưa có mục tiêu tiết kiệm nào.\nTạo mục tiêu để bắt đầu!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(goals) { goal ->
                GoalCard(
                    goal = goal,
                    onContribute = { onContribute(goal.id, it) }
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: SavingGoal,
    onContribute: (Long) -> Unit
) {
    var showContributeDialog by remember { mutableStateOf(false) }
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                StatusChip(status = goal.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
                color = if (goal.status == GoalStatus.ACHIEVED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatCurrency(goal.currentAmount)} / ${formatCurrency(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showContributeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nạp tiền")
            }
        }
    }

    if (showContributeDialog) {
        ContributeDialog(
            goalName = goal.name,
            onDismiss = { showContributeDialog = false },
            onConfirm = { amount ->
                onContribute(amount)
                showContributeDialog = false
            }
        )
    }
}

@Composable
private fun StatusChip(status: GoalStatus) {
    val (text, color) = when (status) {
        GoalStatus.IN_PROGRESS -> "Đang tiết kiệm" to MaterialTheme.colorScheme.secondary
        GoalStatus.ACHIEVED -> "Đã đạt" to MaterialTheme.colorScheme.primary
        GoalStatus.FAILED -> "Thất bại" to MaterialTheme.colorScheme.error
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun AnalyticsTab(
    categoryBreakdown: List<CategoryBreakdown>,
    isLoading: Boolean,
    coupleId: String,
    onLoadAnalytics: (String, String) -> Unit
) {
    var startDate by remember { mutableStateOf("2026-04-01T00:00:00Z") }
    var endDate by remember { mutableStateOf("2026-04-30T23:59:59Z") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onLoadAnalytics(startDate, endDate) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Xem thống kê")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (categoryBreakdown.isEmpty()) {
            Text(
                text = "Chưa có dữ liệu thống kê",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Chi tiêu theo danh mục",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            categoryBreakdown.forEach { item ->
                CategoryBreakdownItem(category = item.category, amount = item.totalAmount)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CategoryBreakdownItem(category: String, amount: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AddTransactionSheet(
    onDismiss: () -> Unit,
    onAddExpense: (Long, String, String?) -> Unit,
    onAddIncome: (Long, String, String?, String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("FOOD_DRINK") }
    var note by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = if (isIncome) "Thêm thu nhập" else "Thêm chi tiêu",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() } },
            label = { Text("Số tiền") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (!isIncome) {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Danh mục (FOOD_DRINK, TRANSPORT,...)") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Đích đến: ")
                Spacer(modifier = Modifier.width(8.dp))
                Text("WALLET")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Ghi chú") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val amountLong = amount.toLongOrNull() ?: 0L
                    if (isIncome) {
                        onAddIncome(amountLong, "WALLET", null, note.ifBlank { null })
                    } else {
                        onAddExpense(amountLong, category, note.ifBlank { null })
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Lưu")
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Hủy")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AddGoalSheet(
    onDismiss: () -> Unit,
    onCreateGoal: (String, Long, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Tạo mục tiêu tiết kiệm",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên mục tiêu") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = targetAmount,
            onValueChange = { targetAmount = it.filter { c -> c.isDigit() } },
            label = { Text("Số tiền mục tiêu") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = deadline,
            onValueChange = { deadline = it },
            label = { Text("Hạn chót (ISO format)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val amountLong = targetAmount.toLongOrNull() ?: 0L
                    onCreateGoal(name, amountLong, deadline.ifBlank { null })
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Tạo")
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Hủy")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ContributeDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nạp tiền vào $goalName") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = { Text("Số tiền") }
            )
        },
        confirmButton = {
            Button(onClick = {
                amount.toLongOrNull()?.let { onConfirm(it) }
            }) {
                Text("Nạp")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

private fun formatCurrency(amount: Long): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount) + " đ"
}