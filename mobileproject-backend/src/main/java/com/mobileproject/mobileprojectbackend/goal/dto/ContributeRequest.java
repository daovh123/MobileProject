package com.mobileproject.mobileprojectbackend.goal.dto;

public record ContributeRequest(
        Long amount,
        String contributorId,
        String note
) {
}