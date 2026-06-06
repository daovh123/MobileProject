/**
 * Bộ UI components hiện đại dùng chung cho toàn bộ ứng dụng.
 *
 * Bao gồm các composable đa năng:
 * - [ModernGradientCard]: Card gradient cho hero sections.
 * - [AIInsightChip]: Chip gợi ý AI.
 * - [SettingGroupCard] / [SettingRow]: Nhóm cài đặt.
 * - [SquircleAvatar]: Avatar bo góc squircle.
 * - [SpendingDonutChart]: Biểu đồ donut chi tiêu.
 * - [RecentActivityItem]: Hàng giao dịch gần đây.
 * - [DiscoveryImageCard]: Card khám phá địa điểm.
 * - [EmptyChatSuggestions]: Gợi ý chat trống.
 * - [MomentCard]: Card kỷ niệm.
 * - [pinkRippleClickable]: Modifier extension ripple hồng.
 */
package com.example.mobileproject.presentation.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mobileproject.R

/**
 * Card gradient dùng chung cho các section nổi bật.
 *
 * Render một [Surface] với bo góc 32dp, shadow 8dp,
 * và background gradient tùy chỉnh. Thường dùng cho
 * hero sections hoặc các card nổi bật trên màn hình chính.
 *
 * @param modifier [Modifier] tùy chỉnh.
 * @param brush [Brush] gradient dùng làm background.
 * @param content Nội dung composable bên trong card.
 */
@Composable
fun ModernGradientCard(modifier: Modifier = Modifier, brush: Brush, content: @Composable () -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(32.dp), color = Color.Transparent, shadowElevation = 8.dp) {
        Box(modifier = Modifier.background(brush).padding(24.dp)) { content() }
    }
}

/**
 * Chip hiển thị gợi ý hoặc mẹo từ AI.
 *
 * Render một chip nhỏ với icon ✨ (AutoAwesome) và văn bản gợi ý,
 * sử dụng màu secondary với alpha nhẹ để tạo cảm giác nhẹ nhàng.
 *
 * @param text Nội dung gợi ý cần hiển thị.
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
fun AIInsightChip(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/**
 * Card nhóm cài đặt với bo góc lớn và shadow nhẹ.
 *
 * Dùng làm container cho danh sách các [SettingRow],
 * tạo cảm giác nhóm các mục cài đặt liên quan.
 *
 * @param modifier [Modifier] tùy chỉnh.
 * @param content Nội dung composable (thường là danh sách [SettingRow]).
 */
@Composable
fun SettingGroupCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) { content() }
    }
}

/**
 * Hàng cài đặt với icon, tiêu đề, phụ đề và widget tùy chỉnh bên phải.
 *
 * Mỗi hàng có icon trong container bo góc, tiêu đề và phụ đề optional.
 * Hỗ trợ click với hiệu ứng ripple hồng đặc trưng qua [pinkRippleClickable].
 *
 * @param icon [ImageVector] hiển thị bên trái.
 * @param title Tiêu đề chính của mục cài đặt.
 * @param subtitle Phụ đề optional hiển thị dưới tiêu đề.
 * @param trailing Composable optional bên phải (ví dụ: Switch, Text).
 * @param modifier [Modifier] tùy chỉnh.
 * @param onClick Callback khi nhấn vào hàng. Nếu null, hàng không thể click.
 */
@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = if (onClick != null) modifier.pinkRippleClickable(onClick) else modifier
    Row(modifier = rowModifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            if (!subtitle.isNullOrBlank()) Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
    }
}

/**
 * Avatar hình vuông bo góc (squircle) hỗ trợ ảnh từ URL hoặc Bitmap.
 *
 * Hiển thị ảnh người dùng với bo góc 30dp (squircle shape).
 * Nếu không có ảnh, hiển thị chữ cái đầu làm fallback.
 * Sử dụng Coil để load ảnh bất đồng bộ với crossfade.
 *
 * @param imageModel Ảnh nguồn: có thể là URL (String), Bitmap, hoặc null.
 * @param fallbackText Văn bản hiển thị khi không có ảnh (thường là chữ cái đầu tên).
 * @param modifier [Modifier] tùy chỉnh.
 * @param size Kích thước avatar tính bằng dp (mặc định 96).
 */
