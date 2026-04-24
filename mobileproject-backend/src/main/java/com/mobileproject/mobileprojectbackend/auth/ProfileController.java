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

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(profileService.getProfile(authorizationHeader));
    }

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

    @GetMapping("/frames")
    public ResponseEntity<List<AvatarFrameResponse>> getFrames() {
        List<AvatarFrameResponse> frames = avatarFrameCatalog.getAll().stream()
                .map(f -> new AvatarFrameResponse(f.id(), f.name(), f.resourceKey(), f.color()))
                .toList();
        return ResponseEntity.ok(frames);
    }

    @PutMapping("/frame")
    public ResponseEntity<ProfileResponse> setFrame(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody AvatarFrameRequest request) {
        ProfileResponse response = profileService.updateAvatarFrame(authorizationHeader, request.frameId());
        return ResponseEntity.ok(response);
    }
}
