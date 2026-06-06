package com.mobileproject.mobileprojectbackend.place.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request tạo kế hoạch khám phá (explore plan).
 *
 * @param randomSeed     seed ngẫu nhiên (đảm bảo kết quả ổn định)
 * @param q              từ khóa tìm kiếm
 * @param province       tỉnh/thành phố
 * @param district       quận/huyện
 * @param type           loại: food, drink, all
 * @param minRating      điểm đánh giá tối thiểu
 * @param nearLat        vĩ độ hiện tại
 * @param nearLng        kinh độ hiện tại
 * @param radiusKm       bán kính tìm kiếm
 * @param budget         ngân sách (VND, tối thiểu 1000)
 * @param peopleCount    số người (1-20, mặc định 2)
 * @param desiredStops   số điểm dừng mong muốn (1-5, mặc định 2)
 * @param excludePlaceIds danh sách ID địa điểm cần loại trừ
 * @param viewedPlaceIds  ID đã xem (cũng bị loại trừ)
 * @param gonePlaceIds    ID đã đi (cũng bị loại trừ)
 * @param sentPlaceIds    ID đã gửi (cũng bị loại trừ)
 * @param recentKeywords  từ khóa tìm kiếm gần đây (ưu tiên gợi ý liên quan)
 */
public record ExplorePlanRequest(
        Long randomSeed,
        String q,
        String province,
        String district,
        String type,
        Double minRating,
        Double nearLat,
        Double nearLng,
        Double radiusKm,
        @NotNull @Min(1_000) Long budget,
        @Min(1) @Max(20) Integer peopleCount,
        @Min(1) @Max(5) Integer desiredStops,
        List<String> excludePlaceIds,
        List<String> viewedPlaceIds,
        List<String> gonePlaceIds,
        List<String> sentPlaceIds,
        List<String> recentKeywords) {
}
