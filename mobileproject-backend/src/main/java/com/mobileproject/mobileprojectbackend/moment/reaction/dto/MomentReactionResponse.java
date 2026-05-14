package com.mobileproject.mobileprojectbackend.moment.reaction.dto;

import com.mobileproject.mobileprojectbackend.moment.dto.MomentReactionSummary;

import java.util.List;

public record MomentReactionResponse(
        String momentId,
        String viewerReaction,
        int reactionsCount,
        List<MomentReactionSummary> reactions) {
}
