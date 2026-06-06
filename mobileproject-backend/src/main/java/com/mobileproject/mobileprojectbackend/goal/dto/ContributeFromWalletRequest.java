package com.mobileproject.mobileprojectbackend.goal.dto;

/**
 * DTO request để đóng góp tiền vào mục tiêu từ ví chung.
 *
 * @param amount số tiền đóng góp (VND, phải &gt; 0)
 * @param note   ghi chú
 */
public record ContributeFromWalletRequest(
        Long amount,
        String note
) {
}