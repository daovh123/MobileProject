package com.mobileproject.mobileprojectbackend.moment.dto;

import java.time.Instant;
import java.util.List;

/**
 * DTO hiển thị kỷ niệm đầy đủ cho client, bao gồm reaction summary và comment count.
 *
 * @param id             ID kỷ niệm
 * @param coupleId       ID couple
 * @param title          tiêu đề
 * @param imageUrl       URL hình ảnh
 * @param createdAt      thời điểm tạo
 * @param reactionsCount tổng số reaction
 * @param commentsCount  tổng số bình luận
 * @param viewerReaction reaction hiện tại của người xem
 * @param reactions      tổng hợp reaction theo loại
 */
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
