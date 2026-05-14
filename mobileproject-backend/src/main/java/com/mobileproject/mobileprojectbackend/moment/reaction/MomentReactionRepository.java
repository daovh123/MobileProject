package com.mobileproject.mobileprojectbackend.moment.reaction;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MomentReactionRepository extends MongoRepository<MomentReaction, String> {

    List<MomentReaction> findByMomentIdOrderByCreatedAtDesc(String momentId);

    Optional<MomentReaction> findFirstByMomentIdAndUserId(String momentId, String userId);

    long countByMomentId(String momentId);

    long deleteByMomentIdAndUserId(String momentId, String userId);
}
