package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.AvatarFrameRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.AvatarFrameResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.AvatarUploadResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.ProfileResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.ProfileUpsertRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller REST quản lý hồ sơ cá nhân, avatar và khung viền avatar.
 *
 * <p>Base path: {@code /api/auth/profile}</p>
 * <p>Tất cả các endpoint đều yêu cầu xác thực (Bearer token trong header Authorization).</p>
 */
@RestController
@RequestMapping("/api/auth/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AvatarService avatarService;
    private final AvatarFrameCatalog avatarFrameCatalog;

    public ProfileController(ProfileService profileService,
                             AvatarService avatarService,
                             AvatarFrameCatalog avatarFrameCatalog) {
        this.profileService = profileService;
        this.avatarService = avatarService;
        this.avatarFrameCatalog = avatarFrameCatalog;
    }

    /**
     * Lấy thông tin hồ sơ cá nhân.
     *
     * <ul>
     *   <li>HTTP Method: {@code GET}</li>
     *   <li>Path: {@code /api/auth/profile}</li>
     *   <li>Auth: Yêu cầu Bearer token</li>
     *   <li>Response: {@link ProfileResponse}</li>
     *   <li>HTTP 200: Thành công</li>
     * </ul>
     */
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(profileService.getProfile(authorizationHeader));
    }

    /**
     * Tạo mới hoặc cập nhật hồ sơ cá nhân.
     *
     * <ul>
     *   <li>HTTP Method: {@code PUT}</li>
     *   <li>Path: {@code /api/auth/profile}</li>
     *   <li>Auth: Yêu cầu Bearer token</li>
     *   <li>Request body: {@link ProfileUpsertRequest} (fullName, nickName, birthDate, gender, email, phoneNumber)</li>
     *   <li>Response: {@link ProfileResponse}</li>
     *   <li>HTTP 200: Thành công</li>
     *   <li>HTTP 400: Dữ liệu không hợp lệ</li>
     *   <li>HTTP 409: Email đã được sử dụng bởi tài khoản khác</li>
     * </ul>
     */
    @PutMapping
    public ResponseEntity<ProfileResponse> upsertProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody ProfileUpsertRequest request) {
        return ResponseEntity.ok(profileService.upsertProfile(authorizationHeader, request));
    }

    /**
     * Accepts a multipart image, converts it to a Base64 Data URL, stores it in
     * MongoDB, and returns the Data URL so the client can use it immediately with
     * Coil's AsyncImage (which supports {@code data:} URIs natively).
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam("file") MultipartFile file) {
        String dataUrl = avatarService.uploadAvatar(authorizationHeader, file);
        return ResponseEntity.ok(new AvatarUploadResponse(true, "Avatar uploaded successfully", dataUrl));
    }

    /**
     * Lấy danh sách tất cả khung viền avatar có sẵn.
     *
     * <ul>
     *   <li>HTTP Method: {@code GET}</li>
     *   <li>Path: {@code /api/auth/profile/frames}</li>
     *   <li>Auth: Không yêu cầu</li>
     *   <li>Response: Danh sách {@link AvatarFrameResponse}</li>
     *   <li>HTTP 200: Thành công</li>
     * </ul>
     */
    @GetMapping("/frames")
    public ResponseEntity<List<AvatarFrameResponse>> getFrames() {
        List<AvatarFrameResponse> frames = avatarFrameCatalog.getAll().stream()
                .map(f -> new AvatarFrameResponse(f.id(), f.name(), f.resourceKey(), f.color()))
                .toList();
        return ResponseEntity.ok(frames);
    }

    /**
     * Cập nhật khung viền avatar của người dùng.
     *
     * <ul>
     *   <li>HTTP Method: {@code PUT}</li>
     *   <li>Path: {@code /api/auth/profile/frame}</li>
     *   <li>Auth: Yêu cầu Bearer token</li>
     *   <li>Request body: {@link AvatarFrameRequest} (frameId)</li>
     *   <li>Response: {@link ProfileResponse}</li>
     *   <li>HTTP 200: Thành công</li>
     *   <li>HTTP 400: frameId không hợp lệ</li>
     * </ul>
     */
    @PutMapping("/frame")
    public ResponseEntity<ProfileResponse> setFrame(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody AvatarFrameRequest request) {
        ProfileResponse response = profileService.updateAvatarFrame(authorizationHeader, request.frameId());
        return ResponseEntity.ok(response);
    }
}
