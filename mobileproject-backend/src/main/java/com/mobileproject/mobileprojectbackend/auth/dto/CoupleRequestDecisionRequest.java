package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Request DTO cho API quyết định yêu cầu ghép đôi.
 *
 * @param accept {@code true} để chấp nhận, {@code false} để từ chối
 */
public record CoupleRequestDecisionRequest(
        boolean accept
) {
}
