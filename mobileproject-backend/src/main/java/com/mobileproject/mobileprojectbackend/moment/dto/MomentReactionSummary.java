package com.mobileproject.mobileprojectbackend.moment.dto;

/**
 * Tóm tắt số lượng reaction theo loại.
 *
 * @param reaction loại reaction (HEART, FIRE, WOW, LAUGH)
 * @param count    số lượng
 */
public record MomentReactionSummary(
        String reaction,
        int count) {
}
