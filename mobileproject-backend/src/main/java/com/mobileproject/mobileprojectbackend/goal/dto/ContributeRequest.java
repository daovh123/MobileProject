package com.mobileproject.mobileprojectbackend.goal.dto;

/**
 * DTO request để đóng góp tiền trực tiếp vào mục tiêu (không trừ ví).
 *
 * @param amount        số tiền đóng góp (VND, phải &gt; 0)
 * @param contributorId ID người đóng góp
 * @param note          ghi chú
 */
public record ContributeRequest(
        Long amount,
        String contributorId,
        String note
) {
}