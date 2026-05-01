package com.mobileproject.mobileprojectbackend.moment.dto;

public record MomentRequest(
    String coupleId,
    String title,
    String base64Image
) {}
