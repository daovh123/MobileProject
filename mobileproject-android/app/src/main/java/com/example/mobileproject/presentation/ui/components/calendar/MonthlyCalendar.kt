/**
 * Component lịch tháng cho màn hình kỷ niệm.
 *
 * Cung cấp:
 * - [MonthlyCalendarCard]: Lưới lịch 7×6 với ghi chú sự kiện.
 * - [VietnamCalendarNotes]: Danh sách ngày lễ Việt Nam theo tháng.
 * - [buildMonthCells]: Utility xây dựng mảng ô lịch.
 */
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Card lịch tháng hiển thị các ngày trong tháng với ghi chú.
 *
 * Render lưới 7 cột (T2-CN) × 6 hàng, mỗi ô hiển thị số ngày
 * và ghi chú (nếu có). Ghi chú hiển thị tối đa 2 dòng với màu primary.
 *
 * @param year Năm cần hiển thị.
 * @param month Tháng cần hiển thị (1-12).
 * @param notesByDay Map từ ngày (Int) sang danh sách ghi chú (List<String>).
 * @param modifier [Modifier] tùy chỉnh.
 */
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.memories_calendar_month_title, month, year),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

/**
 * Ô hiển thị một ngày trong tháng trên lịch.
 *
 * Render số ngày và ghi chú (nếu có) trong Surface bo góc.
 * Nếu [dayOfMonth] là null (ô trống), hiển thị ô rỗng.
 *
 * @param dayOfMonth Số ngày trong tháng (1-31) hoặc null cho ô trống.
 * @param noteLines Danh sách ghi chú cho ngày này.
 * @param modifier [Modifier] tùy chỉnh.
 */
@Composable
private fun CalendarDayCell(
    dayOfMonth: Int?,
    noteLines: List<String>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(56.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                color = MaterialTheme.colorScheme.onSurface,
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
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Xây dựng mảng 42 ô (6 hàng × 7 cột) cho lưới lịch tháng.
 *
 * Các ô trống (trước ngày đầu tháng và sau ngày cuối tháng) có giá trị null.
 * Tuần bắt đầu từ [weekStart] (mặc định Calendar.MONDAY = T2).
 *
 * @param year Năm.
 * @param month Tháng (1-12).
 * @param weekStart Ngày bắt đầu tuần (Calendar.MONDAY, Calendar.SUNDAY, ...).
 * @return List 42 phần tử, mỗi phần tử là số ngày hoặc null.
 */
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

/**
 * Cung cấp ghi chú các ngày lễ và sự kiện quan trọng của Việt Nam.
 *
 * Chỉ chứa các ngày lễ dương lịch cố định (không bao gồm âm lịch).
 * Dữ liệu được hardcode, reserved để mở rộng theo năm trong tương lai.
 */
object VietnamCalendarNotes {
    /**
     * Lấy danh sách ghi chú cho một tháng cụ thể.
     *
     * @param year Năm (dùng cho future扩展, hiện tại chưa sử dụng).
     * @param month Tháng (1-12).
     * @return Map từ ngày sang danh sách ghi chú.
     */
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
