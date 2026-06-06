package com.mobileproject.mobileprojectbackend.place.dto;

import java.util.List;

/**
 * Danh sách tùy chọn cho bộ lọc địa điểm trên UI.
 *
 * @param districts danh sách quận/huyện
 * @param provinces danh sách tỉnh/thành phố
 */
public record PlaceFilterOptionsResponse(
        List<String> districts,
        List<String> provinces
) {
}