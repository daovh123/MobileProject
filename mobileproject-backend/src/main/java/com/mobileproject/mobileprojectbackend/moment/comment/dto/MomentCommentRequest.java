package com.mobileproject.mobileprojectbackend.moment.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MomentCommentRequest(
        @NotBlank @Size(max = 300) String content) {
}