@Composable
fun SquircleAvatar(imageModel: Any?, fallbackText: String, modifier: Modifier = Modifier, size: Int = 96) {
    Box(modifier = modifier.size(size.dp).clip(RoundedCornerShape(30.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
        if (imageModel != null) {
            AsyncImage(
                model = if (imageModel is Bitmap) ImageRequest.Builder(LocalContext.current).data(imageModel).crossfade(true).build() else imageModel,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(text = fallbackText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * Biểu đồ donut (ring chart) hiển thị phân bổ chi tiêu theo danh mục.
 *
 * Render biểu đồ donut bên trái và danh sách chú thích (legend) bên phải.
 * Hỗ trợ chọn danh mục: khi chọn, vòng cung tương ứng dày hơn và
 * tên danh mục hiển thị đậm hơn trong legend.
 *
 * @param items Danh sách cặp (tên danh mục, tỷ lệ 0.0-1.0).
 * @param colors Danh sách màu tương ứng cho từng danh mục.
 * @param modifier [Modifier] tùy chỉnh.
 * @param selectedIndex Chỉ số danh mục đang được chọn (-1 nếu không chọn).
 * @param onSelect Callback khi chọn/bỏ chọn một danh mục.
 */
@Composable
fun SpendingDonutChart(
    items: List<Pair<String, Float>>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(160.dp)) {
            var start = -90f
            items.forEachIndexed { index, item ->
                val sweep = (item.second * 360f).coerceAtLeast(1f)
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = if (index == selectedIndex) 40f else 32f, cap = StrokeCap.Round),
                )
                start += sweep
            }
        }
        LazyColumn(modifier = Modifier.height(160.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(items) { index, item ->
                Row(modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onSelect(if (selectedIndex == index) -1 else index) }.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(colors[index % colors.size], CircleShape))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = item.first, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/**
 * Hàng hiển thị một hoạt động giao dịch gần đây.
 *
 * Render icon trong container tròn, tiêu đề, phụ đề và số tiền.
 * Số tiền có màu đỏ nếu chi tiêu, xanh nếu thu nhập.
 *
 * @param icon [ImageVector] của danh mục giao dịch.
 * @param iconTint Màu của icon.
 * @param title Tiêu đề giao dịch (tên danh mục hoặc mục tiêu).
 * @param subtitle Phụ đề (thường là ngày tháng).
 * @param amountText Số tiền đã định dạng (bao gồm dấu +/-).
 * @param isNegative true nếu là chi tiêu (màu đỏ), false nếu thu nhập (màu xanh).
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
fun RecentActivityItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    amountText: String,
    isNegative: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(iconTint.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = amountText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = if (isNegative) Color(0xFFE53935) else Color(0xFF4CAF50))
    }
}

/**
 * Card khám phá địa điểm với ảnh, đánh giá và overlay gradient.
 *
 * Render ảnh địa điểm với gradient tối phía dưới, badge đánh giá
 * ở góc trên phải, và tên cùng phụ đề ở dưới cùng.
 *
 * @param imageUrl URL ảnh địa điểm.
 * @param title Tên địa điểm.
 * @param subtitle Phụ đề (địa chỉ, mô tả ngắn).
 * @param ratingText Chuỗi đánh giá (ví dụ: "4.5 (120)").
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
fun DiscoveryImageCard(
    imageUrl: String?,
    title: String,
    subtitle: String,
    ratingText: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(24.dp), shadowElevation = 6.dp) {
        Box(modifier = Modifier.height(240.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
            Surface(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp), shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.9f)) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = ratingText, style = MaterialTheme.typography.labelMedium)
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

/**
 * Giao diện gợi ý khi cuộc trò chuyện trống.
 *
 * Hiển thị icon ✨ và danh sách các gợi ý dạng chip có thể click.
 * Khi nhấn vào một gợi ý, callback [onSuggestionClick] được gọi
 * với nội dung gợi ý tương ứng.
 *
 * @param suggestions Danh sách chuỗi gợi ý.
 * @param onSuggestionClick Callback khi chọn một gợi ý.
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
fun EmptyChatSuggestions(suggestions: List<String>, onSuggestionClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(34.dp))
        suggestions.forEach { text ->
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), modifier = Modifier.clickable { onSuggestionClick(text) }) {
                Text(text = text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
            }
        }
    }
}

/**
 * Card hiển thị một kỷ niệm (moment) với ảnh, tiêu đề và tùy chọn ghim.
 *
 * Render ảnh kỷ niệm, badge ghim (pin) ở góc trên phải, tiêu đề,
 * ngày tháng và footer optional. Hỗ trợ toggle ghim/bỏ ghim.
 *
 * @param title Tiêu đề kỷ niệm.
 * @param dateText Chuỗi ngày tháng đã định dạng.
 * @param imageModel Ảnh nguồn (URL, Bitmap, hoặc resource).
 * @param pinned true nếu kỷ niệm đang được ghim.
 * @param onPinToggle Callback khi nhấn vào nút ghim/bỏ ghim.
 * @param modifier [Modifier] tùy chỉnh.
 * @param footer Composable optional hiển thị dưới ngày tháng.
 */
@Composable
fun MomentCard(
    title: String,
    dateText: String,
    imageModel: Any?,
    pinned: Boolean,
    onPinToggle: () -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(24.dp), shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
        Column {
            Box(modifier = Modifier.height(220.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(imageModel).crossfade(true).build(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).clickable(onClick = onPinToggle),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PushPin, contentDescription = null, tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        if (pinned) {
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(text = stringResource(R.string.moment_pinned), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = dateText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                footer?.invoke()
            }
        }
    }
}

/**
 * Modifier extension thêm hiệu ứng ripple hồng (#E94057) khi click.
 *
 * Sử dụng [composed] để tạo [MutableInteractionSource] riêng biệt
 * cho mỗi composable, đảm bảo hiệu ứng ripple không bị chia sẻ.
 *
 * @param onClick Callback khi click.
 * @return [Modifier] với hiệu ứng ripple hồng.
 */
fun Modifier.pinkRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = Color(0xFFE94057).copy(alpha = 0.12f)),
        onClick = onClick,
    )
}
