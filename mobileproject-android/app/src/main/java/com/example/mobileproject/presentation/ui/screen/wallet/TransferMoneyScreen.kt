package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val banks = remember { VietnamBankCatalog.banks }

    var accountNumber by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf<VietnamBank?>(null) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showBankSheet by remember { mutableStateOf(false) }

    val amountValue = amount.toLongOrNull() ?: 0L
    val canConfirm = accountNumber.length >= 6 &&
        selectedBank != null &&
        amountValue > 0L &&
        !uiState.isSubmitting &&
        !uiState.isWaitingBankConfirmation

    LaunchedEffect(scannedQrRaw) {
        if (scannedQrRaw.isBlank()) return@LaunchedEffect
        val resolved = resolveTransferQr(context, scannedQrRaw)
        if (resolved == null) {
            note = scannedQrRaw.take(160)
            snackbarHostState.showSnackbar("Không đọc được thông tin chuyển khoản từ QR.")
            return@LaunchedEffect
        }

        resolved.accountNumber?.takeIf { it.isNotBlank() }?.let { accountNumber = it }
        resolved.bank?.let { selectedBank = it }
        resolved.amount?.takeIf { it > 0L }?.let { amount = it.toString() }
        resolved.note?.takeIf { it.isNotBlank() }?.let { note = it }

        if (note.isBlank() && resolved.note.isNullOrBlank()) {
            note = resolved.rawContent.take(160)
        }

        snackbarHostState.showSnackbar(
            resolved.warning ?: "Đã đọc QR và tự động điền thông tin nếu QR có chứa dữ liệu VietQR."
        )
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showBankSheet) {
        BankSelectionSheet(
            banks = banks,
            selectedBank = selectedBank,
            onBankSelected = { bank ->
                selectedBank = bank
                showBankSheet = false
            },
            onDismiss = { showBankSheet = false },
            colorScheme = colorScheme,
        )
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
            SuccessContent(
                accountNumber = accountNumber,
                amountValue = amountValue,
                note = note,
                selectedBank = selectedBank,
                onNavigateBack = onNavigateBack,
                colorScheme = colorScheme,
                modifier = Modifier.padding(paddingValues),
            )
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
                WaitingBankCard(uiState = uiState, colorScheme = colorScheme)
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
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

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colorScheme.surfaceVariant,
                focusedContainerColor = colorScheme.surfaceVariant,
                unfocusedBorderColor = colorScheme.outlineVariant,
                focusedBorderColor = colorScheme.primary,
            )
            val textFieldShape = RoundedCornerShape(24.dp)

            OutlinedTextField(
                value = accountNumber,
                onValueChange = { input -> if (input.all(Char::isDigit)) accountNumber = input },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tài khoản") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = textFieldShape,
                colors = textFieldColors,
            )

            OutlinedTextField(
                value = selectedBank?.name.orEmpty(),
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text("Ngân hàng") },
                leadingIcon = {
                    if (selectedBank != null) {
                        BankLogo(bank = selectedBank, size = 24.dp)
                    } else {
                        Icon(Icons.Default.AccountBalance, contentDescription = null)
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showBankSheet = true }) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Chọn ngân hàng")
                    }
                },
                shape = textFieldShape,
                colors = textFieldColors,
            )

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
                shape = textFieldShape,
                colors = textFieldColors,
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                label = { Text("Nội dung") },
                shape = textFieldShape,
                colors = textFieldColors,
            )

            TextButton(
                onClick = {
                    note = buildString {
                        append("Rút tiền ")
                        append(selectedBank?.shortName ?: "BANK")
                        append(" ")
                        append(accountNumber)
                    }
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Dùng nội dung mặc định", color = Color(0xFFFF3B6B))
            }

            Button(
                onClick = {
                    val bank = selectedBank ?: return@Button
                    viewModel.submitTransfer(
                        amount = amountValue,
                        bankName = bank.name,
                        accountNumber = accountNumber,
                        note = note.ifBlank { null },
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(24.dp),
                enabled = canConfirm,
            ) {
                Text(
                    if (uiState.isSubmitting) "Đang tạo lệnh rút..." else "Xác nhận",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankSelectionSheet(
    banks: List<VietnamBank>,
    selectedBank: VietnamBank?,
    onBankSelected: (VietnamBank) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: ColorScheme,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = "Chọn ngân hàng",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(banks, key = { it.id }) { bank ->
                    BankItem(
                        bank = bank,
                        isSelected = selectedBank?.id == bank.id,
                        onClick = { onBankSelected(bank) },
                        colorScheme = colorScheme,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BankItem(
    bank: VietnamBank,
    isSelected: Boolean,
    onClick: () -> Unit,
    colorScheme: ColorScheme,
) {
    val borderColor = if (isSelected) colorScheme.primary else colorScheme.outlineVariant.copy(alpha = 0.4f)
    val containerColor = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.35f) else colorScheme.surfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BankLogo(bank = bank, size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bank.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                )
                Text(
                    text = bank.shortName,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(
    accountNumber: String,
    amountValue: Long,
    note: String,
    selectedBank: VietnamBank?,
    onNavigateBack: () -> Unit,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
            text = "Rút tiền đã được xác nhận",
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
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text("Quay lại")
        }
    }
}

@Composable
private fun WaitingBankCard(
    uiState: com.example.mobileproject.presentation.viewmodel.TransferMoneyUiState,
    colorScheme: ColorScheme,
) {
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
                text = if (uiState.isWaitingBankConfirmation) "Đang chờ xác nhận tiền ra" else "Trạng thái lệnh rút",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
            )
            uiState.transferCode?.takeIf { it.isNotBlank() }?.let {
                TransferSummaryRow("Mã giao dịch", it)
            }
            uiState.statusText?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            if (uiState.isWaitingBankConfirmation) {
                Text(
                    text = "Ứng dụng chưa tự chuyển tiền trong ngân hàng. Bạn vẫn cần mở ứng dụng ngân hàng, nhập mật khẩu/OTP và chuyển tiền thật.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun TransferSummaryRow(
    label: String,
    value: String,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
        )
    }
}
