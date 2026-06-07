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

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val availableText = stringResource(R.string.add_expense_available, "%,d".format(uiState.availableBalance))
                        Text(
                            text = stringResource(R.string.add_expense_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                        Text(
                            text = availableText,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = colorScheme.primary
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
                        containerColor = colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.add_expense_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = colorScheme.onPrimary
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
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.surfaceContainerLowest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.add_expense_attach_title),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.add_expense_attach_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
