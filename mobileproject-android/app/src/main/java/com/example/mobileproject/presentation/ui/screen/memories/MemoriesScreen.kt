package com.example.mobileproject.presentation.ui.screen.memories

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import com.example.mobileproject.presentation.seed.SeedDataProvider
import com.example.mobileproject.presentation.ui.components.core.AppPrimaryButton
import com.example.mobileproject.presentation.ui.components.core.AppScreenBackground
import com.example.mobileproject.presentation.ui.components.core.AppSectionHeader
import com.example.mobileproject.presentation.ui.components.core.AppSurfaceCard
import com.example.mobileproject.presentation.ui.components.calendar.VietnamCalendarNotes
import kotlinx.coroutines.delay
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

@Composable
fun MemoriesScreen(
    accessToken: String,
    onNavigateToExplore: () -> Unit,
) {
    val now = remember { Calendar.getInstance() }
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH) + 1
    val today = now.get(Calendar.DAY_OF_MONTH)
    val calendarNotes = remember(currentYear, currentMonth) {
        VietnamCalendarNotes.notesForMonth(year = currentYear, month = currentMonth)
    }
    val monthTitle = remember(currentYear, currentMonth) {
        buildMonthTitle(year = currentYear, month = currentMonth)
    }
    var revealIndex by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        delay(70)
        revealIndex = 1
        delay(80)
        revealIndex = 2
        delay(90)
        revealIndex = 3
        delay(90)
        revealIndex = 4
    }

    AppScreenBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AppSectionHeader(
                    title = stringResource(R.string.memories_header_title),
                    subtitle = stringResource(
                        R.string.memories_greeting,
                        SeedDataProvider.memoryGreetingName,
                    ),
                )
            }

            item {
                AnimatedVisibility(visible = revealIndex >= 1) {
                    AppSurfaceCard {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                            RecentMomentsCard(
                                mainLabel = SeedDataProvider.memoryMoments.getOrNull(0).orEmpty(),
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = revealIndex >= 2) {
                    AppSurfaceCard {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                            MemoriesCalendarCard(
                                monthTitle = monthTitle,
                                year = currentYear,
                                month = currentMonth,
                                selectedDay = today,
                                notesByDay = calendarNotes,
                                eventTitle = SeedDataProvider.memoryEventTitle,
                                eventSubtitle = SeedDataProvider.memoryEventSubtitle,
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = revealIndex >= 3) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppSectionHeader(
                            title = stringResource(R.string.memories_partner_info),
                            subtitle = stringResource(R.string.memories_tagline),
                        )
                        PartnerInfoCard(
                            partnerName = SeedDataProvider.partnerName,
                            partnerMeta = SeedDataProvider.partnerMeta,
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(visible = revealIndex >= 4) {
                    AppPrimaryButton(
                        text = stringResource(R.string.page_explore),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToExplore,
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoriesHeader(
    name: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.memories_greeting, name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.md3_primary),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.memories_header_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.md3_on_surface),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.memories_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(R.color.md3_on_surface_variant),
        )
    }
}

@Composable
private fun RecentMomentsCard(
    mainLabel: String,
    modifier: Modifier = Modifier,
) {
    val mainPhotoC1 = colorResource(R.color.md3_tertiary_container)
    val mainPhotoC2 = colorResource(R.color.md3_secondary_container)
    val mainPhotoC3 = colorResource(R.color.md3_primary_container)
    val mainPhotoBrush = remember(mainPhotoC1, mainPhotoC2, mainPhotoC3) {
        Brush.linearGradient(listOf(mainPhotoC1, mainPhotoC2, mainPhotoC3))
    }

    val secondaryPhotoC1 = colorResource(R.color.md3_secondary_container)
    val secondaryPhotoC2 = colorResource(R.color.md3_tertiary_container)
    val secondaryPhotoBrush = remember(secondaryPhotoC1, secondaryPhotoC2) {
        Brush.linearGradient(listOf(secondaryPhotoC1, secondaryPhotoC2))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.memories_recent_moments),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.md3_on_surface),
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colorResource(R.color.md3_primary_container),
                    border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_memories_24),
                        contentDescription = null,
                        tint = colorResource(R.color.md3_primary),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.65f)
                        .height(176.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(mainPhotoBrush),
                ) {
                    val label = mainLabel.ifBlank { stringResource(R.string.memories_recent_moments) }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = colorResource(R.color.md3_primary_container),
                        border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.md3_primary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(secondaryPhotoBrush),
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = colorResource(R.color.md3_primary_container),
                        border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.memories_more_count),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.md3_primary),
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class DayCell(
    val dayNumber: Int,
    val inCurrentMonth: Boolean,
    val note: String?,
)

@Composable
private fun MemoriesCalendarCard(
    monthTitle: String,
    year: Int,
    month: Int, // 1-12
    selectedDay: Int,
    notesByDay: Map<Int, List<String>>,
    eventTitle: String,
    eventSubtitle: String,
    modifier: Modifier = Modifier,
) {
    val weekdayLabels = remember {
        listOf("S", "M", "T", "W", "T", "F", "S")
    }

    val dayCells = remember(year, month, notesByDay) {
        buildMonthGrid(
            year = year,
            month = month,
            weekStart = Calendar.SUNDAY,
            notesByDay = notesByDay,
        )
    }.chunked(7)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = colorResource(R.color.md3_primary_container),
                    border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_memories_24),
                            contentDescription = null,
                            tint = colorResource(R.color.md3_primary),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.md3_on_surface),
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    painter = painterResource(R.drawable.ic_expand_more_24),
                    contentDescription = null,
                    tint = colorResource(R.color.md3_on_surface_variant),
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(-90f),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                weekdayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.md3_on_surface_variant),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                dayCells.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { cell ->
                            MemoriesCalendarDayCell(
                                cell = cell,
                                selectedDay = selectedDay,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = colorResource(R.color.md3_surface_variant),
                border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = colorResource(R.color.md3_primary_container),
                        border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_memories_24),
                                contentDescription = null,
                                tint = colorResource(R.color.md3_primary),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = eventTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.md3_on_surface),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = eventSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(R.color.md3_on_surface_variant),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoriesCalendarDayCell(
    cell: DayCell,
    selectedDay: Int,
    modifier: Modifier = Modifier,
) {
    val isSelected = cell.inCurrentMonth && cell.dayNumber == selectedDay
    val inMonth = cell.inCurrentMonth

    val numberColor = when {
        isSelected -> colorResource(R.color.md3_on_primary)
        inMonth -> colorResource(R.color.md3_on_surface)
        else -> colorResource(R.color.md3_on_surface_variant).copy(alpha = 0.45f)
    }

    val noteText = cell.note.orEmpty()
    val showNote = inMonth && noteText.isNotBlank()

    Box(
        modifier = modifier.height(56.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colorResource(R.color.md3_primary), CircleShape),
                    )
                }
            }

            if (isSelected) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = colorResource(R.color.md3_primary),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = cell.dayNumber.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = numberColor,
                        )
                    }
                }
            } else {
                Text(
                    text = cell.dayNumber.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = numberColor,
                    textAlign = TextAlign.Center,
                )
            }

            if (showNote) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = noteText,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(R.color.md3_primary),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PartnerInfoCard(
    partnerName: String,
    partnerMeta: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = colorResource(R.color.md3_surface_variant),
                border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_avatar_24),
                        contentDescription = null,
                        tint = colorResource(R.color.md3_primary),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = partnerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.md3_on_surface),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_heart_filled),
                        contentDescription = null,
                        tint = colorResource(R.color.md3_primary),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = partnerMeta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(R.color.md3_on_surface_variant),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = colorResource(R.color.md3_surface_variant),
                border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tune_24),
                        contentDescription = null,
                        tint = colorResource(R.color.md3_on_surface_variant),
                    )
                }
            }
        }
    }
}

