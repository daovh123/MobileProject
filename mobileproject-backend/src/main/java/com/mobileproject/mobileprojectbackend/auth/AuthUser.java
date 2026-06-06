package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

/**
 * Entity đại diện cho người dùng trong hệ thống, ánh xạ tới collection {@code users} trên MongoDB.
 *
 * <p>Chứa thông tin cá nhân, thông tin xác thực, và trạng thái ghép đôi của người dùng.</p>
 *
 * <p>Thông tin xác thực:</p>
 * <ul>
 *   <li>{@code username} - tên đăng nhập (duy nhất, viết thường)</li>
 *   <li>{@code email} - địa chỉ email (duy nhất, viết thường)</li>
 *   <li>{@code passwordHash} - mật khẩu đã mã hóa bằng BCrypt</li>
 * </ul>
 *
 * <p>Thông tin ghép đôi:</p>
 * <ul>
 *   <li>{@code partnerUserId} - ID của đối tác đã ghép đôi (null nếu chưa ghép)</li>
 *   <li>{@code avatarFrameId} - ID khung viền avatar hiện tại</li>
 * </ul>
 */
@Getter
@Setter
@Document(collection = "users")
public class AuthUser {

    /** ID duy nhất của người dùng (MongoDB ObjectId). */
    @Id
    private String id;
    /** Tên đăng nhập, viết thường, duy nhất trong hệ thống. */
    private String username;
    /** Địa chỉ email, viết thường, duy nhất trong hệ thống. */
    private String email;
    /** Mật khẩu đã mã hóa bằng BCrypt. */
    private String passwordHash;
    /** Thời điểm tạo tài khoản (ISO-8601 instant). */
    private String createdAt;
    /** Họ và tên đầy đủ. */
    private String fullName;
    /** Biệt danh (nickname), có thể null. */
    private String nickName;
    /** Ngày sinh (ISO-8601 date, ví dụ: 2000-01-15). */
    private String birthDate;
    /** Giới tính: MALE, FEMALE, hoặc OTHER. */
    private String gender;
    /** Số điện thoại, có thể null. */
    private String phoneNumber;
    /** Cờ đánh dấu người dùng đã hoàn thành hồ sơ cá nhân. */
    private boolean profileCompleted;
    /** ID của đối tác đã ghép đôi (null nếu chưa ghép). */
    private String partnerUserId;
    /** URL avatar dạng Base64 Data URL (data:image/...;base64,...). */
    private String avatarUrl;
    /** ID của khung viền avatar (tham chiếu tới {@link AvatarFrame}). */
    private String avatarFrameId;

    /** Constructor mặc định yêu cầu bởi MongoDB driver. */
    public AuthUser() {
    }

    /**
     * Constructor tạo người dùng mới khi đăng ký.
     *
     * @param username     tên đăng nhập
     * @param email        địa chỉ email
     * @param passwordHash mật khẩu đã mã hóa
     * @param createdAt    thời điểm tạo (ISO-8601)
     */
    public AuthUser(String username, String email, String passwordHash, String createdAt) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.profileCompleted = false;
    }

}
