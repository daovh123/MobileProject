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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.mobileproject.presentation.viewmodel.TopUpViewModel
import com.example.mobileproject.presentation.viewmodel.VietnamBank
import com.example.mobileproject.utils.formatSimpleAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferMoneyScreen(
    onNavigateBack: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val banks = remember { TopUpViewModel.vietnamBanks }

    var accountNumber by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf<VietnamBank?>(null) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var bankMenuExpanded by remember { mutableStateOf(false) }
    var isConfirmed by remember { mutableStateOf(false) }

    val amountValue = amount.toLongOrNull() ?: 0L
    val canConfirm = accountNumber.length >= 6 && selectedBank != null && amountValue > 0L

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Chuyen tien",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                ),
            )
        },
    ) { paddingValues ->
        if (isConfirmed) {
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
                    text = "Da xac nhan chuyen tien",
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
                        TransferSummaryRow("So tai khoan", accountNumber)
                        HorizontalDivider()
                        TransferSummaryRow("Ngan hang", selectedBank?.name.orEmpty())
                        HorizontalDivider()
                        TransferSummaryRow("So tien", formatSimpleAmount(amountValue))
                        if (note.isNotBlank()) {
                            HorizontalDivider()
                            TransferSummaryRow("Noi dung", note)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Quay lai")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
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
                                text = "Chuyen khoan thu cong",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Nhap tai khoan, chon ngan hang, nhap tien va xac nhan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { input ->
                        if (input.all(Char::isDigit)) {
                            accountNumber = input
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("So tai khoan") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedBank?.name.orEmpty(),
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth(),
                        readOnly = true,
                        label = { Text("Ngan hang") },
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
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    selectedBank = bank
                                    bankMenuExpanded = false
                                },
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .matchParentSize(),
                        color = Color.Transparent,
                        onClick = { bankMenuExpanded = true },
                    ) {}
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        if (input.all(Char::isDigit)) {
                            amount = input
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("So tien") },
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
                    label = { Text("Noi dung") },
                    placeholder = { Text("Nhap noi dung chuyen tien") },
                    minLines = 2,
                )

                TextButton(
                    onClick = {
                        if (note.isBlank()) {
                            note = "Chuyen tien"
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Dung noi dung mac dinh")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { isConfirmed = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = canConfirm,
                ) {
                    Text("Xac nhan")
                }
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
