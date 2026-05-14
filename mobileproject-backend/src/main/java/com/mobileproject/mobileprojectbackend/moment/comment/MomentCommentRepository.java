package com.mobileproject.mobileprojectbackend.moment.comment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MomentCommentRepository extends MongoRepository<MomentComment, String> {

    List<MomentComment> findByMomentIdOrderByCreatedAtAsc(String momentId);

    long countByMomentId(String momentId);
}
