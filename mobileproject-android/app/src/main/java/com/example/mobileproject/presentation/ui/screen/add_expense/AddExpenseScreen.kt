package com.example.mobileproject.presentation.ui.screen.add_expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.screen.add_expense.components.*
import com.example.mobileproject.presentation.viewmodel.AddExpenseViewModel

/**
 * Màn hình Thêm chi tiêu (Add Expense) – form nhập khoản chi mới.
 *
 * Cấu trúc UI chính:
 * - TopAppBar: tiêu đề "Thêm chi tiêu" + số dư khả dụng, nút back.
 * - [AmountInput]: ô nhập số tiền lớn ở giữa, font size tự động co theo độ dài.
 * - [CategorySelector]: hàng 3 danh mục nhanh (Ăn uống/Hẹn hò/Khác) + nút More.
 * - [ExpenseFormFields]: ô nhập ghi chú + chọn ngày (DatePickerDialog native).
 * - [AttachReceiptSection]: placeholder đính kèm hóa đơn (chưa implement).
 * - Nút "Thêm chi tiêu" ở cuối (floating footer button).
 * - [CategoryBottomSheet]: bottom sheet chọn danh mục đầy đủ (hiện khi nhấn More).
 *
 * ViewModel: [AddExpenseViewModel] (Hilt-injected) – onAmountChange(), onCategorySelect(),
 * onNoteChange(), onDateChange(), saveExpense().
 *
 * Navigation: onNavigateBack khi lưu thành công (isSuccess) hoặc nhấn back.
 *
 * Layout: Scaffold + Column dọc scrollable, spacing 24dp, footer button
 * cố định ở bottom center.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCategorySheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = Color(0xFFFFF0F0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val availableText = stringResource(R.string.add_expense_available, "%,d".format(uiState.availableBalance))
                        Text(
                            text = stringResource(R.string.add_expense_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8A80)
                        )
                        Text(
                            text = availableText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = Color(0xFFFF8A80)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Amount Input Section
                AmountInput(
                    amount = uiState.amount,
                    onAmountChange = viewModel::onAmountChange
                )

                // Category Selector
                CategorySelector(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::onCategorySelect,
                    onMoreClick = { showCategorySheet = true }
                )

                // Note & Date Fields
                ExpenseFormFields(
                    note = uiState.note,
                    onNoteChange = viewModel::onNoteChange,
                    date = uiState.date,
                    onDateChange = viewModel::onDateChange
                )

                // Attach Receipt Placeholder
                AttachReceiptSection()
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error Snackbar
            uiState.error?.let { err ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text(stringResource(R.string.common_error)) },
                    text = { Text(err) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(stringResource(R.string.common_ok))
                        }
                    }
                )
            }

            // Footer Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { viewModel.saveExpense() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8A80)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.add_expense_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        if (showCategorySheet) {
            CategoryBottomSheet(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::onCategorySelect,
                onDismiss = { showCategorySheet = false }
            )
        }
    }
}

@Composable
fun AttachReceiptSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E1).copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFF0F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFFFF8A80))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.add_expense_attach_title),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )
                Text(
                    text = stringResource(R.string.add_expense_attach_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
