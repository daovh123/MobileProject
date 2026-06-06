package com.mobileproject.mobileprojectbackend.place.dto;

/**
 * Một điểm dừng trong kế hoạch khám phá.
 *
 * @param stopOrder      thứ tự điểm dừng (1-based)
 * @param experienceType loại trải nghiệm: "food" hoặc "drink"
 * @param estimatedCost  chi phí ước tính (VND)
 * @param reason         lý do gợi ý (ví dụ: "Đa dạng hóa lịch trình • Hợp budget")
 * @param place          thông tin địa điểm
 */
public record ExplorePlanItemResponse(
        int stopOrder,
        String experienceType,
        Long estimatedCost,
        String reason,
        PlaceDto place) {
}
