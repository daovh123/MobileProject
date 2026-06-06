package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.CoupleCodeResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestActionResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestCreateRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestDecisionRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.CouplePartnerProfileResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleStatusResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST quản lý ghép đôi (couple) giữa hai người dùng.
 *
 * <p>Base path: {@code /api/auth/couple}</p>
 * <p>Tất cả các endpoint đều yêu cầu xác thực (Bearer token).</p>
 */
@RestController
@RequestMapping("/api/auth/couple")
public class CoupleController {

    private final CoupleService coupleService;

    public CoupleController(CoupleService coupleService) {
        this.coupleService = coupleService;
    }

    /**
     * Tạo hoặc lấy mã ghép đôi của người dùng hiện tại.
     *
     * <ul>
     *   <li>HTTP Method: {@code POST}</li>
     *   <li>Path: {@code /api/auth/couple/code}</li>
     *   <li>Auth: Yêu cầu Bearer token</li>
     *   <li>Response: {@link CoupleCodeResponse} (myCode, myCodeExpiresAt)</li>
     *   <li>HTTP 200: Thành công</li>
     *   <li>HTTP 400: Profile chưa hoàn thành</li>
     *   <li>HTTP 409: Đã ghép đôi</li>
     * </ul>
     */
    @PostMapping("/code")
    public ResponseEntity<CoupleCodeResponse> generateMyCode(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(coupleService.generateMyCode(authorizationHeader));
    }

    /**
     * Lấy trạng thái ghép đôi tổng hợp.
     *
     * <ul>
     *   <li>HTTP Method: {@code GET}</li>
     *   <li>Path: {@code /api/auth/couple/status}</li>
     *   <li>Auth: Yêu cầu Bearer token</li>
     *   <li>Response: {@link CoupleStatusResponse}</li>
     *   <li>HTTP 200: Thành công</li>
     * </ul>
     */
    @GetMapping("/status")
    public ResponseEntity<CoupleStatusResponse> getStatus(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(coupleService.getStatus(authorizationHeader));
    }

    /**
     * Lấy thông tin hồ sơ của đối tác đã ghép đôi.
     *
     * <ul>
     *   <li>HTTP Method: {@code GET}</li>
     *   <li>Path: {@code /api/auth/couple/partner-profile}</li>
     *   <li>Auth: Yêu cầu Bearer token</li>
     *   <li>Response: {@link CouplePartnerProfileResponse}</li>
     *   <li>HTTP 200: Thành công</li>
     * </ul>
     */
    @GetMapping("/partner-profile")
    public ResponseEntity<CouplePartnerProfileResponse> getPartnerProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(coupleService.getPartnerProfile(authorizationHeader));
    }

    /**
     * Gửi yêu cầu ghép đôi mới bằng mã đối tác.
     *
     * <ul>
     *   <li>HTTP Method: {@code POST}</li>
     *   <li>Path: {@code /api/auth/couple/requests}</li>
     *   <li>Auth: Yêu cầu Bearer token</li>
     *   <li>Request body: {@link CoupleRequestCreateRequest} (partnerCode)</li>
     *   <li>Response: {@link CoupleRequestActionResponse}</li>
     *   <li>HTTP 201: Yêu cầu đã được tạo</li>
     *   <li>HTTP 400: Mã không hợp lệ hoặc tự kết nối với chính mình</li>
     *   <li>HTTP 404: Mã đối tác không tồn tại</li>
     *   <li>HTTP 409: Đã có yêu cầu đang chờ hoặc đã ghép đôi</li>
     * </ul>
     */
    @PostMapping("/requests")
    public ResponseEntity<CoupleRequestActionResponse> createRequest(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody CoupleRequestCreateRequest request) {
        CoupleRequestActionResponse response = coupleService.createRequest(authorizationHeader, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Quyết định (chấp nhận/từ chối) yêu cầu ghép đôi.
     *
     * <ul>
     *   <li>HTTP Method: {@code POST}</li>
     *   <li>Path: {@code /api/auth/couple/requests/{requestId}/decision}</li>
     *   <li>Auth: Yêu cầu Bearer token (phải là người nhận yêu cầu)</li>
     *   <li>Path variable: {@code requestId} - ID của yêu cầu ghép đôi</li>
     *   <li>Request body: {@link CoupleRequestDecisionRequest} (accept: boolean)</li>
     *   <li>Response: {@link CoupleRequestActionResponse}</li>
     *   <li>HTTP 200: Thành công</li>
     *   <li>HTTP 403: Không phải người nhận yêu cầu</li>
     *   <li>HTTP 404: Yêu cầu không tồn tại</li>
     *   <li>HTTP 409: Yêu cầu đã được xử lý trước đó</li>
     * </ul>
     */
    @PostMapping("/requests/{requestId}/decision")
    public ResponseEntity<CoupleRequestActionResponse> decideRequest(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable String requestId,
            @RequestBody CoupleRequestDecisionRequest request) {
        return ResponseEntity.ok(coupleService.decideRequest(authorizationHeader, requestId, request));
    }
}
