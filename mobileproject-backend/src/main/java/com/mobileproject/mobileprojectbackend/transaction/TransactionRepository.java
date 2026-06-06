package com.mobileproject.mobileprojectbackend.transaction;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository MongoDB cho {@link Transaction}.
 * Cung cấp các phương thức truy vấn giao dịch theo cặp đôi.
 */
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    /**
     * Tìm tất cả giao dịch của một cặp đôi, sắp xếp theo thời gian tạo giảm dần (mới nhất trước).
     *
     * @param coupleId ID của cặp đôi
     * @return danh sách giao dịch sắp xếp theo {@code createdAt} giảm dần
     */
    List<Transaction> findByCoupleIdOrderByCreatedAtDesc(String coupleId);

    /**
     * Tìm tất cả giao dịch trong hệ thống (dùng cho debug).
     *
     * @return danh sách tất cả giao dịch
     */
    List<Transaction> findAll();
}