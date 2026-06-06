/**
 * Màn hình Kỷ niệm (Memories) – hiển thị khoảnh khắc gần đây, lịch ngày đặc biệt,
 * và thông tin nửa kia (partner).
 *
 * Cấu trúc UI chính:
 * - [RecentMomentsPreviewCard]: lưới 2×n ảnh preview khoảnh khắc mới nhất,
 *   nhấp vào mở camera chụp ảnh mới.
 * - [MemoriesCalendarCard]: lưới lịch tháng hiện tại, đánh dấu ngày gần nhất
 *   có sự kiện đặc biệt (Valentine, 8/3, kỷ niệm 100 ngày…).
 * - [PartnerSection]: thẻ hiển thị tên, avatar và số ngày bên nhau của partner.
 * - [SpecialDaysBottomSheet]: bottom sheet danh sách 10 ngày đặc biệt sắp tới.
 *
 * ViewModel: [MemoriesViewModel] (Hilt-injected) – quản lý danh sách moments,
 * thông tin partner, và trạng thái lưu ảnh.
 *
 * Điều hướng:
 * - Nhấn [RecentMomentsPreviewCard] → điều hướng tới [CaptureMomentScreen] (camera).
 * - Nhấn [MemoriesCalendarCard] → mở [SpecialDaysBottomSheet].
 *
 * Layout: Column dọc với spacing 12dp, padding ngang 14dp, toàn bộ nằm trong
 * [AppScreenBackground] – gradient nền hồng nhạt đặc trưng.
 */
package com.example.mobileproject.presentation.ui.screen.memories

