package com.mobileproject.mobileprojectbackend.analytics.dto;

public record MonthlyTrendItem(
        int month,
        Long totalIncome,
        Long totalExpense
) {
}