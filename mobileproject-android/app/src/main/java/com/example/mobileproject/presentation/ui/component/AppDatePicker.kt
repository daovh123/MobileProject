package com.example.mobileproject.presentation.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.mobileproject.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePicker(
    onDateSelected: (String) -> Unit, // Returns ISO 8601 date string
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Format for backend: 2026-12-31T00:00:00Z
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        val dateString = sdf.format(Date(millis))
                        onDateSelected(dateString)
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

object DateUtils {
    private val displayFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val backendParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val simpleBackendParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatToDisplay(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "Chọn ngày"
        return try {
            val date = if (dateString.contains("T")) {
                backendParser.parse(dateString)
            } else {
                simpleBackendParser.parse(dateString)
            }
            if (date != null) displayFormatter.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }
}

