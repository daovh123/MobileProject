package com.example.mobileproject.presentation.ui.screen.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.GoalTask
import com.example.mobileproject.domain.model.ExpenseCategory
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalBottomSheet(
    type: String, // "SAVING" or "FUTURE"
    onDismiss: () -> Unit,
    onConfirmSaving: (String, Long, String, String?) -> Unit = { _, _, _, _ -> },
    onConfirmFuture: (String, String, String?, List<GoalTask>) -> Unit = { _, _, _, _ -> },
) {
    val colorScheme = MaterialTheme.colorScheme
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.getAll().first().id) }
    var deadline by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(listOf("")) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (type == "SAVING") {
                    stringResource(R.string.goal_new_saving_title)
                } else {
                    stringResource(R.string.goal_new_future_title)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.goal_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )

            Text(
                stringResource(R.string.goal_category_label),
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant,
            )
            SecondaryScrollableTabRow(
                selectedTabIndex = ExpenseCategory.getAll().indexOfFirst { it.id == category }.coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = colorScheme.surface,
                divider = {},
            ) {
                ExpenseCategory.getAll().forEach { cat ->
                    val selected = cat.id == category
                    Tab(
                        selected = selected,
                        onClick = { category = cat.id },
                        text = {
                            Text(
                                text = cat.displayName,
                                color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            if (type == "SAVING") {
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) targetAmount = it },
                    label = { Text(stringResource(R.string.goal_target_amount_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    prefix = { Text("$ ") },
                )
            } else {
                Text(
                    stringResource(R.string.goal_tasks_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurfaceVariant,
                )
                tasks.forEachIndexed { index, taskContent ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = taskContent,
                            onValueChange = { newContent ->
                                tasks = tasks.toMutableList().apply { this[index] = newContent }
                            },
                            label = { Text(stringResource(R.string.goal_task_label_format, index + 1)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                        )
                        IconButton(
                            onClick = {
                                if (tasks.size > 1) {
                                    tasks = tasks.toMutableList().apply { removeAt(index) }
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                TextButton(onClick = { tasks = tasks + "" }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.goal_add_task))
                }
            }

            OutlinedTextField(
                value = deadline,
                onValueChange = { deadline = it },
                label = { Text(stringResource(R.string.goal_deadline_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text(stringResource(R.string.goal_optional_placeholder)) },
            )

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (type == "SAVING") {
                            onConfirmSaving(name, targetAmount.toLongOrNull() ?: 0L, category, deadline.ifBlank { null })
                        } else {
                            val goalTasks = tasks.filter { it.isNotBlank() }.map {
                                GoalTask(
                                    taskId = UUID.randomUUID().toString(),
                                    content = it,
                                    isCompleted = false,
                                )
                            }
                            onConfirmFuture(name, category, deadline.ifBlank { null }, goalTasks)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(stringResource(R.string.goal_confirm), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
