package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository MongoDB cho entity {@link CoupleRequest}.
 *
 * <p>Cung cấp các phương thức truy vấn yêu cầu ghép đôi theo người gửi/người nhận
 * và trạng thái, sắp xếp theo thời gian cập nhật giảm dần.</p>
 */
public interface CoupleRequestRepository extends MongoRepository<CoupleRequest, String> {

    /**
     * Tìm yêu cầu PENDING mới nhất của người gửi.
     *
     * @param requesterUserId ID người gửi
     * @param status          trạng thái yêu cầu (thường là PENDING)
     * @return Optional chứa CoupleRequest nếu tìm thấy
     */
    Optional<CoupleRequest> findFirstByRequesterUserIdAndStatusOrderByUpdatedAtDesc(
            String requesterUserId,
            CoupleRequestStatus status
    );

    /**
     * Tìm yêu cầu PENDING mới nhất dành cho người nhận.
     *
     * @param recipientUserId ID người nhận
     * @param status          trạng thái yêu cầu (thường là PENDING)
     * @return Optional chứa CoupleRequest nếu tìm thấy
     */
    Optional<CoupleRequest> findFirstByRecipientUserIdAndStatusOrderByUpdatedAtDesc(
            String recipientUserId,
            CoupleRequestStatus status
    );

    /**
     * Tìm yêu cầu gần nhất mà người dùng đã gửi (bất kỳ trạng thái nào).
     *
     * @param requesterUserId ID người gửi
     * @return Optional chứa CoupleRequest nếu tìm thấy
     */
    Optional<CoupleRequest> findFirstByRequesterUserIdOrderByUpdatedAtDesc(String requesterUserId);

    /**
     * Tìm yêu cầu ACCEPTED giữa 2 người dùng cụ thể (dùng để suy ngày bắt đầu).
     *
     * @param requesterUserId ID người gửi
     * @param recipientUserId ID người nhận
     * @param status          trạng thái (thường là ACCEPTED)
     * @return Optional chứa CoupleRequest nếu tìm thấy
     */
    Optional<CoupleRequest> findFirstByRequesterUserIdAndRecipientUserIdAndStatusOrderByUpdatedAtDesc(
            String requesterUserId,
            String recipientUserId,
            CoupleRequestStatus status
    );

    /**
     * Xóa tất cả yêu cầu theo tên đăng nhập người gửi.
     *
     * @param requesterUsername tên đăng nhập người gửi
     * @return số lượng bản ghi đã xóa
     */
    long deleteByRequesterUsername(String requesterUsername);

    /**
     * Xóa tất cả yêu cầu theo tên đăng nhập người nhận.
     *
     * @param recipientUsername tên đăng nhập người nhận
     * @return số lượng bản ghi đã xóa
     */
    long deleteByRecipientUsername(String recipientUsername);
}
