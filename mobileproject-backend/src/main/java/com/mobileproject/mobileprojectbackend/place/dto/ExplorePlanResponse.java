package com.mobileproject.mobileprojectbackend.place.dto;

import java.util.List;

public record ExplorePlanResponse(
        ExplorePlanSummaryResponse summary,
        List<ExplorePlanItemResponse> items) {
}
