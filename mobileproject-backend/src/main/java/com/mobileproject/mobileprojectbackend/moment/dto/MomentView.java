package com.mobileproject.mobileprojectbackend.moment.dto;

import java.time.Instant;
import java.util.List;

public record MomentView(
        String id,
        String coupleId,
        String title,
        String imageUrl,
        Instant createdAt,
        int reactionsCount,
        int commentsCount,
        String viewerReaction,
        List<MomentReactionSummary> reactions) {
}
