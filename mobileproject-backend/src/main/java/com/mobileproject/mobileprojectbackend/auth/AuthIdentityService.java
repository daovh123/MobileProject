package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service xác định danh tính người dùng hiện tại từ header Authorization.
 *
 * <p>Được sử dụng bởi các controller/service cần lấy thông tin người dùng đã xác thực.
 * Quy trình:</p>
 * <ol>
 *   <li>Giải mã JWT token từ header để lấy username</li>
 *   <li>Tìm người dùng từ cache (Redis → in-memory → MongoDB)</li>
 *   <li>Ném {@link ResponseStatusException} HTTP 401 nếu token không hợp lệ hoặc user không tồn tại</li>
 * </ol>
 */
@Service
public class AuthIdentityService {

    private final AuthTokenService authTokenService;
    private final AuthUserCacheService authUserCacheService;

    public AuthIdentityService(AuthTokenService authTokenService, AuthUserCacheService authUserCacheService) {
        this.authTokenService = authTokenService;
        this.authUserCacheService = authUserCacheService;
    }

    /**
     * Lấy người dùng hiện tại từ token. Ném HTTP 401 nếu không xác thực được.
     *
     * @param authorizationHeader header Authorization chứa Bearer token
     * @return đối tượng {@link AuthUser} của người dùng hiện tại
     * @throws ResponseStatusException HTTP 401 nếu token không hợp lệ hoặc user không tồn tại
     */
    public AuthUser requireCurrentUser(String authorizationHeader) {
        String username = authTokenService.resolveUsername(authorizationHeader)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid access token"));

        return authUserCacheService.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User session not found"));
    }
}
