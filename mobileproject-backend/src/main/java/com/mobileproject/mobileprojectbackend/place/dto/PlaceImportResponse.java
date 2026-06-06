package com.mobileproject.mobileprojectbackend.place.dto;

/**
 * Kết quả import dữ liệu địa điểm.
 *
 * @param success          import thành công hay không
 * @param message          thông báo mô tả
 * @param importedCount    số địa điểm đã import
 * @param totalInDatabase  tổng số địa điểm trong DB sau import
 * @param sourceFile       đường dẫn file nguồn
 */
public record PlaceImportResponse(
        boolean success,
        String message,
        long importedCount,
        long totalInDatabase,
        String sourceFile
) {
}