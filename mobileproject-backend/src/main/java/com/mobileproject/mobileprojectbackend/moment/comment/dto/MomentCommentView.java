package com.mobileproject.mobileprojectbackend.moment.comment.dto;

import java.time.Instant;

public record MomentCommentView(
        String id,
        String momentId,
        String authorUsername,
        String content,
        Instant createdAt) {
}