private fun buildMonthTitle(
    year: Int,
    month: Int,
): String {
    val locale = Locale.getDefault()
    val rawMonth = DateFormatSymbols(locale).months.getOrNull(month - 1).orEmpty()
    val monthName = rawMonth.replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(locale) else ch.toString()
    }.trim()

    if (monthName.isBlank()) {
        return "$month/$year"
    }
    return "$monthName $year"
}

private fun buildMonthGrid(
    year: Int,
    month: Int,
    weekStart: Int,
    notesByDay: Map<Int, List<String>>,
): List<DayCell> {
    val cal = GregorianCalendar().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val firstIndex = ((firstDayOfWeek - weekStart) + 7) % 7

    val prevMonthCal = GregorianCalendar().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, -1)
    }
    val daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val leadingStart = (daysInPrevMonth - firstIndex + 1).coerceAtLeast(1)

    val cells = ArrayList<DayCell>(42)

    for (i in 0 until firstIndex) {
        cells.add(
            DayCell(
                dayNumber = leadingStart + i,
                inCurrentMonth = false,
                note = null,
            )
        )
    }

    for (day in 1..daysInMonth) {
        cells.add(
            DayCell(
                dayNumber = day,
                inCurrentMonth = true,
                note = notesByDay[day]?.firstOrNull(),
            )
        )
    }

    var nextDay = 1
    while (cells.size < 42) {
        cells.add(
            DayCell(
                dayNumber = nextDay,
                inCurrentMonth = false,
                note = null,
            )
        )
        nextDay++
    }

    return cells
}
