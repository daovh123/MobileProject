package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.AuthResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.LoginRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.LogoutResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

/**
 * Service xử lý nghiệp vụ xác thực: đăng ký, đăng nhập, đăng xuất.
 *
 * <p>Nghiệp vụ chính:</p>
 * <ul>
 *   <li><b>Đăng ký</b> - Kiểm tra trùng username/email, mã hóa mật khẩu bằng BCrypt,
 *       lưu vào MongoDB và cache, phát hành JWT token</li>
 *   <li><b>Đăng nhập</b> - Xác thực bằng username hoặc email, so khớp mật khẩu BCrypt,
 *       phát hành JWT token</li>
 *   <li><b>Đăng xuất</b> - Thu hồi (revoke) JWT token hiện tại</li>
 * </ul>
 */
@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final AuthUserCacheService authUserCacheService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AuthService(AuthUserRepository authUserRepository,
            AuthUserCacheService authUserCacheService,
            PasswordEncoder passwordEncoder,
            AuthTokenService authTokenService) {
        this.authUserRepository = authUserRepository;
        this.authUserCacheService = authUserCacheService;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    /**
     * Đăng ký người dùng mới.
     * Kiểm tra trùng username/email, mã hóa mật khẩu, lưu DB, phát hành token.
     *
     * @param request thông tin đăng ký (username, email, password)
     * @return AuthResponse chứa token nếu thành công, hoặc thông báo lỗi
     */
    public AuthResponse register(RegisterRequest request) {
        String usernameKey = normalize(request.username());
        String emailKey = normalize(request.email());

        if (authUserRepository.existsByUsername(usernameKey)) {
            return AuthResponse.failure("Username already exists");
        }
        if (authUserRepository.existsByEmail(emailKey)) {
            return AuthResponse.failure("Email already exists");
        }

        AuthUser user = new AuthUser(
                usernameKey,
                emailKey,
                passwordEncoder.encode(request.password()),
                Instant.now().toString());

        AuthUser savedUser = authUserCacheService.save(user);

        return AuthResponse.success(
                "Register successful",
                issueDevToken(usernameKey),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.isProfileCompleted(),
                isCoupleConnected(savedUser));
    }

    /**
     * Đăng nhập bằng username hoặc email.
     * Tìm user theo username trước, sau đó email, rồi so khớp mật khẩu BCrypt.
     *
     * @param request thông tin đăng nhập (usernameOrEmail, password)
     * @return AuthResponse chứa token nếu thành công, hoặc thông báo lỗi
     */
    public AuthResponse login(LoginRequest request) {
        String loginKey = normalize(request.usernameOrEmail());

        Optional<AuthUser> userOptional = authUserCacheService.findByUsername(loginKey);
        if (userOptional.isEmpty()) {
            userOptional = authUserCacheService.findByEmail(loginKey);
        }

        if (userOptional.isEmpty()
                || !passwordEncoder.matches(request.password(), userOptional.get().getPasswordHash())) {
            return AuthResponse.failure("Invalid username/email or password");
        }

        AuthUser user = userOptional.get();

        return AuthResponse.success(
                "Login successful",
                issueDevToken(user.getUsername()),
                user.getUsername(),
                user.getEmail(),
                user.isProfileCompleted(),
                isCoupleConnected(user));
    }

    /**
     * Đăng xuất bằng cách thu hồi token hiện tại.
     *
     * @param authorizationHeader header Authorization chứa token cần thu hồi
     * @return LogoutResponse trạng thái đăng xuất
     */
    public LogoutResponse logout(String authorizationHeader) {
        boolean revoked = authTokenService.revokeToken(authorizationHeader);
        if (!revoked) {
            return LogoutResponse.failure("Missing or invalid access token");
        }
        return LogoutResponse.success("Logout successful");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String issueDevToken(String username) {
        return authTokenService.issueToken(username);
    }

    private boolean isCoupleConnected(AuthUser user) {
        return user.getPartnerUserId() != null && !user.getPartnerUserId().isBlank();
    }
}
