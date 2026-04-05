package com.mobileproject.mobileprojectbackend.place.dto;

public record PlaceImportResponse(
        boolean success,
        String message,
        long importedCount,
        long totalInDatabase,
        String sourceFile
) {
}