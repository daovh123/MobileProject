package com.mobileproject.mobileprojectbackend.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity đại diện cho yêu cầu ghép đôi, ánh xạ tới collection {@code couple_requests} trên MongoDB.
 *
 * <p>Luồng xử lý:</p>
 * <ol>
 *   <li>Người gửi tạo yêu cầu → status = PENDING</li>
 *   <li>Người nhận chấp nhận → status = ACCEPTED (liên kết 2 user)</li>
 *   <li>Hoặc người nhận từ chối → status = REJECTED</li>
 * </ol>
 */
@Getter
@Setter
@Document(collection = "couple_requests")
public class CoupleRequest {

    /** ID duy nhất của yêu cầu (MongoDB ObjectId). */
    @Id
    private String id;

    /** ID người dùng gửi yêu cầu. */
    private String requesterUserId;
    /** Tên đăng nhập của người gửi yêu cầu. */
    private String requesterUsername;
    /** Tên hiển thị của người gửi (nickname > fullName > username). */
    private String requesterDisplayName;

    /** ID người dùng nhận yêu cầu. */
    private String recipientUserId;
    /** Tên đăng nhập của người nhận yêu cầu. */
    private String recipientUsername;

    /** Trạng thái yêu cầu: PENDING, ACCEPTED, REJECTED. */
    private CoupleRequestStatus status;
    /** Thời điểm tạo yêu cầu (ISO-8601). */
    private String createdAt;
    /** Thời điểm cập nhật cuối (ISO-8601, dùng để sort). */
    private String updatedAt;
}
