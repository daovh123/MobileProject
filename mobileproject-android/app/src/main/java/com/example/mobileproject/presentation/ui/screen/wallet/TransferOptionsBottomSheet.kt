/**
 * # TransferOptionsBottomSheet - Bottom sheet chọn phương thức chuyển tiền
 *
 * Hiển thị 2 tùy chọn chuyển tiền:
 * - **Chuyển khoản thủ công**: mở màn hình TransferMoneyScreen
 * - **Quét QR**: mở màn hình QRScannerScreen
 *
 * Sử dụng [ModalBottomSheet] của Material3.
 *
 * ## Navigation triggers
 * - onDismiss: đóng bottom sheet
 * - onManualTransfer: chuyển đến màn hình nhập thông tin thủ công
 * - onScanQr: chuyển đến màn hình quét QR
 */
package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Bottom sheet cho phép chọn phương thức chuyển tiền: thủ công hoặc quét QR.
 *
 * @param onDismiss đóng bottom sheet
 * @param onManualTransfer chọn chuyển khoản thủ công
 * @param onScanQr chọn quét QR
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferOptionsBottomSheet(
    onDismiss: () -> Unit,
    onManualTransfer: () -> Unit,
    onScanQr: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Chuyển tiền",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Chọn cách chuyển tiền phù hợp.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = onManualTransfer,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Text("Chuyển khoản thủ công", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onScanQr,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Text("Quét QR", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
