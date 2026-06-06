/**
 * Component hiển thị số dư ví tình yêu.
 *
 * Cung cấp [BalanceSection] composable với gradient background hồng ấm
 * và [formatSimpleAmount] utility để định dạng số tiền.
 */
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.presentation.ui.theme.AppTheme
import java.text.DecimalFormat

/**
 * Hiển thị số dư ví tình yêu với gradient background đặc trưng.
 *
 * Component này render một [ElevatedCard] chứa gradient hồng ấm
 * (từ [ExtendedColorScheme.balanceCardGradient]), hiển thị tiêu đề
 * "VÍ TÌNH YÊU" và số dư hiện tại được định dạng theo kiểu Việt Nam
 * (phân cách bằng dấu phẩy).
 *
 * @param balance Số dư hiện tại của ví, đơn vị là VND.
 * @param modifier [Modifier] tùy chỉnh cho card bên ngoài.
 */
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
                Text(
                    text = formatSimpleAmount(balance),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 40.sp,
                    ),
                    color = Color.White,
                )
            }
        }
    }
}

/**
 * Định dạng số tiền thành chuỗi dễ đọc với dấu phân cách hàng nghìn.
 *
 * Ví dụ: 1000000L -> "1,000,000"
 *
 * @param amount Số tiền cần định dạng.
 * @return Chuỗi đã định dạng.
 */
fun formatSimpleAmount(amount: Long): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(amount)
}

