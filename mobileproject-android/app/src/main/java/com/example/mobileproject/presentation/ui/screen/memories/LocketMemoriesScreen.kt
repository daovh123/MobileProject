package com.example.mobileproject.presentation.ui.screen.memories

import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.mobileproject.R
import com.example.mobileproject.data.model.moment.MomentCommentDto
import com.example.mobileproject.data.model.moment.MomentDto
import com.example.mobileproject.presentation.ui.components.core.AppScreenBackground
import com.example.mobileproject.presentation.ui.icons.LucideCamera
import com.example.mobileproject.presentation.ui.icons.LucideImage
import com.example.mobileproject.presentation.ui.icons.LucideReply
import com.example.mobileproject.presentation.ui.icons.LucideSend
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
fun LocketMemoriesScreen(
    accessToken: String,
    onOpenCapture: () -> Unit,
) {
    val viewModel: MemoriesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var commentDraft by rememberSaveable { mutableStateOf("") }
    var showMomentsHistory by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(accessToken) {
        viewModel.load(accessToken)
    }

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

    AppScreenBackground(modifier = Modifier.background(Color(0xFFFCE5E8))) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val nearestReminder = remember(uiState.relationshipStartAt) {
                buildUpcomingCalendarReminders(
                    relationshipStartAt = uiState.relationshipStartAt,
                    today = LocalDate.now(),
                ).firstOrNull()
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    MemoriesTopSection()
                }

                item {
                    RecentMomentsCard(
                        moments = uiState.moments,
                        onOpenCapture = onOpenCapture,
                        onOpenHistory = { showMomentsHistory = true },
                    )
                }

                item {
                    MemoriesCalendarCard(
                        nearestReminder = nearestReminder,
                    )
                }

                item {
                    PartnerInfoCard(
                        partnerName = uiState.partnerUsername ?: stringResource(R.string.memories_partner_unknown),
                        relationshipStartAt = uiState.relationshipStartAt,
                    )
                }
            }
        }
    }

    if (uiState.isCommentsVisible) {
        CommentsBottomSheet(
            comments = uiState.comments,
            isLoading = uiState.isCommentsLoading,
            draft = commentDraft,
            onDraftChange = { commentDraft = it },
            onDismiss = {
                commentDraft = ""
                viewModel.dismissComments()
            },
            onSend = {
                viewModel.submitComment(commentDraft)
                commentDraft = ""
            },
        )
    }

    if (showMomentsHistory) {
        MomentsHistoryBottomSheet(
            moments = uiState.moments,
            onDismiss = { showMomentsHistory = false },
            onCreateNew = {
                showMomentsHistory = false
                onOpenCapture()
            },
        )
    }
}

@Composable
private fun MemoriesTopSection() {
    Spacer(modifier = Modifier.height(2.dp))
    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "Kỷ niệm của chúng ta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF3A2A2D),
            )
            Text(
                text = "Mỗi đồng tiền kể câu chuyện của chúng ta.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8E777B),
            )
        }

        Image(
            painter = painterResource(R.drawable.sticker_10),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(112.dp)
                .offset(x = 4.dp, y = 4.dp),
        )
    }
}

