package com.example.mobileproject.presentation.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.mobileproject.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog chọn ngày dùng chung cho toàn bộ ứng dụng.
 *
 * Hiển thị [DatePickerDialog] của Material 3 với nút xác nhận và hủy.
 * Khi người dùng chọn ngày và nhấn OK, ngày được chuyển đổi sang
 * định dạng ISO 8601 (`yyyy-MM-dd'T'HH:mm:ss'Z'`) theo múi giờ UTC
 * và trả về qua callback [onDateSelected].
 *
 * @param onDateSelected Callback trả về chuỗi ngày ISO 8601 khi người dùng xác nhận chọn ngày.
 * @param onDismiss Callback được gọi khi dialog bị đóng (nhấn Hủy hoặc bấm ra ngoài).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePicker(
    onDateSelected: (String) -> Unit,
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

/**
 * Tiện ích định dạng ngày tháng cho giao diện.
 *
 * Chuyển đổi chuỗi ngày từ backend (ISO 8601 hoặc `yyyy-MM-dd`)
 * sang định dạng hiển thị thân thiện với người dùng (`dd MMM yyyy`).
 */
object DateUtils {
    private val displayFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val backendParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val simpleBackendParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Chuyển đổi chuỗi ngày từ backend sang định dạng hiển thị.
     *
     * Hỗ trợ cả định dạng ISO 8601 đầy đủ (`yyyy-MM-dd'T'HH:mm:ss'Z'`)
     * và định dạng rút gọn (`yyyy-MM-dd`). Nếu chuỗi rỗng hoặc null,
     * trả về "Chọn ngày" làm placeholder.
     *
     * @param dateString Chuỗi ngày từ backend cần định dạng lại.
     * @return Chuỗi ngày đã định dạng để hiển thị trên UI, hoặc "Chọn ngày" nếu rỗng.
     */
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

