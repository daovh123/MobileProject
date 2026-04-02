package com.mobileproject.mobileprojectbackend.place.dto;

import java.util.List;

public record PlaceFilterOptionsResponse(
        List<String> districts,
        List<String> provinces
) {
}