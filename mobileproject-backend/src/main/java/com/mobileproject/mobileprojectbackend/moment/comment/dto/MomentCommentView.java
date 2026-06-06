package com.mobileproject.mobileprojectbackend.moment.comment.dto;

import java.time.Instant;

/**
 * DTO hiển thị thông tin bình luận cho client.
 *
 * @param id             ID bình luận
 * @param momentId       ID kỷ niệm
 * @param authorUsername tên người bình luận
 * @param content        nội dung
 * @param createdAt      thời điểm tạo
 */
public record MomentCommentView(
        String id,
        String momentId,
        String authorUsername,
        String content,
        Instant createdAt) {
}
