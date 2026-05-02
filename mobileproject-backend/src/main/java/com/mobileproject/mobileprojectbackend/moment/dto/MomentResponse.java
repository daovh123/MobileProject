package com.mobileproject.mobileprojectbackend.moment.dto;

import com.mobileproject.mobileprojectbackend.moment.Moment;

public record MomentResponse(
    boolean success,
    String message,
    Moment moment
) {}
