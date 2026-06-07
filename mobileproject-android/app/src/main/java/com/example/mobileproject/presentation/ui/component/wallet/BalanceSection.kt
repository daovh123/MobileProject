package com.example.mobileproject.presentation.ui.component.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.presentation.ui.theme.AppTheme
import java.text.DecimalFormat

@Composable
fun BalanceSection(
    balance: Long,
    modifier: Modifier = Modifier,
) {
    val extendedColors = AppTheme.extendedColors
    val balanceText = formatSimpleAmount(balance)
    var fontSize by remember(balanceText) {
        mutableStateOf(
            when {
                balanceText.length <= 10 -> 40.sp
                balanceText.length <= 14 -> 34.sp
                balanceText.length <= 18 -> 28.sp
                else -> 24.sp
            }
        )
    }
    val minFontSize = 18.sp

    ElevatedCard(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(extendedColors.balanceCardGradient)
                .padding(vertical = 32.dp, horizontal = 28.dp),
        ) {
            Column {
                Text(
                    text = "VÍ TÌNH YÊU",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.90f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = balanceText,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    onTextLayout = { result ->
                        if (result.hasVisualOverflow && fontSize > minFontSize) {
                            fontSize = (fontSize.value - 2f).sp
                        }
                    },
                )
            }
        }
    }
}

fun formatSimpleAmount(amount: Long): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(amount)
}

