package com.mobileproject.mobileprojectbackend.map.dto;

public record MapLastLocationsResponse(
        boolean success,
        String message,
        String coupleId,
        LocationDto myLocation,
        LocationDto partnerLocation
) {
    public static MapLastLocationsResponse success(
            String message,
            String coupleId,
            LocationDto myLocation,
            LocationDto partnerLocation
    ) {
        return new MapLastLocationsResponse(true, message, coupleId, myLocation, partnerLocation);
    }
}
