package com.mobileproject.mobileprojectbackend.place.dto;

import java.util.List;

/**
 * Response của kế hoạch khám phá, bao gồm tóm tắt và danh sách địa điểm gợi ý.
 *
 * @param summary tóm tắt ngân sách, số người, chi phí ước tính
 * @param items   danh sách địa điểm trong kế hoạch
 */
public record ExplorePlanResponse(
        ExplorePlanSummaryResponse summary,
        List<ExplorePlanItemResponse> items) {
}
