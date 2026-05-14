package com.mobileproject.mobileprojectbackend.moment.dto;

public record MomentResponse(
        boolean success,
        String message,
        MomentView moment) {
}
