/**
 * Thanh điều hướng dưới cùng (bottom navigation bar) tùy chỉnh.
 *
 * Cung cấp [AppNavigationBar] composable với indicator thanh ngang,
 * animation màu mượt mà, và layout responsive theo số lượng tab.
 */
package com.example.mobileproject.presentation.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Thanh điều hướng dưới cùng (bottom navigation bar) của ứng dụng.
 *
 * Render các tab từ [NavigationConfig.navigationItems] với:
 * - Indicator thanh ngang (32dp) phía trên tab đang chọn, màu primary.
 * - Icon và nhãn với animateColorAsState cho hiệu ứng chuyển mượt.
 * - Padding navigation bar tự động (navigationBarsPadding).
 *
 * @param currentRoute Route hiện tại để xác định tab đang chọn.
 * @param onNavigate Callback khi chọn một tab, truyền route tương ứng.
 * @param modifier [Modifier] tùy chỉnh.
 * @param items Danh sách [NavigationItem], mặc định từ [NavigationConfig].
 */
@Composable
fun AppNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavigationItem> = NavigationConfig.navigationItems,
) {
    if (items.isEmpty()) return

    val colorScheme = MaterialTheme.colorScheme
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.let { if (it < 0) 0 else it }
    val indicatorColor = colorScheme.primary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 4.dp),
        ) {
            val itemWidth = maxWidth / items.size
            val indicatorOffset = itemWidth * selectedIndex + (itemWidth - 32.dp) / 2

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = indicatorOffset, y = 5.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .background(indicatorColor, RoundedCornerShape(999.dp)),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItemContent(
                        item = item,
                        isSelected = index == selectedIndex,
                        selectedColor = indicatorColor,
                        onNavigate = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Nội dung của một tab trong thanh điều hướng.
 *
 * Hiển thị icon và nhãn dọc, với màu animate giữa selected/unselected.
 * Nhãn sử dụng SemiBold khi được chọn, Normal khi không chọn.
 *
 * @param item [NavigationItem] cần hiển thị.
 * @param isSelected true nếu tab đang được chọn.
 * @param selectedColor Màu khi tab được chọn.
 * @param onNavigate Callback khi nhấn vào tab.
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
private fun NavigationBarItemContent(
    item: NavigationItem,
    isSelected: Boolean,
    selectedColor: Color,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
    )

    Column(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onNavigate),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.contentDescriptionRes),
            tint = iconColor,
            modifier = Modifier
                .padding(top = 6.dp)
                .size(24.dp),
        )

        Text(
            text = stringResource(item.labelRes),
            color = iconColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
