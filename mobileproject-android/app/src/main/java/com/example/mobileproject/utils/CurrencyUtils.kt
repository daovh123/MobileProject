package com.example.mobileproject.utils

import java.text.DecimalFormat

fun formatSimpleAmount(amount: Long): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(amount)
}
