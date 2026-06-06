package com.mobileproject.mobileprojectbackend.topup;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository MongoDB cho {@link TopUpRequest}.
 * Cung cấp các phương thức truy vấn yêu cầu nạp tiền theo mã chuyển khoản và SePay ID.
 */
public interface TopUpRequestRepository extends MongoRepository<TopUpRequest, String> {

    /**
     * Tìm yêu cầu nạp tiền theo mã chuyển khoản duy nhất.
     *
     * @param transferCode mã chuyển khoản
     * @return yêu cầu nạp tiền nếu tồn tại
     */
    Optional<TopUpRequest> findByTransferCode(String transferCode);

    /**
     * Tìm yêu cầu nạp tiền theo SePay transaction ID (dùng để deduplicate webhook).
     *
     * @param sepayId ID giao dịch từ SePay
     * @return yêu cầu nạp tiền nếu tồn tại
     */
    Optional<TopUpRequest> findBySepayId(Long sepayId);

    /**
     * Tìm yêu cầu nạp tiền theo mã tham chiếu SePay (dùng để deduplicate webhook).
     *
     * @param referenceCode mã tham chiếu từ SePay
     * @return yêu cầu nạp tiền nếu tồn tại
     */
    Optional<TopUpRequest> findByReferenceCode(String referenceCode);
}
