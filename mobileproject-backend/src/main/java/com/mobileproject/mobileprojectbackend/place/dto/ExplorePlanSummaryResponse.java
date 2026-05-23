package com.mobileproject.mobileprojectbackend.place.dto;

public record ExplorePlanSummaryResponse(
        Long totalBudget,
        Integer peopleCount,
        Integer desiredStops,
        Long estimatedTotalCost,
        Boolean lowBalance,
        String balanceMessage,
        Long suggestedDefaultBudget) {
}
