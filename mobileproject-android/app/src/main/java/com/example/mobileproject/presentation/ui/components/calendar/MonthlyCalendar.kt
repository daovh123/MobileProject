package com.example.mobileproject.presentation.ui.components.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import java.util.Calendar
import java.util.GregorianCalendar

@Composable
fun MonthlyCalendarCard(
    year: Int,
    month: Int, // 1-12
    notesByDay: Map<Int, List<String>>,
    modifier: Modifier = Modifier,
) {
    val weekdayLabels = remember {
        listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    }

    val weeks = remember(year, month) {
        buildMonthCells(
            year = year,
            month = month,
            weekStart = Calendar.MONDAY,
        ).chunked(7)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.memories_calendar_month_title, month, year),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.md3_primary),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                weekdayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = colorResource(R.color.md3_on_surface_variant),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        week.forEach { dayOfMonth ->
                            CalendarDayCell(
                                dayOfMonth = dayOfMonth,
                                noteLines = if (dayOfMonth != null) {
                                    notesByDay[dayOfMonth].orEmpty()
                                } else {
                                    emptyList()
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayOfMonth: Int?,
    noteLines: List<String>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(56.dp),
        color = colorResource(R.color.md3_surface_variant),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colorResource(R.color.md3_outline)),
    ) {
        if (dayOfMonth == null) {
            Box(modifier = Modifier.fillMaxSize())
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Text(
                text = dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.md3_on_surface),
            )

            val noteText = noteLines
                .asSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .joinToString(separator = "\n")

            if (noteText.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = noteText,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(R.color.md3_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun buildMonthCells(
    year: Int,
    month: Int, // 1-12
    weekStart: Int,
): List<Int?> {
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

    val cells = MutableList<Int?>(42) { null }
    for (day in 1..daysInMonth) {
        val index = firstIndex + (day - 1)
        if (index in 0..41) {
            cells[index] = day
        }
    }

    return cells
}

object VietnamCalendarNotes {
    fun notesForMonth(
        year: Int,
        month: Int,
    ): Map<Int, List<String>> {
        val notes = linkedMapOf<Int, MutableList<String>>()

        fun add(day: Int, label: String) {
            notes.getOrPut(day) { mutableListOf() }.add(label)
        }

        when (month) {
            1 -> {
                add(1, "Tết Dương lịch")
            }

            2 -> {
                add(14, "Valentine")
            }

            3 -> {
                add(8, "Quốc tế Phụ nữ")
            }

            4 -> {
                add(30, "Giải phóng miền Nam")
            }

            5 -> {
                add(1, "Quốc tế Lao động")
            }

            9 -> {
                add(2, "Quốc khánh")
            }

            10 -> {
                add(20, "Phụ nữ Việt Nam")
            }

            11 -> {
                add(20, "Nhà giáo Việt Nam")
            }

            12 -> {
                add(25, "Giáng sinh")
            }
        }

        // year is reserved for future year-specific notes.
        return notes.mapValues { (_, values) -> values.toList() }
    }
}
