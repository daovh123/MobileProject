package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository MongoDB cho entity {@link AuthUser}.
 *
 * <p>Cung cấp các phương thức truy vấn người dùng theo username, email,
 * và các thao tác kiểm tra sự tồn tại / xóa.</p>
 */
public interface AuthUserRepository extends MongoRepository<AuthUser, String> {

    /**
     * Kiểm tra username đã tồn tại trong hệ thống chưa.
     *
     * @param username tên đăng nhập cần kiểm tra
     * @return {@code true} nếu username đã tồn tại
     */
    boolean existsByUsername(String username);

    /**
     * Kiểm tra email đã tồn tại trong hệ thống chưa.
     *
     * @param email địa chỉ email cần kiểm tra
     * @return {@code true} nếu email đã tồn tại
     */
    boolean existsByEmail(String email);

    /**
     * Tìm người dùng theo tên đăng nhập.
     *
     * @param username tên đăng nhập
     * @return Optional chứa người dùng nếu tìm thấy
     */
    Optional<AuthUser> findByUsername(String username);

    /**
     * Tìm người dùng theo địa chỉ email.
     *
     * @param email địa chỉ email
     * @return Optional chứa người dùng nếu tìm thấy
     */
    Optional<AuthUser> findByEmail(String email);

    /**
     * Xóa người dùng theo tên đăng nhập.
     *
     * @param username tên đăng nhập của người dùng cần xóa
     * @return số lượng bản ghi đã xóa
     */
    long deleteByUsername(String username);
}
