package com.mobileproject.mobileprojectbackend.place.dto;

import java.util.List;

public record PlaceImageBackfillResponse(
        boolean success,
        String message,
        int scannedCount,
        int totalGpsCsCount,
        int replacedCount,
        boolean dryRun,
        List<String> sampleUpdatedPlaceIds
) {
}
