package com.example.mobileproject.presentation.ui.screen.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.utils.formatAmountWithDots
import com.example.mobileproject.utils.formatSimpleAmount
import com.example.mobileproject.utils.stripAmountFormatting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeGoalBottomSheet(
    selectedGoal: SavingGoal?,
    availableGoals: List<SavingGoal>,
    onDismiss: () -> Unit,
    onConfirm: (goalId: String, amount: Long, note: String?, isDirect: Boolean) -> Unit,
    onWithdrawToWallet: (SavingGoal) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val initialGoalId = selectedGoal?.id ?: availableGoals.firstOrNull()?.id.orEmpty()
    var selectedGoalId by rememberSaveable(selectedGoal?.id, availableGoals.firstOrNull()?.id) {
        mutableStateOf(initialGoalId)
    }
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var isDirect by rememberSaveable { mutableStateOf(false) }
    var isGoalPickerExpanded by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val goal = remember(selectedGoalId, availableGoals) {
        availableGoals.firstOrNull { it.id == selectedGoalId } ?: availableGoals.firstOrNull()
    }
    val isCompleted = goal?.let(::isGoalReadyToWithdraw) == true
    val isWithdrawn = goal?.status == GoalStatus.WITHDRAWN
    val remainingAmount = goal?.let(::remainingAmountForGoal) ?: 0L
    val amountValue = amount.toLongOrNull() ?: 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.contribute_goal_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(colorScheme.surfaceContainer, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.common_close),
                        tint = colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.contribute_select_goal),
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))

            GoalPickerCard(
                selectedGoal = goal,
                goals = availableGoals,
                isExpanded = isGoalPickerExpanded,
                onToggle = { if (availableGoals.size > 1) isGoalPickerExpanded = !isGoalPickerExpanded },
                onGoalSelected = {
                    selectedGoalId = it.id
                    isGoalPickerExpanded = false
                    amount = ""
                    note = ""
                },
            )

            goal?.let { selected ->
                Spacer(modifier = Modifier.height(18.dp))
                GoalProgressOverview(goal = selected)

                if (isCompleted || isWithdrawn) {
                    Spacer(modifier = Modifier.height(20.dp))
                    CompletedGoalActionCard(
                        goal = selected,
                        onWithdrawToWallet = {
                            onWithdrawToWallet(selected)
                            onDismiss()
                        },
                    )
                } else {
                    if (remainingAmount > 0L) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Surface(
                            onClick = { amount = remainingAmount.toString() },
                            shape = RoundedCornerShape(16.dp),
                            color = colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Điền phần còn thiếu",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSecondaryContainer,
                                    )
                                    Text(
                                        text = "${formatSimpleAmount(remainingAmount)} đ để hoàn thành mục tiêu này",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.contribute_payment_method),
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PaymentMethodCard(
                            title = stringResource(R.string.contribute_from_wallet),
                            icon = Icons.Default.AccountBalanceWallet,
                            isSelected = !isDirect,
                            modifier = Modifier.weight(0.8f),
                            onClick = { isDirect = false },
                        )
                        PaymentMethodCard(
                            title = stringResource(R.string.contribute_direct_pay),
                            icon = Icons.Default.Payments,
                            isSelected = isDirect,
                            modifier = Modifier.weight(1.2f),
                            onClick = { isDirect = true },
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.contribute_amount_label),
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = formatAmountWithDots(amount),
                        onValueChange = { amount = stripAmountFormatting(it) },
                        prefix = { Text("đ ", color = colorScheme.primary, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            if (remainingAmount > 0L) {
                                Text(
                                    text = "Còn thiếu ${formatSimpleAmount(remainingAmount)} đ để hoàn thành.",
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = colorScheme.surfaceContainerLow,
                            focusedContainerColor = colorScheme.surfaceContainerLowest,
                            unfocusedBorderColor = colorScheme.outlineVariant,
                            focusedBorderColor = colorScheme.primary,
                        ),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.contribute_note_label),
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("Trích quỹ lương, thưởng thêm...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = colorScheme.surfaceContainerLow,
                            focusedContainerColor = colorScheme.surfaceContainerLowest,
                            unfocusedBorderColor = colorScheme.outlineVariant,
                            focusedBorderColor = colorScheme.primary,
                        ),
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            val confirmedGoal = goal ?: return@Button
                            if (amountValue > 0L) {
                                onConfirm(confirmedGoal.id, amountValue, note.ifBlank { null }, isDirect)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        shape = RoundedCornerShape(28.dp),
                        enabled = amountValue > 0L,
                    ) {
                        Text(
                            text = stringResource(R.string.contribute_confirm),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            } ?: run {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Chưa có mục tiêu tiết kiệm nào để đóng góp.",
                        modifier = Modifier.padding(18.dp),
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun GoalPickerCard(
    selectedGoal: SavingGoal?,
    goals: List<SavingGoal>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onGoalSelected: (SavingGoal) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = colorScheme.surfaceContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedGoal?.name ?: stringResource(R.string.contribute_select_goal_placeholder),
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    selectedGoal?.let {
                        Text(
                            text = buildGoalStatusCaption(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (goals.size > 1) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        AnimatedVisibility(visible = isExpanded && goals.size > 1) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                goals.forEach { goal ->
                    Surface(
                        onClick = { onGoalSelected(goal) },
                        shape = RoundedCornerShape(18.dp),
                        color = if (selectedGoal?.id == goal.id) {
                            colorScheme.primaryContainer.copy(alpha = 0.6f)
                        } else {
                            colorScheme.surfaceContainerLow
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = goal.name,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = formatSimpleAmount(displaySavedAmount(goal)),
                                    color = colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                            Text(
                                text = buildGoalStatusCaption(goal),
                                color = colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalProgressOverview(goal: SavingGoal) {
    val colorScheme = MaterialTheme.colorScheme
    val savedAmount = displaySavedAmount(goal)
    val remainingAmount = remainingAmountForGoal(goal)
    val progress = when {
        goal.targetAmount <= 0L -> 0f
        goal.status == GoalStatus.WITHDRAWN -> 1f
        else -> (savedAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f)
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Tiến độ mục tiêu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                GoalStatusBadge(goal = goal)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GoalInfoStat(
                    title = "Mức",
                    value = "${formatSimpleAmount(goal.targetAmount)} đ",
                    modifier = Modifier.weight(1f),
                )
                GoalInfoStat(
                    title = if (goal.status == GoalStatus.WITHDRAWN) "Đã rút" else "Đã góp",
                    value = "${formatSimpleAmount(savedAmount)} đ",
                    modifier = Modifier.weight(1f),
                )
                GoalInfoStat(
                    title = "Còn thiếu",
                    value = "${formatSimpleAmount(remainingAmount)} đ",
                    modifier = Modifier.weight(1f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceContainerHighest,
                )
                Text(
                    text = buildGoalStatusCaption(goal),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GoalInfoStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun GoalStatusBadge(goal: SavingGoal) {
    val colorScheme = MaterialTheme.colorScheme
    val (label, containerColor, contentColor) = when {
        goal.status == GoalStatus.WITHDRAWN -> Triple(
            "Đã rút",
            colorScheme.tertiaryContainer.copy(alpha = 0.65f),
            colorScheme.onTertiaryContainer,
        )
        isGoalReadyToWithdraw(goal) -> Triple(
            "Đã hoàn thành",
            colorScheme.primaryContainer.copy(alpha = 0.75f),
            colorScheme.onPrimaryContainer,
        )
        else -> Triple(
            "Đang góp",
            colorScheme.secondaryContainer.copy(alpha = 0.75f),
            colorScheme.onSecondaryContainer,
        )
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CompletedGoalActionCard(
    goal: SavingGoal,
    onWithdrawToWallet: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val hasBeenWithdrawn = goal.status == GoalStatus.WITHDRAWN

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (hasBeenWithdrawn) {
            colorScheme.surfaceContainerLow
        } else {
            colorScheme.primaryContainer.copy(alpha = 0.55f)
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (hasBeenWithdrawn) colorScheme.tertiary else colorScheme.primary,
                )
                Column {
                    Text(
                        text = if (hasBeenWithdrawn) "Khoản này đã được rút về ví chung" else "Mục tiêu đã đủ tiền",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = if (hasBeenWithdrawn) {
                            "Số tiền đã quay về ví chung và có thể dùng tiếp cho các khoản khác."
                        } else {
                            "Bạn có thể rút ${formatSimpleAmount(goal.currentAmount)} đ về ví chung ngay bây giờ."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (!hasBeenWithdrawn) {
                HorizontalDivider(color = colorScheme.primary.copy(alpha = 0.12f))
                Button(
                    onClick = onWithdrawToWallet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                ) {
                    Text(
                        text = "Rút về ví chung",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) colorScheme.primary else colorScheme.surfaceContainer,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun isGoalReadyToWithdraw(goal: SavingGoal): Boolean {
    return goal.status == GoalStatus.ACHIEVED || goal.currentAmount >= goal.targetAmount
}

private fun remainingAmountForGoal(goal: SavingGoal): Long {
    return when {
        goal.status == GoalStatus.WITHDRAWN -> 0L
        isGoalReadyToWithdraw(goal) -> 0L
        else -> (goal.targetAmount - goal.currentAmount).coerceAtLeast(0L)
    }
}

private fun displaySavedAmount(goal: SavingGoal): Long {
    return if (goal.status == GoalStatus.WITHDRAWN) {
        goal.withdrawnAmount
    } else {
        goal.currentAmount
    }
}

private fun buildGoalStatusCaption(goal: SavingGoal): String {
    return when {
        goal.status == GoalStatus.WITHDRAWN -> "Đã rút ${formatSimpleAmount(goal.withdrawnAmount)} đ về ví chung."
        isGoalReadyToWithdraw(goal) -> "Đã góp đủ ${formatSimpleAmount(goal.targetAmount)} đ. Có thể rút về ví chung."
        else -> "Đã góp ${formatSimpleAmount(goal.currentAmount)} đ, còn ${formatSimpleAmount(remainingAmountForGoal(goal))} đ."
    }
}
