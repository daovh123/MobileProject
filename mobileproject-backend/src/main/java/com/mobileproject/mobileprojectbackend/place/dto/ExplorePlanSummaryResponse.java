package com.mobileproject.mobileprojectbackend.place.dto;

/**
 * Tóm tắt kế hoạch khám phá.
 *
 * @param totalBudget          tổng ngân sách (VND)
 * @param peopleCount          số người
 * @param desiredStops         số điểm dừng mong muốn
 * @param estimatedTotalCost   tổng chi phí ước tính
 * @param lowBalance           cảnh báo số dư thấp (< 30.000đ)
 * @param balanceMessage       thông báo khi số dư thấp
 * @param suggestedDefaultBudget ngân sách đề xuất mặc định (30.000đ)
 */
public record ExplorePlanSummaryResponse(
        Long totalBudget,
        Integer peopleCount,
        Integer desiredStops,
        Long estimatedTotalCost,
        Boolean lowBalance,
        String balanceMessage,
        Long suggestedDefaultBudget) {
}
