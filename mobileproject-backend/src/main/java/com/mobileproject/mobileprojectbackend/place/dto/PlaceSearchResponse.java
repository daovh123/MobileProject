package com.mobileproject.mobileprojectbackend.place.dto;

import java.util.List;

/**
 * Response phân trang kết quả tìm kiếm địa điểm.
 *
 * @param items danh sách địa điểm trên trang hiện tại
 * @param total tổng số kết quả
 * @param page  trang hiện tại (0-based)
 * @param size  số kết quả mỗi trang
 */
public record PlaceSearchResponse(
        List<PlaceDto> items,
        long total,
        int page,
        int size
) {
}