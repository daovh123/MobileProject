package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.AuthResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.LoginRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.LogoutResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST xử lý các endpoint xác thực: đăng ký, đăng nhập, đăng xuất.
 *
 * <p>Base path: {@code /api/auth}</p>
 * <p>Tất cả các endpoint đều công khai (permitAll trong SecurityConfig).</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Đăng ký tài khoản mới.
     *
     * <ul>
     *   <li>HTTP Method: {@code POST}</li>
     *   <li>Path: {@code /api/auth/register}</li>
     *   <li>Auth: Không yêu cầu</li>
     *   <li>Request body: {@link RegisterRequest} (username, email, password)</li>
     *   <li>Response: {@link AuthResponse} với token JWT</li>
     *   <li>HTTP 201: Đăng ký thành công</li>
     *   <li>HTTP 409: Username hoặc email đã tồn tại</li>
     * </ul>
     *
     * @param request thông tin đăng ký (đã validate)
     * @return ResponseEntity chứa AuthResponse
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        if (!response.success()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Đăng nhập bằng username hoặc email.
     *
     * <ul>
     *   <li>HTTP Method: {@code POST}</li>
     *   <li>Path: {@code /api/auth/login}</li>
     *   <li>Auth: Không yêu cầu</li>
     *   <li>Request body: {@link LoginRequest} (usernameOrEmail, password)</li>
     *   <li>Response: {@link AuthResponse} với token JWT</li>
     *   <li>HTTP 200: Đăng nhập thành công</li>
     *   <li>HTTP 401: Sai thông tin đăng nhập</li>
     * </ul>
     *
     * @param request thông tin đăng nhập (đã validate)
     * @return ResponseEntity chứa AuthResponse
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        if (!response.success()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Đăng xuất - thu hồi token hiện tại.
     *
     * <ul>
     *   <li>HTTP Method: {@code POST}</li>
     *   <li>Path: {@code /api/auth/logout}</li>
     *   <li>Auth: Yêu cầu Bearer token (tùy chọn header)</li>
     *   <li>Request header: {@code Authorization: Bearer <token>}</li>
     *   <li>Response: {@link LogoutResponse}</li>
     *   <li>HTTP 200: Đăng xuất thành công</li>
     *   <li>HTTP 401: Token không hợp lệ hoặc thiếu</li>
     * </ul>
     *
     * @param authorizationHeader header Authorization (có thể null)
     * @return ResponseEntity chứa LogoutResponse
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        LogoutResponse response = authService.logout(authorizationHeader);
        if (!response.success()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
