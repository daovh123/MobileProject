package com.example.mobileproject.presentation.ui.screen.wallet

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.presentation.model.wallet.VietnamBank
import com.example.mobileproject.presentation.model.wallet.VietnamBankCatalog
import com.example.mobileproject.presentation.ui.screen.wallet.components.BankLogo
import com.example.mobileproject.presentation.viewmodel.TransferMoneyViewModel
import com.example.mobileproject.utils.formatSimpleAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferMoneyScreen(
    onNavigateBack: () -> Unit,
    scannedQrRaw: String = "",
    viewModel: TransferMoneyViewModel = hiltViewModel(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val banks = remember { VietnamBankCatalog.banks }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var accountNumber by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf<VietnamBank?>(null) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var bankMenuExpanded by remember { mutableStateOf(false) }

    val amountValue = amount.toLongOrNull() ?: 0L
    val canConfirm = accountNumber.length >= 6 && selectedBank != null && amountValue > 0L && !uiState.isSubmitting

    LaunchedEffect(scannedQrRaw) {
        if (scannedQrRaw.isNotBlank() && note.isBlank()) {
            note = "QR: ${scannedQrRaw.take(120)}"
            snackbarHostState.showSnackbar("Da doc QR. Vui long kiem tra STK/Ngân hàng/so tien roi xac nhan.")
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Chuyển tiền",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background),
            )
        },
    ) { paddingValues ->
        if (uiState.isSuccess) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(88.dp),
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Đã tạo giao dịch chuyển tiền",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        selectedBank?.let { bank ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                BankLogo(bank = bank, size = 30.dp)
                                Text(
                                    text = bank.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                )
                            }
                            HorizontalDivider()
                        }
                        TransferSummaryRow("Số tài khoản", accountNumber)
                        HorizontalDivider()
                        TransferSummaryRow("Số tiền", formatSimpleAmount(amountValue))
                        if (note.isNotBlank()) {
                            HorizontalDivider()
                            TransferSummaryRow("Nội dung", note)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Quay lại")
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (uiState.isWaitingBankConfirmation || !uiState.statusText.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = if (uiState.isWaitingBankConfirmation) "Dang cho xac nhan tien ra" else "Trang thai lenh rut",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                        uiState.transferCode?.takeIf { it.isNotBlank() }?.let {
                            TransferSummaryRow("Ma giao dich", it)
                        }
                        uiState.statusText?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = colorScheme.primaryContainer.copy(alpha = 0.55f),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = colorScheme.primary,
                    )
                    Column {
                        Text(
                            text = "Chuyển khoản thủ công",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Nhập tài khoản, chọn ngân hàng, nhập tiền và xác nhận.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = accountNumber,
                onValueChange = { input -> if (input.all(Char::isDigit)) accountNumber = input },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tài khoản") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedBank?.name.orEmpty(),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    label = { Text("Ngân hàng") },
                    leadingIcon = {
                        if (selectedBank != null) {
                            BankLogo(bank = selectedBank, size = 24.dp)
                        } else {
                            Icon(Icons.Default.AccountBalance, contentDescription = null)
                        }
                    },
                    trailingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                )
                DropdownMenu(
                    expanded = bankMenuExpanded,
                    onDismissRequest = { bankMenuExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.92f),
                ) {
                    banks.forEach { bank ->
                        DropdownMenuItem(
                            text = { Text(bank.name) },
                            leadingIcon = { BankLogo(bank = bank, size = 22.dp) },
                            onClick = {
                                selectedBank = bank
                                bankMenuExpanded = false
                            },
                        )
                    }
                }
                Surface(
                    modifier = Modifier.matchParentSize(),
                    color = Color.Transparent,
                    onClick = { bankMenuExpanded = true },
                ) {}
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { input -> if (input.all(Char::isDigit)) amount = input },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    if (amountValue > 0L) {
                        Text(formatSimpleAmount(amountValue))
                    }
                },
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nội dung") },
                placeholder = { Text("Nhập nội dung chuyển tiền") },
                minLines = 2,
            )

            TextButton(
                onClick = { if (note.isBlank()) note = "Chuyển khoản nội bộ" },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Dùng nội dung mặc định")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val bank = selectedBank ?: return@Button
                    viewModel.submitTransfer(
                        amount = amountValue,
                        bankName = bank.name,
                        accountNumber = accountNumber,
                        note = note.takeIf { it.isNotBlank() },
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            ) {
                Text(if (uiState.isSubmitting) "Đang xử lý..." else "Xác nhận")
            }
        }
    }
}

@Composable
private fun TransferSummaryRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