@Composable
private fun RecentMomentsCard(
    moments: List<MomentDto>,
    onOpenCapture: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val latestMoment = moments.firstOrNull()
    val secondMoment = moments.getOrNull(1)
    val remainingCount = (moments.size - 2).coerceAtLeast(12)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenHistory),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8E8EA)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Khoảnh khắc gần đây",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B3A3D),
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
                    modifier = Modifier.weight(1f).height(126.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (latestMoment != null) {
                            MomentPhoto(model = latestMoment.imageUrl, contentDescription = latestMoment.title)
                            Surface(
                                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF28A97),
                            ) {
                                Text(
                                    text = latestMoment.title.ifBlank { "Bữa tối kỷ niệm" },
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        } else {
                            EmptyMomentTile(onOpenCapture = onOpenCapture)
                        }
                    }
                }

                Column(modifier = Modifier.width(78.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(59.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        if (secondMoment != null) {
                            MomentPhoto(model = secondMoment.imageUrl, contentDescription = secondMoment.title)
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = LucideCamera,
                                    contentDescription = null,
                                    tint = Color(0xFFF28A97),
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().height(59.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF28A97)),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().clickable(onClick = onOpenCapture),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "+$remainingCount",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MomentsHistoryBottomSheet(
    moments: List<MomentDto>,
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sortedMoments = remember(moments) { sortMomentsNewestFirst(moments) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Toàn bộ kỷ niệm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3A2A2D),
            )

            Button(
                onClick = onCreateNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF28A97),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = LucideCamera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo kỷ niệm mới", fontWeight = FontWeight.SemiBold)
            }

            if (sortedMoments.isEmpty()) {
                Text(
                    text = "Chưa có kỷ niệm nào.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(sortedMoments) { moment ->
                        MomentHistoryItem(moment = moment)
                    }
                }
            }
        }
    }
}

@Composable
private fun MomentHistoryItem(moment: MomentDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                MomentPhoto(model = moment.imageUrl, contentDescription = moment.title)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = moment.title.ifBlank { "Khoảnh khắc mới" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = moment.createdAt?.let { formatMomentDateText(it) } ?: "Không rõ thời gian",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MemoriesCalendarCard(
    nearestReminder: CalendarReminder?,
) {
    val today = remember { LocalDate.now() }
    val calendarMonth = remember { YearMonth.now() }
    val selectedDay = when {
        nearestReminder != null && YearMonth.from(nearestReminder.date) == calendarMonth -> nearestReminder.date.dayOfMonth
        else -> today.dayOfMonth
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7E9EB)),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Tháng ${calendarMonth.monthValue}/${calendarMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B3A3D),
                    )
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF8E777B),
                    )
                }

                CalendarMonthGrid(month = calendarMonth, selectedDay = selectedDay)

                Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF3EFF0)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(modifier = Modifier.size(28.dp), shape = RoundedCornerShape(14.dp), color = Color(0xFFFFD8DE)) {
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
                                text = nearestReminder?.title ?: "Kỷ niệm gần nhất",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4B3A3D),
                            )
                            val subtitle = nearestReminder?.let { reminder ->
                                val days = ChronoUnit.DAYS.between(today, reminder.date).coerceAtLeast(0)
                                "Còn $days ngày • ${formatReminderDate(reminder.date)}"
                            } ?: "Chưa có dữ liệu ngày bắt đầu"
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8E777B),
                            )
                        }
                    }
                }
            }

            Image(
                painter = painterResource(R.drawable.sticker_7),
                contentDescription = null,
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-6).dp),
            )
        }
    }
}

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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

@Composable
private fun PartnerInfoCard(
    partnerName: String,
    relationshipStartAt: String?,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Thông tin đối phương",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B3A3D),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F1F2)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFE9E9ED),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = LucideUser, contentDescription = null, tint = Color(0xFF6A6A74))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = partnerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3D2E31),
                        )
                        Text(
                            text = "♡ ${relationshipStartAt?.let { formatStartYear(it) } ?: "Đã kết nối"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E777B),
                        )
                    }
                }
            }
        }

        Image(
            painter = painterResource(R.drawable.sticker_4),
            contentDescription = null,
            modifier = Modifier
                .size(58.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (-14).dp, y = 8.dp),
        )
    }
}

@Composable
private fun MomentPhoto(
    model: String,
    contentDescription: String?,
) {
    val imageData = remember(model) { decodeDataUrl(model) }
    val imageModel = imageData ?: model
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
}

