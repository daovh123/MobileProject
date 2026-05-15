package com.example.mobileproject.presentation.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.utils.formatSimpleAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeGoalBottomSheet(
    selectedGoal: SavingGoal?,
    availableGoals: List<SavingGoal>,
    onDismiss: () -> Unit,
    onConfirm: (goalId: String, amount: Long, note: String?, isDirect: Boolean) -> Unit
) {
    var goal by remember { mutableStateOf(selectedGoal ?: availableGoals.firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isDirect by remember { mutableStateOf(false) }
    var showGoalPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.contribute_goal_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color(0xFFFFF0F0), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close), tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Goal Selector (if not pre-selected or to change)
            Text(stringResource(R.string.contribute_select_goal), fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { if (availableGoals.size > 1) showGoalPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF0F0).copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFFFF8A80))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = goal?.name ?: stringResource(R.string.contribute_select_goal_placeholder),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (availableGoals.size > 1) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contribution Type
            Text(stringResource(R.string.contribute_payment_method), fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentMethodCard(
                    title = stringResource(R.string.contribute_from_wallet),
                    icon = Icons.Default.AccountBalanceWallet,
                    isSelected = !isDirect,
                    modifier = Modifier.weight(1f),
                    onClick = { isDirect = false }
                )
                PaymentMethodCard(
                    title = stringResource(R.string.contribute_direct_pay),
                    icon = Icons.Default.Payments,
                    isSelected = isDirect,
                    modifier = Modifier.weight(1f),
                    onClick = { isDirect = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Input
            Text(stringResource(R.string.contribute_amount_label), fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                prefix = { Text("$ ", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFFFF0F0).copy(alpha = 0.3f),
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFFF8A80)
                ),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Note
            Text(stringResource(R.string.contribute_note_label), fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Trích quỹ lương, Thưởng thêm...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFFFF0F0).copy(alpha = 0.3f),
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFFF8A80)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = { 
                    goal?.let { g ->
                        if (amount.isNotBlank()) {
                            onConfirm(g.id, amount.toLong(), note.ifBlank { null }, isDirect)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A80)),
                shape = RoundedCornerShape(28.dp),
                enabled = goal != null && amount.isNotBlank()
            ) {
                Text(stringResource(R.string.contribute_confirm), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showGoalPicker) {
            GoalPickerSheet(
                goals = availableGoals,
                onGoalSelected = { goal = it; showGoalPicker = false },
                onDismiss = { showGoalPicker = false }
            )
        }
    }
}

@Composable
fun PaymentMethodCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFFFF8A80) else Color(0xFFFFF0F0),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalPickerSheet(
    goals: List<SavingGoal>,
    onGoalSelected: (SavingGoal) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(stringResource(R.string.contribute_select_goal_placeholder), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            goals.forEach { goal ->
                Surface(
                    onClick = { onGoalSelected(goal) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF0F0)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(goal.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(formatSimpleAmount(goal.currentAmount), color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
