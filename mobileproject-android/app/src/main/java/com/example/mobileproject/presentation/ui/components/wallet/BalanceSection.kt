package com.example.mobileproject.presentation.ui.components.wallet

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.presentation.ui.components.core.AutoShrinkSingleLineText
import com.example.mobileproject.presentation.ui.theme.AppTheme
import java.text.DecimalFormat

@Composable
fun BalanceSection(
    balance: Long,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = AppTheme.extendedColors

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
                AutoShrinkSingleLineText(
                    text = formatSimpleAmount(balance),
                    maxFontSize = 40.sp,
                    minFontSize = 20.sp,
                    stepGranularity = 1.sp,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

fun formatSimpleAmount(amount: Long): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(amount)
}