import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.mobileproject.R
import com.example.mobileproject.data.model.moment.MomentDto
import com.example.mobileproject.domain.entity.PartnerProfileSummary
import com.example.mobileproject.presentation.ui.components.core.AppScreenBackground
import com.example.mobileproject.presentation.ui.icons.LucideChevronRight
import com.example.mobileproject.presentation.ui.icons.LucideImage
import com.example.mobileproject.presentation.ui.icons.LucideSettings
import com.example.mobileproject.presentation.ui.icons.LucideUser
import com.example.mobileproject.presentation.viewmodel.MemoriesViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun MemoriesScreen(
    accessToken: String,
    onOpenCapture: () -> Unit,
) {
    val viewModel: MemoriesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showSpecialDaySheet by rememberSaveable { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val sortedMoments = remember(uiState.moments) { sortMomentsNewestFirst(uiState.moments) }
    val upcomingSpecialDays = remember(uiState.relationshipStartAt, today) {
        buildUpcomingSpecialDays(uiState.relationshipStartAt, today, 10)
    }
    val nearestReminder = upcomingSpecialDays.firstOrNull()

    // Tải dữ liệu khi accessToken thay đổi và khi resume từ background
    LaunchedEffect(accessToken) {
        viewModel.load(accessToken)
        viewModel.onPartnerInfoClick()
    }

    // Lifecycle-aware reload: tự động refresh khi quay lại screen (ON_RESUME)
    DisposableEffect(lifecycleOwner, accessToken) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.load(accessToken)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    AppScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RecentMomentsPreviewCard(
                moments = sortedMoments,
                onOpenCapture = onOpenCapture,
            )
            MemoriesCalendarCard(
                nearestReminder = nearestReminder,
                onOpenSpecialDays = { showSpecialDaySheet = true },
            )
            PartnerSection(
                partnerProfile = uiState.partnerProfile,
                partnerUsername = uiState.partnerUsername,
                relationshipStartAt = uiState.relationshipStartAt,
                isLoading = uiState.isPartnerInfoLoading,
                onClick = viewModel::onPartnerInfoClick,
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showSpecialDaySheet) {
        SpecialDaysBottomSheet(
            days = upcomingSpecialDays,
            onDismiss = { showSpecialDaySheet = false },
        )
    }
}

/**
 * Card preview khoảnh khắc gần đây – lưới 2×n ảnh thumbnail.
 * Bên trái: ảnh lớn (132dp height), bên phải: ảnh nhỏ + ô "+N" (số còn lại).
 * Nhấp vào bất kỳ đâu để mở [CaptureMomentScreen].
 */
@Composable
private fun RecentMomentsPreviewCard(
    moments: List<MomentDto>,
    onOpenCapture: () -> Unit,
) {
    val first = moments.firstOrNull()
    val second = moments.getOrNull(1)
    val remain = (moments.size - 2).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenCapture),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCEFF1)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Khoảnh khắc gần đây",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4B3A3D),
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = LucideImage,
                    contentDescription = null,
                    tint = Color(0xFFF28A97),
                    modifier = Modifier.size(18.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(132.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F7)),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (first != null) {
                            MomentPhoto(first.imageUrl, first.title)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF28A97),
                            ) {
                                Text(
                                    text = first.title.ifBlank { "Kỷ niệm mới" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        } else {
                            EmptyMomentTile(onClick = onOpenCapture)
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .width(90.dp)
                        .height(132.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F7)),
                    ) {
                        if (second != null) {
                            MomentPhoto(second.imageUrl, second.title)
                        } else {
                            EmptyMomentTile(onClick = onOpenCapture)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF28A97)),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = if (remain > 0) "+$remain" else "Mở",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Thẻ hiển thị thông tin nửa kia (partner) – tên, số ngày bên nhau,
 * trạng thái kết nối. Nhấp để refresh thông tin.
 */
@Composable
private fun PartnerSection(
    partnerProfile: PartnerProfileSummary?,
    partnerUsername: String?,
    relationshipStartAt: String?,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val displayName = when {
        isLoading -> "Đang tải..."
        !partnerProfile?.fullName.isNullOrBlank() -> partnerProfile?.fullName.orEmpty()
        !partnerProfile?.nickName.isNullOrBlank() -> partnerProfile?.nickName.orEmpty()
        !partnerProfile?.username.isNullOrBlank() -> partnerProfile?.username.orEmpty()
        !partnerUsername.isNullOrBlank() -> partnerUsername
        else -> "Nửa kia"
    }

    val metaText = when {
        partnerProfile?.paired == false -> "Chưa kết nối cặp đôi"
        partnerProfile?.daysTogether != null -> "${partnerProfile.daysTogether} ngày bên nhau"
        !partnerProfile?.startAt.isNullOrBlank() -> "Bắt đầu từ ${partnerProfile?.startAt?.take(10)}"
        !relationshipStartAt.isNullOrBlank() -> "Bắt đầu từ ${relationshipStartAt.take(10)}"
        else -> "Chạm để làm mới thông tin"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7E3E7)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = R.drawable.sticker_11,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Thông tin nửa kia",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF4B3A3D),
                    fontWeight = FontWeight.Bold,
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8FA)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = LucideUser,
                        contentDescription = null,
                        tint = Color(0xFF2F3A40),
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3D3033),
                        )
                        Text(
                            text = metaText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E777B),
                        )
                    }
                    Icon(
                        imageVector = LucideSettings,
                        contentDescription = null,
                        tint = Color(0xFF8E777B),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * Card lịch tháng – hiển thị lưới lịch tháng hiện tại với ngày đặc biệt
 * gần nhất được đánh dấu. Nhấp để mở [SpecialDaysBottomSheet].
 */
@Composable
private fun MemoriesCalendarCard(
    nearestReminder: CalendarReminder?,
    onOpenSpecialDays: () -> Unit = {},
) {
    val today = LocalDate.now()
    val calendarMonth = remember { YearMonth.now() }
    val selectedDay = when {
        nearestReminder != null && YearMonth.from(nearestReminder.date) == calendarMonth -> nearestReminder.date.dayOfMonth
        else -> today.dayOfMonth
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSpecialDays),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7E9EB)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = calendarMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B3A3D),
                )
                Icon(
                    imageVector = LucideChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF8E777B),
                )
            }
            CalendarMonthGrid(month = calendarMonth, selectedDay = selectedDay)
            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF3EFF0)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFD8DE),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Cake,
                                contentDescription = null,
                                tint = Color(0xFFF28A97),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nearestReminder?.title ?: "Chưa có ngày đặc biệt sắp tới",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4B3A3D),
                        )
                        val subtitle = nearestReminder?.let { reminder ->
                            val days = ChronoUnit.DAYS.between(today, reminder.date).coerceAtLeast(0)
                            "Còn $days ngày - ${formatReminderDate(reminder.date)}"
                        } ?: "Chạm để xem 10 ngày đặc biệt sắp tới"
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E777B),
                        )
                    }
                    AsyncImage(
                        model = R.drawable.sticker_7,
                        contentDescription = null,
                        modifier = Modifier.size(46.dp),
                    )
                }
            }
        }
    }
}

/**
 * Bottom sheet hiển thị danh sách 10 ngày đặc biệt sắp tới.
 * Sử dụng ModalBottomSheet với skipPartiallyExpanded=true (mở full ngay).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpecialDaysBottomSheet(
    days: List<CalendarReminder>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "10 ngày đặc biệt sắp tới",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3A2A2D),
            )
            if (days.isEmpty()) {
                Text(
                    text = "Không có ngày đặc biệt nào sắp tới.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                days.forEachIndexed { index, day ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF9EEF0),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E777B),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = day.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF3D3033),
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = formatReminderDate(day.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8E777B),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Lưới lịch tháng – hiển thị 7 cột (CN-T7) × N hàng.
 * Ngày được chọn (selectedDay) có nền hồng (F28A97).
 * Tính leadingBlank từ dayOfWeek để căn chỉnh ngày đầu tháng.
 */
