package com.example.mobileproject.domain.entity

data class ExplorePlan(
    val summary: ExplorePlanSummary,
    val items: List<ExplorePlanItem>,
)

data class ExplorePlanSummary(
    val totalBudget: Long,
    val peopleCount: Int,
    val desiredStops: Int,
    val estimatedTotalCost: Long,
    val lowBalance: Boolean,
    val balanceMessage: String?,
    val suggestedDefaultBudget: Long,
)

data class ExplorePlanItem(
    val stopOrder: Int,
    val experienceType: String,
    val estimatedCost: Long,
    val reason: String,
    val place: Place,
)
