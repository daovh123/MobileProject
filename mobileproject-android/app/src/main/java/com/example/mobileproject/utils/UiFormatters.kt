package com.example.mobileproject.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

// ── Vietnamese locale & currency formatting ──────────────────────────────────

/** Vietnamese locale used for all date/number formatting in this file. */
private val vnLocale = Locale("vi", "VN")

/** Symbols that use dot (`.`) as the thousands separator (Vietnamese convention). */
private val vnSymbols = DecimalFormatSymbols(vnLocale).apply { groupingSeparator = '.' }

/** Pre-configured formatter: groups digits with dots and no decimal places. */
private val vndFormatter = DecimalFormat("#,###", vnSymbols)

/**
 * Extension on [Long] that formats a monetary amount in Vietnamese Dong.
 * Uses dot grouping (e.g. 1.234.567) and appends the "đ" suffix.
 * Negative amounts are prefixed with a minus sign.
 *
 * Usage: `val display = 150000L.toVndCurrency()` → `"150.000đ"`
 */
fun Long.toVndCurrency(): String {
    val formatted = vndFormatter.format(absoluteValue)
    return if (this < 0) "-${formatted}đ" else "${formatted}đ"
}

/** Convenience overload so [Int] amounts can also be formatted directly. */
fun Int.toVndCurrency(): String = toLong().toVndCurrency()

// ── Date/time formatting ─────────────────────────────────────────────────────

/**
 * Formats a [LocalDateTime] into a Vietnamese human-readable moment string.
 * Output format: `"HH:mm {period} • {day} tháng {month}"` where period is:
 *   - "sáng" (morning)   for hours 0–11
 *   - "chiều" (afternoon) for hours 12–17
 *   - "tối" (evening)    for hours 18–23
 *
 * Example: `"14:30 chiều • 5 tháng 6"`
 */
fun formatMomentDate(date: LocalDateTime): String {
    val period = when (date.hour) {
        in 0..11 -> "sáng"
        in 12..17 -> "chiều"
        else -> "tối"
    }
    val timeText = date.format(DateTimeFormatter.ofPattern("HH:mm", vnLocale))
    return "$timeText $period • ${date.dayOfMonth} tháng ${date.monthValue}"
}

/**
 * Parses a raw date string (ISO-8601 with Z/offset, or "yyyy-MM-dd HH:mm:ss" format)
 * and formats it via [formatMomentDate]. Returns the original string on parse failure.
 *
 * Handles two common backend date formats:
 *   - ISO-8601 with timezone: "2024-06-05T14:30:00Z" or "+07:00" → converted to device local time.
 *   - Plain datetime: "2024-06-05 14:30:00" → parsed as-is (assumed local time).
 *
 * @param raw nullable date string from API response or local storage.
 * @return formatted Vietnamese moment string, or empty string if [raw] is null/blank.
 */
fun formatMomentDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val text = raw.trim()
    val parsed = runCatching {
        if (text.endsWith("Z") || text.contains("+")) {
            // ISO-8601 with timezone offset → convert to device local time.
            Instant.parse(text).atZone(ZoneId.systemDefault()).toLocalDateTime()
        } else {
            // Plain datetime string: normalize space to 'T' and strip fractional seconds.
            LocalDateTime.parse(text.replace(" ", "T").substringBefore('.'))
        }
    }.getOrNull() ?: return text  // Return raw string if parsing fails.
    return formatMomentDate(parsed)
}
