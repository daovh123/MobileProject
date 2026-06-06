package com.example.mobileproject.presentation.ui.screen.add_expense.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Ô nhập số tiền – hiển thị số tiền lớn ở giữa với font size tự động co giãn.
 *
 * Cấu trúc UI:
 * - Card bo góc 32dp, nền hồng nhạt (FFE4E1 alpha 0.5).
 * - Label "How much did you spend?" ở trên.
 * - BasicTextField với keyboardType Decimal, textAlign Center.
 * - Font size tự động giảm khi số dài: 64sp (≤6 digits) → 56sp → 44sp → 32sp (>12 digits).
 * - Placeholder "0.00" màu hồng nhạt khi trống.
 *
 * @param amount chuỗi số tiền hiện tại.
 * @param onAmountChange callback khi thay đổi số tiền.
 */
@Composable
fun AmountInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color(0xFFFFE4E1).copy(alpha = 0.5f)
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 32.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How much did you spend?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val fontSize = when {
                amount.length > 12 -> 32.sp
                amount.length > 9 -> 44.sp
                amount.length > 6 -> 56.sp
                else -> 64.sp
            }

            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                textStyle = TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8A80),
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (amount.isEmpty()) {
                            Text(
                                text = "0.00",
                                style = TextStyle(
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8A80).copy(alpha = 0.3f),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
