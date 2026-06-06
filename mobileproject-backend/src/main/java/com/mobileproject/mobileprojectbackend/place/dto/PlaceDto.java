package com.mobileproject.mobileprojectbackend.place.dto;

/**
 * DTO trả về thông tin chi tiết một địa điểm cho client.
 *
 * @param id                 ID duy nhất
 * @param name               tên địa điểm
 * @param address            địa chỉ
 * @param district           quận/huyện
 * @param category           phân loại
 * @param mealType           loại bữa ăn
 * @param rating             điểm đánh giá (0-5)
 * @param reviewCount        số lượt đánh giá
 * @param openHours          giờ mở cửa
 * @param priceRange         khoảng giá
 * @param imageUrl           URL hình ảnh
 * @param pinned             có được ghím hay không
 * @param googleMapsUrl      liên kết Google Maps
 * @param lat                vĩ độ
 * @param lng                kinh độ
 * @param province           tỉnh/thành phố
 * @param normalizedDistrict quận/huyện chuẩn hóa
 * @param effectiveTag       tag phân loại hiệu quả
 * @param food               là quán ăn
 * @param drink              là quán nước
 * @param distanceKm         khoảng cách từ người dùng (km), null nếu không có tọa độ
 */
public record PlaceDto(
                String id,
                String name,
                String address,
                String district,
                String category,
                String mealType,
                Double rating,
                Integer reviewCount,
                String openHours,
                String priceRange,
                String imageUrl,
                Boolean pinned,
                String googleMapsUrl,
                Double lat,
                Double lng,
                String province,
                String normalizedDistrict,
                String effectiveTag,
                Boolean food,
                Boolean drink,
                Double distanceKm) {
}