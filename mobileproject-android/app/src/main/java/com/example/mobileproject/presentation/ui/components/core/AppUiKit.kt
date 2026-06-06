/**
 * UI Kit cốt lõi (core) của ứng dụng.
 *
 * Cung cấp các composable基础 dùng chung trên toàn app:
 * - [AppScreenBackground]: Background gradient chuẩn với decorative blobs.
 * - [AppSurfaceCard]: Card surface tiêu chuẩn với border và shadow.
 * - [AppSectionHeader]: Header tiêu đề section.
 * - [AppPrimaryButton]: Nút primary tiêu chuẩn.
 * - [AppFormTextField]: TextField cho biểu mẫu.
 * - [AppStateMessage]: Thông báo trạng thái.
 * - [AppMetricChip]: Chip hiển thị chỉ số.
 * - [AppEmptyState]: Trạng thái rỗng.
 */
package com.example.mobileproject.presentation.ui.components.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Background gradient chuẩn cho toàn bộ màn hình trong ứng dụng.
 *
 * Render gradient dọc từ surface -> surfaceContainerLow -> surfaceContainer,
 * kết hợp 2 hình tròn trang trí (radial gradient) ở góc trên phải và góc
 * dưới trái để tạo chiều sâu và cảm giác mềm mại.
 *
 * @param modifier [Modifier] tùy chỉnh.
 * @param content Nội dung composable hiển thị bên trên background.
 */
@Composable
fun AppScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val gradient = remember(colorScheme) {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.surface,
                colorScheme.surfaceContainerLow,
                colorScheme.surfaceContainer.copy(alpha = 0.85f),
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 98.dp, y = (-122).dp)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = 0.10f),
                            Color.Transparent,
                        ),
                    ),
                    shape = RoundedCornerShape(999.dp),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-110).dp, y = 130.dp)
                .size(320.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorScheme.tertiary.copy(alpha = 0.07f),
                            Color.Transparent,
                        ),
                    ),
                    shape = RoundedCornerShape(999.dp),
                ),
        )

        content()
    }
}

/**
 * Card surface tiêu chuẩn với border nhẹ và shadow.
 *
 * Dùng làm container chung cho các section nội dung,
 * đảm bảo tính nhất quán về bo góc, shadow và border trên toàn app.
 *
 * @param modifier [Modifier] tùy chỉnh.
 * @param content Nội dung composable bên trong card.
 */
@Composable
fun AppSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colorScheme.surfaceContainerLowest.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.12f)),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        content()
    }
}

/**
 * Header tiêu đề cho các section nội dung.
 *
 * Hiển thị tiêu đề lớn (titleLarge, Bold) và phụ đề optional (bodyMedium)
 * bên dưới. Thường dùng kết hợp với [AppSurfaceCard].
 *
 * @param title Tiêu đề chính của section.
 * @param subtitle Phụ đề optional.
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
fun AppSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Nút primary tiêu chuẩn của ứng dụng.
 *
 * Sử dụng màu primary từ MaterialTheme, bo góc large, và
 * fontWeight Bold cho label. Hỗ trợ trạng thái disabled với
 * màu surfaceContainerHigh.
 *
 * @param text Văn bản hiển thị trên nút.
 * @param modifier [Modifier] tùy chỉnh.
 * @param enabled Trạng thái bật/tắt của nút.
 * @param onClick Callback khi nhấn nút.
 */
@Composable
fun AppPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * TextFieldOutlined tiêu chuẩn cho biểu mẫu (form).
 *
 * Sử dụng OutlinedTextField của Material 3 với màu sắc tùy chỉnh:
 * - Container: surfaceContainerLow (focused) / surfaceContainerLowest (unfocused).
 * - Border: primary (focused) / outlineVariant nhẹ (unfocused).
 * Hỗ trợ leading/trailing icon, placeholder, supporting text và keyboard options.
 *
 * @param value Giá trị hiện tại của trường nhập.
 * @param onValueChange Callback khi giá trị thay đổi.
 * @param label Nhãn hiển thị (label).
 * @param modifier [Modifier] tùy chỉnh.
 * @param readOnly true nếu trường chỉ đọc.
 * @param singleLine true nếu trường nhập một dòng.
 * @param placeholder Composable placeholder optional.
 * @param trailing Composable trailing icon optional.
 * @param leading Composable leading icon optional.
 * @param supporting Composable supporting text optional.
 * @param keyboardOptions Tùy chọn bàn phím.
 * @param keyboardActions Hành động bàn phím.
 */
@Composable
fun AppFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    placeholder: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    supporting: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colorScheme = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        readOnly = readOnly,
        singleLine = singleLine,
        label = { Text(label) },
        placeholder = placeholder,
        leadingIcon = leading,
        trailingIcon = trailing,
        supportingText = supporting,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
            unfocusedContainerColor = colorScheme.surfaceContainerLowest.copy(alpha = 0.90f),
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outlineVariant.copy(alpha = 0.30f),
            focusedLabelColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurfaceVariant,
            cursorColor = colorScheme.primary,
        ),
    )
}

/**
 * Hiển thị thông báo trạng thái với tiêu đề, nội dung và nút hành động optional.
 *
 * Dùng cho các trạng thái như lỗi, loading, hoặc thông báo hệ thống.
 * Sử dụng [AppSurfaceCard] làm container.
 *
 * @param title Tiêu đề thông báo.
 * @param message Nội dung chi tiết.
 * @param modifier [Modifier] tùy chỉnh.
 * @param actionText Văn bản nút hành động optional.
 * @param onAction Callback khi nhấn nút hành động.
 */
@Composable
fun AppStateMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    AppSurfaceCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!actionText.isNullOrBlank() && onAction != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    AppPrimaryButton(
                        text = actionText,
                        onClick = onAction,
                    )
                }
            }
        }
    }
}

/**
 * Chip hiển thị chỉ số (metric) với giá trị và nhãn.
 *
 * Sử dụng container secondaryContainer với border nhẹ,
 * hiển thị giá trị lớn (titleMedium, Bold) và nhãn (labelMedium) bên dưới.
 *
 * @param value Giá trị số hoặc chuỗi cần hiển thị.
 * @param label Nhãn mô tả giá trị.
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
fun AppMetricChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = colorScheme.secondaryContainer.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSecondaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSecondaryContainer.copy(alpha = 0.78f),
            )
        }
    }
}

/**
 * Hiển thị trạng thái rỗng (empty state) với tiêu đề và phụ đề.
 *
 * Dùng khi danh sách không có dữ liệu hoặc màn hình chưa có nội dung.
 * Sử dụng [Surface] với border nhẹ và bo góc large.
 *
 * @param title Tiêu đề thông báo trạng thái rỗng.
 * @param subtitle Phụ đề mô tả chi tiết.
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
fun AppEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
