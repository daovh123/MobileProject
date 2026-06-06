package com.example.mobileproject.utils

import java.text.DecimalFormat

/**
 * Formats a [Long] amount as a comma-separated string (e.g. 1234567 → "1,234,567").
 * Used for simple numeric display where no currency symbol or locale-specific grouping
 * is needed (e.g. point balances, generic counters).
 *
 * For Vietnamese Dong formatting with "đ" suffix and dot grouping, see [Long.toVndCurrency].
 *
 * @param amount the raw numeric value.
 * @return formatted string with thousand separators.
 */
fun formatSimpleAmount(amount: Long): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(amount)
}
