package com.mobileproject.mobileprojectbackend.place.dto;

import java.util.List;

/**
 * Kết quả backfill ảnh GPS-CS.
 *
 * @param success              thao tác thành công
 * @param message              thông báo mô tả
 * @param scannedCount         số địa điểm đã quét
 * @param totalGpsCsCount      tổng số ảnh GPS-CS tìm thấy
 * @param replacedCount        số ảnh đã thay thế
 * @param dryRun               có phải dry-run hay không
 * @param sampleUpdatedPlaceIds mẫu ID địa điểm đã cập nhật (tối đa 20)
 */
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
