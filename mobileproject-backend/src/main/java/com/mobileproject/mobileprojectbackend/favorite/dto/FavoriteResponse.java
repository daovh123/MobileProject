package com.mobileproject.mobileprojectbackend.favorite.dto;

import java.time.Instant;

/**
 * Response thao tác yêu thích.
 *
 * @param success    thành công
 * @param message    thông báo ("Added to favorites" / "Removed from favorites")
 * @param favoriteId ID bản ghi yêu thích (null nếu xóa)
 * @param placeId    ID địa điểm
 * @param createdAt  thời điểm tạo (null nếu xóa)
 */
public record FavoriteResponse(
        boolean success,
        String message,
        String favoriteId,
        String placeId,
        Instant createdAt) {
}
