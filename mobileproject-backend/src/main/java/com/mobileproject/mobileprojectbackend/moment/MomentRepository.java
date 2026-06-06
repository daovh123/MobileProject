package com.mobileproject.mobileprojectbackend.moment;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository truy vấn collection {@code moments}.
 * Tìm kỷ niệm theo coupleId, sắp xếp mới nhất trước.
 */
public interface MomentRepository extends MongoRepository<Moment, String> {
    /**
     * Tìm tất cả kỷ niệm của một cặp đôi, sắp xếp theo thời gian tạo giảm dần.
     */
    List<Moment> findByCoupleIdOrderByCreatedAtDesc(String coupleId);
}
