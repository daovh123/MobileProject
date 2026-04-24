package com.example.mobileproject.presentation.ui.screen.profile

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
        )

        OutlinedTextField(
            value = nickName,
            onValueChange = onNickNameChange,
            label = { Text(text = stringResource(R.string.profile_nickname)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
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
        )

        Text(
            text = stringResource(R.string.profile_gender),
            style = MaterialTheme.typography.titleSmall,
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
            )
            FilterChip(
                selected = gender == "FEMALE",
                onClick = { onGenderChange("FEMALE") },
                label = { Text(text = stringResource(R.string.profile_gender_female)) },
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = gender == "OTHER",
                onClick = { onGenderChange("OTHER") },
                label = { Text(text = stringResource(R.string.profile_gender_other)) },
                modifier = Modifier.weight(1f),
            )
        }

        if (showSaveButton) {
            Button(
                onClick = onSave,
                enabled = saveEnabled && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(4.dp),
                    )
                } else {
                    Text(text = stringResource(R.string.profile_save))
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
