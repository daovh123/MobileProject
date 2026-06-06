package com.mobileproject.mobileprojectbackend.place.dto;

import java.util.List;

/**
 * Thống kê tổng quan dữ liệu địa điểm.
 *
 * @param totalPlaces              tổng số địa điểm
 * @param totalFoodPlaces          số quán ăn
 * @param totalDrinkPlaces         số quán nước
 * @param totalPinnedPlaces        số địa điểm được ghím
 * @param totalPlacesWithCoordinates số địa điểm có tọa độ
 * @param topProvinces             top tỉnh/thành phố
 * @param topDistricts             top quận/huyện
 * @param topTags                  top tag phổ biến
 */
public record PlaceFeatureSummaryResponse(
        long totalPlaces,
        long totalFoodPlaces,
        long totalDrinkPlaces,
        long totalPinnedPlaces,
        long totalPlacesWithCoordinates,
        List<CountItem> topProvinces,
        List<CountItem> topDistricts,
        List<String> topTags
) {
    public record CountItem(String name, long count) {
    }
}