package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.viewmodel.TopUpStep
import com.example.mobileproject.presentation.viewmodel.TopUpViewModel
import com.example.mobileproject.presentation.viewmodel.VietnamBank
import com.example.mobileproject.utils.formatSimpleAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQR: (amount: Long, bankId: String, bankName: String, note: String) -> Unit,
    onNavigateToBankRedirect: (amount: Long, bankId: String, bankName: String, note: String) -> Unit,
    viewModel: TopUpViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.top_up_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep == TopUpStep.BANK_SELECT) {
                            viewModel.goToPreviousStep()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                if (targetState == TopUpStep.BANK_SELECT) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "step_transition"
        ) { step ->
            when (step) {
                TopUpStep.AMOUNT -> AmountStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    colorScheme = colorScheme,
                    modifier = Modifier.padding(paddingValues)
                )
                TopUpStep.BANK_SELECT -> BankSelectStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    colorScheme = colorScheme,
                    onNavigateToQR = onNavigateToQR,
                    onNavigateToBankRedirect = onNavigateToBankRedirect,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun AmountStep(
    uiState: com.example.mobileproject.presentation.viewmodel.TopUpUiState,
    viewModel: TopUpViewModel,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Amount Input - Fixed Alignment and Auto-scaling
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.top_up_amount_prompt), color = colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Dùng một text ẩn để đo kích thước hoặc dùng logic fontSize dựa trên độ dài chuỗi
                val fontSize = when {
                    uiState.amount.length > 12 -> 32.sp
                    uiState.amount.length > 9 -> 44.sp
                    uiState.amount.length > 6 -> 56.sp
                    else -> 72.sp
                }

                BasicTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    textStyle = TextStyle(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.amount.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.top_up_amount_placeholder),
                                    style = TextStyle(
                                        fontSize = fontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary.copy(alpha = 0.2f),
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.width(120.dp).height(2.dp).background(colorScheme.surfaceVariant))
        }

        // Destination
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.top_up_destination_label), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = uiState.destination, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = colorScheme.primary)
                }
            }
        }

        // Note
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.top_up_note_label), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                placeholder = { Text(stringResource(R.string.top_up_note_placeholder), color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorScheme.surfaceVariant,
                    focusedContainerColor = colorScheme.surfaceVariant,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = colorScheme.primary
                )
            )
        }

        // Predicted Balance
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = stringResource(R.string.top_up_predicted_balance_label), color = colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatSimpleAmount(uiState.predictedBalance),
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Continue Button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = { viewModel.goToNextStep() },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                enabled = uiState.isAmountValid
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Chọn ngân hàng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.top_up_secure_note),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BankSelectStep(
    uiState: com.example.mobileproject.presentation.viewmodel.TopUpUiState,
    viewModel: TopUpViewModel,
    colorScheme: ColorScheme,
    onNavigateToQR: (amount: Long, bankId: String, bankName: String, note: String) -> Unit,
    onNavigateToBankRedirect: (amount: Long, bankId: String, bankName: String, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val amount = uiState.amount.toLongOrNull() ?: 0L

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Amount summary chip
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nạp ${formatSimpleAmount(amount)} VNĐ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimaryContainer
                )
            }
        }

        // Bank selection header
        Text(
            text = "CHỌN NGÂN HÀNG",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        // Bank grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(TopUpViewModel.vietnamBanks) { bank ->
                BankItem(
                    bank = bank,
                    isSelected = uiState.selectedBank?.id == bank.id,
                    onClick = { viewModel.onBankSelected(bank) },
                    colorScheme = colorScheme
                )
            }
        }

        // Payment method buttons (only visible when bank is selected)
        AnimatedVisibility(
            visible = uiState.selectedBank != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "PHƯƠNG THỨC THANH TOÁN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                // QR Code button
                Button(
                    onClick = {
                        uiState.selectedBank?.let { bank ->
                            onNavigateToQR(
                                amount,
                                bank.id,
                                bank.name,
                                uiState.note.ifBlank { "Nạp tiền ví You & Me" }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Tạo mã QR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bank app redirect button
                OutlinedButton(
                    onClick = {
                        uiState.selectedBank?.let { bank ->
                            onNavigateToBankRedirect(
                                amount,
                                bank.id,
                                bank.name,
                                uiState.note.ifBlank { "Nạp tiền ví You & Me" }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Mở app ngân hàng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BankItem(
    bank: VietnamBank,
    isSelected: Boolean,
    onClick: () -> Unit,
    colorScheme: ColorScheme
) {
    val bankColor = Color(bank.color)
    val borderColor = if (isSelected) colorScheme.primary else Color.Transparent
    val containerColor = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.5f) else colorScheme.surfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bank icon (letter badge)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bankColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bank.shortName.take(2),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Bank name
            Text(
                text = bank.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
