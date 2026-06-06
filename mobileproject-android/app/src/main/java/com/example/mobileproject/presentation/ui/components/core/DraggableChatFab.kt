/**
 * FloatingActionButton có thể kéo thả cho tính năng chat nhanh.
 *
 * Cung cấp [DraggableChatFab] composable với vị trí lưu trữ persist qua
 * [rememberSaveable], hỗ trợ kéo tự do trong giới hạn parent container.
 */
package com.example.mobileproject.presentation.ui.components.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R

/**
 * FloatingActionButton có thể kéo thả tự do trên màn hình.
 *
 * Hiển thị một nút chat hình tròn có thể kéo đến bất kỳ vị trí nào
 * trong giới hạn màn hình. Vị trí được lưu vào [rememberSaveable]
 * để giữ nguyên khi xoay màn hình hoặc cấu hình thay đổi.
 *
 * Nút có shadow 12dp, border nhẹ và sử dụng màu primaryContainer.
 * Nhãn hiển thị từ resource `R.string.chat_fab_label`.
 *
 * @param onClick Callback khi nhấn vào nút FAB.
 * @param modifier [Modifier] tùy chỉnh, thường là `fillMaxSize()` để xác định vùng kéo.
 */
@Composable
fun DraggableChatFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val colorScheme = MaterialTheme.colorScheme
        val density = LocalDensity.current
        val buttonSize = 56.dp
        val buttonSizePx = with(density) { buttonSize.toPx() }
        val marginPx = with(density) { 18.dp.toPx() }

        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val maxX = (maxWidthPx - buttonSizePx).coerceAtLeast(0f)
        val maxY = (maxHeightPx - buttonSizePx).coerceAtLeast(0f)

        val initialX = (maxX - marginPx).coerceAtLeast(0f)
        val initialY = (maxY - marginPx).coerceAtLeast(0f)

        var offsetX by rememberSaveable { mutableStateOf(Float.NaN) }
        var offsetY by rememberSaveable { mutableStateOf(Float.NaN) }

        LaunchedEffect(maxX, maxY, initialX, initialY) {
            if (offsetX.isNaN() || offsetY.isNaN()) {
                offsetX = initialX
                offsetY = initialY
            } else {
                offsetX = offsetX.coerceIn(0f, maxX)
                offsetY = offsetY.coerceIn(0f, maxY)
            }
        }

        Surface(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                .size(buttonSize)
                .pointerInput(maxX, maxY) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY)
                    }
                }
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = colorScheme.primaryContainer,
            border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.18f)),
            shadowElevation = 12.dp,
            tonalElevation = 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.chat_fab_label),
                    color = colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
