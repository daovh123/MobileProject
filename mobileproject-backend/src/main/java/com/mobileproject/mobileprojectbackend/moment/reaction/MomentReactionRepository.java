package com.mobileproject.mobileprojectbackend.moment.reaction;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy vấn collection {@code moment_reactions}.
 */
public interface MomentReactionRepository extends MongoRepository<MomentReaction, String> {

    /** Tìm tất cả reaction của một moment, sắp xếp mới nhất trước. */
    List<MomentReaction> findByMomentIdOrderByCreatedAtDesc(String momentId);

    /** Tìm reaction hiện có của user cho một moment. */
    Optional<MomentReaction> findFirstByMomentIdAndUserId(String momentId, String userId);

    /** Đếm tổng số reaction của một moment. */
    long countByMomentId(String momentId);

    /** Xóa reaction của user cho một moment. */
    long deleteByMomentIdAndUserId(String momentId, String userId);
}
