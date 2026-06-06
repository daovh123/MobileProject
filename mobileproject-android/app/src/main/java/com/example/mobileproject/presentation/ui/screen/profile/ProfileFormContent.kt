package com.example.mobileproject.presentation.ui.screen.profile

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import java.time.Instant
import java.time.ZoneId

/**
 * Form nội dung chỉnh sửa profile – component tái sử dụng chứa các trường nhập liệu.
 *
 * Trường nhập:
 * - Họ và tên (fullName): OutlinedTextField singleLine.
 * - Biệt danh (nickName): OutlinedTextField singleLine.
 * - Ngày sinh (birthDate): OutlinedTextField readOnly, chạm để mở DatePickerDialog.
 *   Sử dụng pointerInput + awaitEachGesture để bắt sự kiện chạm trên field readOnly.
 * - Giới tính (gender): hàng 3 FilterChip (MALE/FEMALE/OTHER).
 * - Nút Lưu: enabled khi saveEnabled && !isSaving, hiển thị CircularProgressIndicator
 *   khi đang lưu.
 *
 * DatePicker integration:
 * - [DatePickerDialog] Material3, convert millis → LocalDate → String (yyyy-MM-dd).
 *
 * @param fullName họ tên hiện tại.
 * @param nickName biệt danh hiện tại.
 * @param birthDate ngày sinh dạng "yyyy-MM-dd".
 * @param gender giới tính: "MALE"/"FEMALE"/"OTHER".
 * @param isSaving trạng thái đang lưu.
 * @param onFullNameChange callback khi thay đổi họ tên.
 * @param onNickNameChange callback khi thay đổi biệt danh.
 * @param onBirthDateChange callback khi chọn ngày sinh mới.
 * @param onGenderChange callback khi chọn giới tính.
 * @param onSave callback khi nhấn nút Lưu.
 * @param saveEnabled có cho phép lưu hay không.
 * @param showSaveButton có hiển thị nút Lưu hay không.
 */
@Composable
fun ProfileFormContent(
    fullName: String,
    nickName: String,
    birthDate: String,
    gender: String,
    isSaving: Boolean,
    onFullNameChange: (String) -> Unit,
    onNickNameChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabled: Boolean = true,
    showSaveButton: Boolean = true,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.90f),
        unfocusedContainerColor = colorScheme.surface.copy(alpha = 0.95f),
        focusedBorderColor = colorScheme.primary,
        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.28f),
        focusedLabelColor = colorScheme.primary,
        unfocusedLabelColor = colorScheme.onSurfaceVariant,
        cursorColor = colorScheme.primary,
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = { Text(text = stringResource(R.string.profile_fullname)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
            shape = MaterialTheme.shapes.large,
        )

        OutlinedTextField(
            value = nickName,
            onValueChange = onNickNameChange,
            label = { Text(text = stringResource(R.string.profile_nickname)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
            shape = MaterialTheme.shapes.large,
        )

        OutlinedTextField(
            value = birthDate,
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            label = { Text(text = stringResource(R.string.profile_birthdate)) },
            placeholder = { Text(text = stringResource(R.string.profile_select_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        if (waitForUpOrCancellation(pass = PointerEventPass.Initial) != null) {
                            showDatePicker = true
                        }
                    }
                },
            colors = fieldColors,
            shape = MaterialTheme.shapes.large,
        )

        Text(
            text = stringResource(R.string.profile_gender),
            style = MaterialTheme.typography.titleSmall,
            color = colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = gender == "MALE",
                onClick = { onGenderChange("MALE") },
                label = { Text(text = stringResource(R.string.profile_gender_male)) },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
            )
            FilterChip(
                selected = gender == "FEMALE",
                onClick = { onGenderChange("FEMALE") },
                label = { Text(text = stringResource(R.string.profile_gender_female)) },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
            )
            FilterChip(
                selected = gender == "OTHER",
                onClick = { onGenderChange("OTHER") },
                label = { Text(text = stringResource(R.string.profile_gender_other)) },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
            )
        }

        if (showSaveButton) {
            Button(
                onClick = onSave,
                enabled = saveEnabled && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                ),
                shape = MaterialTheme.shapes.large,
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(4.dp),
                        color = colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.profile_save),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onBirthDateChange(selectedDate.toString())
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