@Composable
private fun EmptyMomentTile(
    onOpenCapture: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize().clickable(onClick = onOpenCapture),
        color = Color(0xFFFFEEF1),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Chạm để thêm",
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

private fun buildUpcomingCalendarReminders(
    relationshipStartAt: String?,
    today: LocalDate,
): List<CalendarReminder> {
    val reminders = mutableListOf<CalendarReminder>()
    val thisYear = today.year

    listOf(
        CalendarReminder(LocalDate.of(thisYear, 2, 14), "Valentine"),
        CalendarReminder(LocalDate.of(thisYear, 3, 8), "Quốc tế Phụ nữ 8/3"),
        CalendarReminder(LocalDate.of(thisYear + 1, 2, 14), "Valentine"),
        CalendarReminder(LocalDate.of(thisYear + 1, 3, 8), "Quốc tế Phụ nữ 8/3"),
    ).filterTo(reminders) { !it.date.isBefore(today) }

    val startDate = parseRelationshipStartDate(relationshipStartAt)
    if (startDate != null) {
        val daysTogether = ChronoUnit.DAYS.between(startDate, today).coerceAtLeast(0)
        val firstMilestone = (((daysTogether / 100) + 1) * 100).toInt()
        for (step in 0 until 8) {
            val milestoneDays = firstMilestone + (step * 100)
            reminders += CalendarReminder(
                date = startDate.plusDays(milestoneDays.toLong()),
                title = "Kỷ niệm $milestoneDays ngày bên nhau",
            )
        }

        val yearsTogether = ChronoUnit.YEARS.between(startDate, today).coerceAtLeast(0)
        val firstYear = (yearsTogether + 1).toInt()
        for (step in 0 until 5) {
            val milestoneYear = firstYear + step
            reminders += CalendarReminder(
                date = startDate.plusYears(milestoneYear.toLong()),
                title = "Kỷ niệm $milestoneYear năm bên nhau",
            )
        }
    }

    return reminders
        .distinctBy { it.date to it.title }
        .filter { !it.date.isBefore(today) }
        .sortedBy { it.date }
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

private fun formatMomentDateText(raw: String): String {
    val epoch = parseMomentEpochMillis(raw)
    if (epoch == null) return raw
    val dateTime = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val dateFormatter = DateTimeFormatter.ofPattern("HH:mm • dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
    return dateTime.format(dateFormatter)
}

private fun parseRelationshipStartDate(raw: String?): LocalDate? {
    if (raw.isNullOrBlank()) return null
    val trimmed = raw.trim()
    val datePart = if (trimmed.length >= 10) trimmed.substring(0, 10) else trimmed
    return runCatching { LocalDate.parse(datePart) }.getOrNull()
}

private fun formatReminderDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")))
}

private fun formatStartYear(raw: String): String {
    val date = parseRelationshipStartDate(raw) ?: return "Đã kết nối"
    return "Đã kết nối từ ${date.year}"
}

private fun decodeDataUrl(dataUrl: String): ByteArray? {
    if (!dataUrl.startsWith("data:")) {
        return null
    }
    val commaIndex = dataUrl.indexOf(',')
    if (commaIndex == -1 || commaIndex == dataUrl.lastIndex) {
        return null
    }
    val base64 = dataUrl.substring(commaIndex + 1)
    return try {
        Base64.decode(base64, Base64.DEFAULT)
    } catch (_: IllegalArgumentException) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsBottomSheet(
    comments: List<MomentCommentDto>,
    isLoading: Boolean,
    draft: String,
    onDraftChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.memories_locket_comments),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (comments.isEmpty()) {
                Text(
                    text = stringResource(R.string.memories_locket_comments_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(comments) { comment ->
                        CommentRow(comment = comment)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    placeholder = { Text(stringResource(R.string.memories_locket_comment_hint)) },
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onSend, enabled = draft.isNotBlank()) {
                    Icon(
                        imageVector = LucideSend,
                        contentDescription = stringResource(R.string.memories_locket_send),
                        tint = if (draft.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentRow(comment: MomentCommentDto, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = comment.authorUsername.ifBlank { stringResource(R.string.memories_locket_comment_author_fallback) },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
