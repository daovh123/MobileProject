package com.mobileproject.mobileprojectbackend.payout;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository MongoDB cho {@link PayoutRequest}.
 * Cung cấp các phương thức truy vấn yêu cầu rút tiền theo mã chuyển khoản và SePay ID.
 */
public interface PayoutRequestRepository extends MongoRepository<PayoutRequest, String> {
    /**
     * Tìm yêu cầu rút tiền theo mã chuyển khoản duy nhất.
     *
     * @param transferCode mã chuyển khoản
     * @return yêu cầu rút tiền nếu tồn tại
     */
    Optional<PayoutRequest> findByTransferCode(String transferCode);

    /**
     * Tìm yêu cầu rút tiền theo SePay transaction ID (dùng deduplicate webhook).
     *
     * @param sepayId ID giao dịch từ SePay
     * @return yêu cầu rút tiền nếu tồn tại
     */
    Optional<PayoutRequest> findBySepayId(Long sepayId);

    /**
     * Tìm yêu cầu rút tiền theo mã tham chiếu SePay.
     *
     * @param referenceCode mã tham chiếu
     * @return yêu cầu rút tiền nếu tồn tại
     */
    Optional<PayoutRequest> findByReferenceCode(String referenceCode);
}

