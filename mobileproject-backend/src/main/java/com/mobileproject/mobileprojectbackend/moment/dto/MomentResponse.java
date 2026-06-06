package com.mobileproject.mobileprojectbackend.moment.dto;

/**
 * Response khi tạo/thao tác với kỷ niệm.
 *
 * @param success thành công hay không
 * @param message thông báo
 * @param moment  thông tin kỷ niệm (null nếu lỗi)
 */
public record MomentResponse(
        boolean success,
        String message,
        MomentView moment) {
}
