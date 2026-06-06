package com.mobileproject.mobileprojectbackend.place.dto;

/**
 * Request tìm kiếm địa điểm với các tiêu chí lọc và phân trang.
 *
 * @param q        từ khóa tìm kiếm
 * @param province tỉnh/thành phố
 * @param district quận/huyện
 * @param type     loại: "food", "drink", "all"
 * @param minRating điểm đánh giá tối thiểu (0-5)
 * @param nearLat  vĩ độ hiện tại (tính khoảng cách)
 * @param nearLng  kinh độ hiện tại
 * @param radiusKm bán kính tìm kiếm (km)
 * @param sort     cách sắp xếp: "trending", "rating", "ratingAsc", "ratingMix", "distance"
 * @param page     trang (0-based)
 * @param size     số kết quả/trang (1-100)
 */
public record PlaceSearchRequest(
                String q,
                String province,
                String district,
                String type,
                Double minRating,
                Double nearLat,
                Double nearLng,
                Double radiusKm,
                String sort,
                int page,
                int size) {
}