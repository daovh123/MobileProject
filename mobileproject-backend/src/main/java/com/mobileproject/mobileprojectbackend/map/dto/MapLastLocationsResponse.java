package com.mobileproject.mobileprojectbackend.map.dto;

/**
 * Response chứa vị trí cuối cùng của cả hai user trong couple.
 *
 * @param success         thành công
 * @param message         thông báo
 * @param coupleId        ID couple
 * @param myLocation      vị trí của user hiện tại
 * @param partnerLocation vị trí của partner
 */
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
