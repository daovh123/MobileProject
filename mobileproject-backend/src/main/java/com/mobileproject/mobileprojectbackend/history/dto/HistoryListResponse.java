package com.mobileproject.mobileprojectbackend.history.dto;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;

import java.util.List;

/**
 * Response danh sách lịch sử xem địa điểm.
 *
 * @param success thành công
 * @param message thông báo
 * @param places  danh sách địa điểm đã xem (mới nhất trước)
 */
public record HistoryListResponse(
        boolean success,
        String message,
        List<PlaceDto> places) {
}
