package com.mobileproject.mobileprojectbackend.moment.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request thêm bình luận cho kỷ niệm.
 *
 * @param content nội dung bình luận (tối đa 300 ký tự, không được trống)
 */
public record MomentCommentRequest(
        @NotBlank @Size(max = 300) String content) {
}
