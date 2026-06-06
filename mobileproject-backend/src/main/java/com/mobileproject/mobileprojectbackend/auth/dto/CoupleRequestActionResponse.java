package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Response DTO cho các API thao tác với yêu cầu ghép đôi (gửi, chấp nhận, từ chối).
 *
 * @param success           trạng thái thành công/thất bại
 * @param message           thông báo mô tả kết quả
 * @param requestId         ID của yêu cầu ghép đôi
 * @param status            trạng thái yêu cầu (PENDING/ACCEPTED/REJECTED)
 * @param requesterUsername  tên đăng nhập người gửi
 * @param recipientUsername  tên đăng nhập người nhận
 */
public record CoupleRequestActionResponse(
        boolean success,
        String message,
        String requestId,
        String status,
        String requesterUsername,
        String recipientUsername
) {
    public static CoupleRequestActionResponse success(String message,
                                                      String requestId,
                                                      String status,
                                                      String requesterUsername,
                                                      String recipientUsername) {
        return new CoupleRequestActionResponse(
                true,
                message,
                requestId,
                status,
                requesterUsername,
                recipientUsername
        );
    }
}