@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    selectedDay: Int,
) {
    val weekdays = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    val firstDay = month.atDay(1)
    val leadingBlank = firstDay.dayOfWeek.value % 7
    val days = (1..month.lengthOfMonth()).toList()
    val cells = List(leadingBlank) { 0 } + days
    val rows = (cells + List((7 - (cells.size % 7)) % 7) { 0 }).chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFAA969A),
                    modifier = Modifier.width(28.dp),
                )
            }
        }
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { day ->
                    if (day == 0) {
                        Spacer(modifier = Modifier.width(28.dp).height(28.dp))
                    } else {
                        val isSelected = day == selectedDay
                        Surface(
                            modifier = Modifier.width(28.dp).height(28.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFFF28A97) else Color.Transparent,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) Color.White else Color(0xFF4B3A3D),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hiển thị ảnh khoảnh khắc từ URL hoặc Base64 data-URL.
 * Nếu là data-URL, decode Base64 trên background thread (Dispatchers.Default)
 * trước khi hiển thị qua Coil AsyncImage.
 * Coil caching: memory + disk + network đều enabled.
 */
@Composable
private fun MomentPhoto(
    model: String,
    contentDescription: String?,
) {
    var imageModel by remember(model) { mutableStateOf<Any?>(null) }

    LaunchedEffect(model) {
        if (model.startsWith("data:")) {
            val decoded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                decodeDataUrl(model)
            }
            imageModel = decoded
        } else {
            imageModel = model
        }
    }

    if (imageModel != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageModel)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun EmptyMomentTile(
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        color = Color(0xFFFFEEF1),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Chạm để mở camera",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF28A97),
            )
        }
    }
}

private data class CalendarReminder(
    val date: LocalDate,
    val title: String,
)

private fun buildUpcomingSpecialDays(
    relationshipStartAt: String?,
    today: LocalDate,
    limit: Int,
): List<CalendarReminder> {
    val reminders = mutableListOf<CalendarReminder>()
    val thisYear = today.year

    listOf(
        CalendarReminder(LocalDate.of(thisYear, 2, 14), "Lễ Tình nhân"),
        CalendarReminder(LocalDate.of(thisYear, 3, 8), "Quốc tế Phụ nữ"),
        CalendarReminder(LocalDate.of(thisYear, 11, 19), "Quốc tế Nam giới"),
        CalendarReminder(LocalDate.of(thisYear, 12, 25), "Giáng sinh"),
        CalendarReminder(LocalDate.of(thisYear + 1, 2, 14), "Lễ Tình nhân"),
        CalendarReminder(LocalDate.of(thisYear + 1, 3, 8), "Quốc tế Phụ nữ"),
        CalendarReminder(LocalDate.of(thisYear + 1, 11, 19), "Quốc tế Nam giới"),
        CalendarReminder(LocalDate.of(thisYear + 1, 12, 25), "Giáng sinh"),
    ).filterTo(reminders) { !it.date.isBefore(today) }

    parseRelationshipStartDate(relationshipStartAt)?.let { startDate ->
        reminders += CalendarReminder(startDate.plusDays(100), "Kỷ niệm 100 ngày")
        reminders += CalendarReminder(startDate.plusDays(200), "Kỷ niệm 200 ngày")
        reminders += CalendarReminder(startDate.plusDays(300), "Kỷ niệm 300 ngày")
        reminders += CalendarReminder(startDate.plusYears(1), "Kỷ niệm 1 năm")
        reminders += CalendarReminder(startDate.plusYears(2), "Kỷ niệm 2 năm")
    }

    return reminders
        .filter { !it.date.isBefore(today) }
        .distinctBy { it.date to it.title }
        .sortedBy { it.date }
        .take(limit)
}

private fun parseRelationshipStartDate(raw: String?): LocalDate? {
    if (raw.isNullOrBlank()) return null
    val datePart = raw.trim().take(10)
    return runCatching { LocalDate.parse(datePart) }.getOrNull()
}

private fun formatReminderDate(date: LocalDate): String {
    val month = date.monthValue.toString().padStart(2, '0')
    val day = date.dayOfMonth.toString().padStart(2, '0')
    return "$day/$month/${date.year}"
}

private fun sortMomentsNewestFirst(moments: List<MomentDto>): List<MomentDto> {
    return moments.sortedByDescending { parseMomentEpochMillis(it.createdAt) ?: Long.MIN_VALUE }
}

private fun parseMomentEpochMillis(raw: String?): Long? {
    if (raw.isNullOrBlank()) return null
    val text = raw.trim()
    return runCatching {
        when {
            text.endsWith("Z") || text.contains("+") -> Instant.parse(text).toEpochMilli()
            else -> {
                val local = LocalDateTime.parse(text.replace(" ", "T").substringBefore('.'))
                local.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }
    }.getOrNull()
}

private fun decodeDataUrl(dataUrl: String): ByteArray? {
    if (!dataUrl.startsWith("data:")) return null
    val commaIndex = dataUrl.indexOf(',')
    if (commaIndex == -1 || commaIndex == dataUrl.lastIndex) return null
    val base64 = dataUrl.substring(commaIndex + 1)
    return try {
        Base64.decode(base64, Base64.DEFAULT)
    } catch (_: IllegalArgumentException) {
        null
    }
}
