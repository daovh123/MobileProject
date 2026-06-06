package com.mobileproject.mobileprojectbackend.favorite.dto;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;

import java.util.List;

/**
 * Response danh sách địa điểm yêu thích.
 *
 * @param success thành công
 * @param message thông báo
 * @param places  danh sách địa điểm yêu thích (mới nhất trước)
 */
public record FavoriteListResponse(
        boolean success,
        String message,
        List<PlaceDto> places) {
}
