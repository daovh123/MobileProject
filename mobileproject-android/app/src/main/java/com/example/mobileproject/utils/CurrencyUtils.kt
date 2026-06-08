package com.example.mobileproject.utils

import java.text.DecimalFormat

fun formatSimpleAmount(amount: Long): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(amount)
}

fun formatAmountWithDots(rawDigits: String): String {
    if (rawDigits.isEmpty()) return ""
    return rawDigits.reversed().chunked(3).joinToString(".").reversed()
}

fun stripAmountFormatting(formatted: String): String {
    return formatted.filter { it.isDigit() }
}
