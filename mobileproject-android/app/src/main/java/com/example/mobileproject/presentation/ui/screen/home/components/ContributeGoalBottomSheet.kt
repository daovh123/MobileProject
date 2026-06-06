/**
 * ContributeGoalBottomSheet - Bottom sheet cho phép đóng góp tiền vào mục tiêu tiết kiệm.
 *
 * Mục đích:
 * - Chọn mục tiêu tiết kiệm (nếu có nhiều).
 * - Chọn phương thức thanh toán: từ ví hoặc đóng góp trực tiếp.
 * - Nhập số tiền và ghi chú.
 *
 * Layout:
 * - [ModalBottomSheet] với nội dung cuộn dọc.
 * - Goal picker (nếu > 1 mục tiêu) → Amount input → Note input → Confirm button.
 *
 * Được sử dụng bởi: [HomeScreen] khi nhấn nút "Đóng góp".
 */
package com.example.mobileproject.presentation.ui.screen.home.components

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.utils.formatSimpleAmount

/**
 * ContributeGoalBottomSheet - Bottom sheet cho phép đóng góp tiền vào mục tiêu tiết kiệm.
 *
 * Mục đích:
 * - Chọn mục tiêu tiết kiệm (nếu có nhiều).
 * - Chọn phương thức thanh toán: từ ví hoặc đóng góp trực tiếp.
 * - Nhập số tiền và ghi chú.
 *
 * Layout:
 * - [ModalBottomSheet] với nội dung cuộn dọc.
 * - Goal picker (nếu > 1 mục tiêu) → Amount input → Note input → Confirm button.
 *
 * @param selectedGoal Mục tiêu đã chọn trước (null nếu chưa chọn).
 * @param availableGoals Danh sách mục tiêu tiết kiệm khả dụng.
 * @param onDismiss Callback khi đóng bottom sheet.
 * @param onConfirm Callback khi xác nhận: (goalId, amount, note, isDirect).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeGoalBottomSheet(
    selectedGoal: SavingGoal?,
    availableGoals: List<SavingGoal>,
    onDismiss: () -> Unit,
    onConfirm: (goalId: String, amount: Long, note: String?, isDirect: Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    var goal by remember { mutableStateOf(selectedGoal ?: availableGoals.firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isDirect by remember { mutableStateOf(false) }
    var showGoalPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
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
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.common_close),
                        tint = colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(R.string.contribute_select_goal),
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { if (availableGoals.size > 1) showGoalPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.surfaceContainer,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = goal?.name ?: stringResource(R.string.contribute_select_goal_placeholder),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    if (availableGoals.size > 1) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(R.string.contribute_payment_method),
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
                    modifier = Modifier.weight(1f),
                    onClick = { isDirect = false },
                )
                PaymentMethodCard(
                    title = stringResource(R.string.contribute_direct_pay),
                    icon = Icons.Default.Payments,
                    isSelected = isDirect,
                    modifier = Modifier.weight(1f),
                    onClick = { isDirect = true },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(R.string.contribute_amount_label),
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                prefix = { Text("$ ", color = colorScheme.primary, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                stringResource(R.string.contribute_note_label),
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Trich quy luong, thuong them...") },
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
                    goal?.let { g ->
                        if (amount.isNotBlank()) {
                            onConfirm(g.id, amount.toLong(), note.ifBlank { null }, isDirect)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                shape = RoundedCornerShape(28.dp),
                enabled = goal != null && amount.isNotBlank(),
            ) {
                Text(
                    stringResource(R.string.contribute_confirm),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showGoalPicker) {
            GoalPickerSheet(
                goals = availableGoals,
                onGoalSelected = { goal = it; showGoalPicker = false },
                onDismiss = { showGoalPicker = false },
            )
        }
    }
}

/**
 * Composable card chọn phương thức thanh toán (ví hoặc trực tiếp).
 *
 * @param title Nhãn phương thức.
 * @param icon Icon minh họa.
 * @param isSelected Có đang được chọn không.
 * @param modifier Modifier tùy chỉnh.
 * @param onClick Callback khi nhấn.
 */
@Composable
fun PaymentMethodCard(
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

/**
 * Composable bottom sheet chọn mục tiêu tiết kiệm.
 * Hiển thị danh sách mục tiêu với tên và số tiền hiện tại.
 *
 * @param goals Danh sách mục tiêu khả dụng.
 * @param onGoalSelected Callback khi chọn mục tiêu.
 * @param onDismiss Callback khi đóng sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalPickerSheet(
    goals: List<SavingGoal>,
    onGoalSelected: (SavingGoal) -> Unit,
    onDismiss: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                stringResource(R.string.contribute_select_goal_placeholder),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            goals.forEach { goal ->
                Surface(
                    onClick = { onGoalSelected(goal) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(goal.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(formatSimpleAmount(goal.currentAmount), color = colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
