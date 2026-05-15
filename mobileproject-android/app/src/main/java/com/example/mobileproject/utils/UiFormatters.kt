package com.example.mobileproject.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

private val vnLocale = Locale("vi", "VN")
private val vnSymbols = DecimalFormatSymbols(vnLocale).apply { groupingSeparator = '.' }
private val vndFormatter = DecimalFormat("#,###", vnSymbols)

fun Long.toVndCurrency(): String {
    val formatted = vndFormatter.format(absoluteValue)
    return if (this < 0) "-${formatted}đ" else "${formatted}đ"
}

fun Int.toVndCurrency(): String = toLong().toVndCurrency()

fun formatMomentDate(date: LocalDateTime): String {
    val period = when (date.hour) {
        in 0..11 -> "sáng"
        in 12..17 -> "chiều"
        else -> "tối"
    }
    val timeText = date.format(DateTimeFormatter.ofPattern("HH:mm", vnLocale))
    return "$timeText $period • ${date.dayOfMonth} tháng ${date.monthValue}"
}

fun formatMomentDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val text = raw.trim()
    val parsed = runCatching {
        if (text.endsWith("Z") || text.contains("+")) {
            Instant.parse(text).atZone(ZoneId.systemDefault()).toLocalDateTime()
        } else {
            LocalDateTime.parse(text.replace(" ", "T").substringBefore('.'))
        }
    }.getOrNull() ?: return text
    return formatMomentDate(parsed)
}
