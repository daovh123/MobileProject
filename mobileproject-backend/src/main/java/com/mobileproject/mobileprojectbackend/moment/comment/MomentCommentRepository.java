package com.mobileproject.mobileprojectbackend.moment.comment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository truy vấn collection {@code moment_comments}.
 */
public interface MomentCommentRepository extends MongoRepository<MomentComment, String> {

    /** Tìm tất cả bình luận của một moment, sắp xếp cũ nhất trước. */
    List<MomentComment> findByMomentIdOrderByCreatedAtAsc(String momentId);

    /** Đếm tổng số bình luận của một moment. */
    long countByMomentId(String momentId);
}
