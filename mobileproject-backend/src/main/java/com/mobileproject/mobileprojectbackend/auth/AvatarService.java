package com.mobileproject.mobileprojectbackend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

/**
 * Converts an uploaded image to a Base64 Data URL and persists it on the
 * {@link AuthUser} document in MongoDB.
 *
 * <p>No external storage service (Firebase Storage, MinIO, S3, …) is used.
 * The resulting {@code data:<mime>;base64,<payload>} string is stored in the
 * {@code avatarUrl} field and is passed directly to Coil's {@code AsyncImage}
 * on the Android client, which supports {@code data:} URIs natively.
 */
@Service
public class AvatarService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvatarService.class);

    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5 MB hard limit
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

    private final AuthIdentityService authIdentityService;
    private final AuthUserCacheService authUserCacheService;

    public AvatarService(AuthIdentityService authIdentityService,
                         AuthUserCacheService authUserCacheService) {
        this.authIdentityService = authIdentityService;
        this.authUserCacheService = authUserCacheService;
    }

    /**
     * Validates the uploaded file, encodes it as a Base64 Data URL, saves it to
     * the user document, and returns the Data URL.
     *
     * @param authorizationHeader Bearer token from the HTTP request
     * @param file                Multipart file from the request
     * @return the {@code data:<mime>;base64,…} string that was persisted
     */
    public String uploadAvatar(String authorizationHeader, MultipartFile file) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file provided");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File exceeds the 5 MB size limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only JPEG, PNG, WebP, and GIF images are accepted");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            LOGGER.error("Failed to read avatar upload for user {}", user.getId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not read the uploaded file");
        }

        String dataUrl = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);

        user.setAvatarUrl(dataUrl);
        authUserCacheService.save(user);

        LOGGER.info("Avatar stored for user {} ({} bytes, {})", user.getId(), bytes.length, contentType);
        return dataUrl;
    }
}
