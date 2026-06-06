package com.mobileproject.mobileprojectbackend.moment.reaction.dto;

import com.mobileproject.mobileprojectbackend.moment.dto.MomentReactionSummary;

import java.util.List;

/**
 * Response tổng hợp reaction của một kỷ niệm.
 *
 * @param momentId       ID kỷ niệm
 * @param viewerReaction reaction hiện tại của người xem (null nếu chưa reaction)
 * @param reactionsCount tổng số reaction
 * @param reactions      tổng hợp theo loại reaction
 */
public record MomentReactionResponse(
        String momentId,
        String viewerReaction,
        int reactionsCount,
        List<MomentReactionSummary> reactions) {
}
