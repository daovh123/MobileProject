package com.mobileproject.mobileprojectbackend.moment.reaction.dto;

import jakarta.validation.constraints.NotBlank;

public record MomentReactionRequest(
        @NotBlank String reaction) {
}
