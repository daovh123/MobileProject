package com.mobileproject.mobileprojectbackend.moment.reaction.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request thả reaction cho kỷ niệm.
 *
 * @param reaction loại reaction (HEART, FIRE, WOW, LAUGH)
 */
public record MomentReactionRequest(
        @NotBlank String reaction) {
}
