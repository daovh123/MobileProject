package com.example.mobileproject.presentation.ui.screen.add_expense.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseFormFields(
    note: String,
    onNoteChange: (String) -> Unit,
    date: Long,
    onDateChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = date }
    val dateFormatter = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.expense_note_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.expense_note_placeholder), color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorScheme.surfaceContainerLowest,
                    focusedContainerColor = colorScheme.surfaceContainerLowest,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    focusedBorderColor = colorScheme.primary
                ),
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = colorScheme.primary) }
            )
        }

        Column {
            Text(
                text = stringResource(R.string.expense_date_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCal = Calendar.getInstance()
                                selectedCal.set(year, month, dayOfMonth)
                                onDateChange(selectedCal.timeInMillis)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                shape = RoundedCornerShape(24.dp),
                color = colorScheme.surfaceContainerLowest,
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isToday(date)) stringResource(R.string.expense_date_today) else dateFormatter.format(Date(date)),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun isToday(millis: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
