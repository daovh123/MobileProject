package com.mobileproject.mobileprojectbackend.moment.reaction;

import com.mobileproject.mobileprojectbackend.moment.dto.MomentReactionSummary;
import com.mobileproject.mobileprojectbackend.moment.reaction.dto.MomentReactionResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service xử lý reaction cho kỷ niệm.
 *
 * <p><strong>Business logic:</strong></p>
 * <ul>
 *   <li>Nếu user đã reaction cùng loại → toggle (xóa reaction)</li>
 *   <li>Nếu user đã reaction khác loại → cập nhật reaction mới</li>
 *   <li>Nếu user chưa reaction → tạo mới</li>
 *   <li>Chỉ cho phép các reaction hợp lệ: HEART, FIRE, WOW, LAUGH</li>
 * </ul>
 */
@Service
public class MomentReactionService {

    private static final Set<String> ALLOWED_REACTIONS = Arrays.stream(MomentReactionType.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private final MomentReactionRepository momentReactionRepository;

    public MomentReactionService(MomentReactionRepository momentReactionRepository) {
        this.momentReactionRepository = momentReactionRepository;
    }

    /**
     * Xử lý reaction: toggle (nếu cùng loại), update (nếu khác loại), hoặc tạo mới.
     *
     * @param momentId ID kỷ niệm
     * @param coupleId ID couple
     * @param userId   ID người reaction
     * @param reaction loại reaction (HEART, FIRE, WOW, LAUGH)
     * @return response chứa reaction hiện tại và tổng hợp reaction
     */
    public MomentReactionResponse react(
            String momentId,
            String coupleId,
            String userId,
            String reaction) {
        String normalizedReaction = normalizeReaction(reaction);

        MomentReaction existing = momentReactionRepository
                .findFirstByMomentIdAndUserId(momentId, userId)
                .orElse(null);

        String viewerReaction;
        if (existing != null) {
            if (normalizedReaction.equals(existing.getReaction())) {
                momentReactionRepository.deleteById(existing.getId());
                viewerReaction = null;
            } else {
                existing.setReaction(normalizedReaction);
                momentReactionRepository.save(existing);
                viewerReaction = normalizedReaction;
            }
        } else {
            MomentReaction newReaction = new MomentReaction(momentId, coupleId, userId, normalizedReaction);
            momentReactionRepository.save(newReaction);
            viewerReaction = normalizedReaction;
        }

        List<MomentReaction> reactions = momentReactionRepository.findByMomentIdOrderByCreatedAtDesc(momentId);
        return toResponse(momentId, viewerReaction, reactions, userId);
    }

    /**
     * Lấy tổng hợp reaction của một moment.
     */
    public MomentReactionResponse getReactionSummary(String momentId, String viewerUserId) {
        List<MomentReaction> reactions = momentReactionRepository.findByMomentIdOrderByCreatedAtDesc(momentId);
        return toResponse(momentId, null, reactions, viewerUserId);
    }

    private MomentReactionResponse toResponse(
            String momentId,
            String viewerReaction,
            List<MomentReaction> reactions,
            String viewerUserId) {
        Map<String, Long> counts = reactions.stream()
                .collect(Collectors.groupingBy(MomentReaction::getReaction, Collectors.counting()));

        List<MomentReactionSummary> summary = counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new MomentReactionSummary(entry.getKey(), entry.getValue().intValue()))
                .toList();

        String resolvedViewerReaction = viewerReaction;
        if (resolvedViewerReaction == null && viewerUserId != null) {
            resolvedViewerReaction = reactions.stream()
                    .filter(reaction -> viewerUserId.equals(reaction.getUserId()))
                    .map(MomentReaction::getReaction)
                    .findFirst()
                    .orElse(null);
        }

        return new MomentReactionResponse(
                momentId,
                resolvedViewerReaction,
                reactions.size(),
                summary);
    }

    private String normalizeReaction(String reaction) {
        if (reaction == null) {
            throw new IllegalArgumentException("Reaction is required");
        }
        String normalized = reaction.trim().toUpperCase(Locale.US);
        if (normalized.isBlank() || !ALLOWED_REACTIONS.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported reaction");
        }
        return normalized;
    }
}
