package com.example.mobileproject.presentation.ui.component.wallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BalanceSection(
    balance: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "TOTAL BALANCE",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Text(
            text = formatCurrency(balance),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 40.sp
            ),
            color = Color(0xFF2D2D2D)
        )
    }
}

private fun formatCurrency(amount: Long): String {
    // Sửa Warning: Dùng Locale.getDefault() để tránh deprecated constructor
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return formatter.format(amount)
}