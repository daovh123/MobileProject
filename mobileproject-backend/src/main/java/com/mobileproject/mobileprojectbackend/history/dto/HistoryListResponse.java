package com.mobileproject.mobileprojectbackend.history.dto;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;

import java.util.List;

public record HistoryListResponse(
        boolean success,
        String message,
        List<PlaceDto> places) {
}
