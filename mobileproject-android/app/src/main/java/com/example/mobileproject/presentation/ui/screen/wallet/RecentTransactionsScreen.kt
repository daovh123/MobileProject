/**
 * # RecentTransactionsScreen - Màn hình danh sách toàn bộ giao dịch
 *
 * Hiển thị danh sách tất cả giao dịch của ví dưới dạng LazyColumn.
 * Hỗ trợ 3 trạng thái:
 * - Loading (CircularProgressIndicator khi đang tải)
 * - Empty ("Không tìm thấy giao dịch")
 * - Data (danh sách TransactionItem)
 *
 * ## ViewModel bindings
 * - [WalletViewModel]: tải lại dữ liệu qua [LaunchedEffect] gọi loadData()
 *
 * ## Navigation triggers
 * - onNavigateBack: quay lại màn hình trước
 */
package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.presentation.ui.component.wallet.TransactionItem
import com.example.mobileproject.presentation.viewmodel.WalletViewModel

/**
 * Màn hình danh sách toàn bộ giao dịch.
 * Hiển thị loading, empty state hoặc LazyColumn các TransactionItem.
 *
 * @param onNavigateBack quay lại
 * @param viewModel WalletViewModel shared với WalletScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentTransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    // Tải lại dữ liệu khi composable mount
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tất cả giao dịch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading && uiState.allTransactions.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorScheme.primary
                )
            } else if (uiState.allTransactions.isEmpty()) {
                Text(
                    text = "Không tìm thấy giao dịch",
                    modifier = Modifier.align(Alignment.Center),
                    color = colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.allTransactions) { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
            }
        }
    }
}


