package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.presentation.model.wallet.VietnamBank
import com.example.mobileproject.presentation.model.wallet.VietnamBankCatalog
import com.example.mobileproject.presentation.ui.screen.wallet.components.BankLogo
import com.example.mobileproject.presentation.viewmodel.TopUpNavigationEvent
import com.example.mobileproject.presentation.viewmodel.TopUpPaymentMode
import com.example.mobileproject.presentation.viewmodel.TopUpStep
import com.example.mobileproject.presentation.viewmodel.TopUpUiState
import com.example.mobileproject.presentation.viewmodel.TopUpViewModel
import com.example.mobileproject.utils.formatSimpleAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQR: (topUpId: String) -> Unit,
    onNavigateToBankRedirect: (topUpId: String) -> Unit,
    viewModel: TopUpViewModel = hiltViewModel(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is TopUpNavigationEvent.OpenQr -> onNavigateToQR(event.topUpId)
                is TopUpNavigationEvent.OpenBankRedirect -> onNavigateToBankRedirect(event.topUpId)
            }
        }
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nạp tiền",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.currentStep == TopUpStep.BANK_SELECT) {
                                viewModel.goToPreviousStep()
                            } else {
                                onNavigateBack()
                            }
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background),
            )
        },
    ) { paddingValues ->
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                if (targetState == TopUpStep.BANK_SELECT) {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "top_up_step_transition",
        ) { step ->
            when (step) {
                TopUpStep.AMOUNT -> AmountStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    colorScheme = colorScheme,
                    modifier = Modifier.padding(paddingValues),
                )

                TopUpStep.BANK_SELECT -> BankSelectStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    colorScheme = colorScheme,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun AmountStep(
    uiState: TopUpUiState,
    viewModel: TopUpViewModel,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Bạn muốn nạp bao nhiêu?", color = colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                        color = colorScheme.primary.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            if (uiState.amount.isEmpty()) {
                                Text(
                                    text = "0",
                                    style = TextStyle(
                                        fontSize = fontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary.copy(alpha = 0.2f),
                                        textAlign = TextAlign.Center,
                                    ),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
                    .background(colorScheme.surfaceVariant),
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Ghi chú (tùy chọn)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                placeholder = { Text("Ví dụ: Nạp ví để chi tiêu tuần này") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorScheme.surfaceVariant,
                    focusedContainerColor = colorScheme.surfaceVariant,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    focusedBorderColor = colorScheme.primary,
                ),
            )
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "Số dư dự kiến sau khi nạp: ",
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = formatSimpleAmount(uiState.predictedBalance),
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Button(
            onClick = viewModel::goToNextStep,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            enabled = uiState.isAmountValid,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Chọn ngân hàng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = "Giao dịch được mã hóa an toàn",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun BankSelectStep(
    uiState: TopUpUiState,
    viewModel: TopUpViewModel,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
) {
    val amount = uiState.amount.toLongOrNull() ?: 0L

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nạp ${formatSimpleAmount(amount)} VNĐ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimaryContainer,
                )
            }
        }

        Text(
            text = "Chọn ngân hàng",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(VietnamBankCatalog.banks, key = { it.id }) { bank ->
                BankItem(
                    bank = bank,
                    isSelected = uiState.selectedBank?.id == bank.id,
                    onClick = { viewModel.onBankSelected(bank) },
                    colorScheme = colorScheme,
                )
            }
        }

        if (uiState.selectedBank != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                uiState.error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Button(
                    onClick = { viewModel.createTopUpRequest(TopUpPaymentMode.QR) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    enabled = !uiState.isLoading,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Tạo mã QR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.createTopUpRequest(TopUpPaymentMode.BANK_REDIRECT) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    enabled = !uiState.isLoading,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Mở app ngân hàng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
