package com.mobileproject.mobileprojectbackend.storage;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.firebase.FirebaseApp;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service upload file lên Firebase Storage.
 *
 * <p><strong>Business logic:</strong></p>
 * <ul>
 *   <li>Upload avatar: lưu tại path {@code avatars/{userId}/avatar_{timestamp}.{ext}}</li>
 *   <li>Upload ảnh kỷ niệm: lưu tại path {@code moments/{coupleId}/moment_{timestamp}.{ext}}</li>
 *   <li>Ảnh được set ACL public-read để ai cũng xem được</li>
 *   <li>Fallback: nếu Firebase chưa cấu hình, avatar dùng UI Avatars placeholder,
 *       ảnh kỷ niệm dùng data URI inline</li>
 * </ul>
 *
 * <p><strong>Cấu hình:</strong> {@code firebase.storage.bucket} – tên bucket Firebase Storage.</p>
 */
@Service
public class FirebaseStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseStorageService.class);

    private final String bucketName;

    public FirebaseStorageService(@Value("${firebase.storage.bucket:}") String bucketName) {
        this.bucketName = bucketName == null ? "" : bucketName.trim();
    }

    /**
     * Uploads avatar bytes to Firebase Storage at path
     * avatars/{userId}/avatar_{timestamp}.{ext}.
     * Returns the public download URL.
     * Falls back to a placeholder URL if Firebase Storage is not configured.
     */
    /**
     * Upload ảnh avatar lên Firebase Storage.
     *
     * @param userId      ID người dùng
     * @param imageBytes  dữ liệu ảnh
     * @param contentType MIME type (image/jpeg, image/png, ...)
     * @return public download URL, hoặc placeholder URL nếu Firebase chưa cấu hình
     */
    public String uploadAvatar(String userId, byte[] imageBytes, String contentType) {
        if (bucketName.isBlank()) {
            LOGGER.warn("firebase.storage.bucket is not configured — returning placeholder avatar URL for user={}",
                    userId);
            return placeholderUrl(userId);
        }

        if (FirebaseApp.getApps().isEmpty()) {
            LOGGER.warn("FirebaseApp is not initialized — returning placeholder avatar URL for user={}", userId);
            return placeholderUrl(userId);
        }

        String extension = extensionForContentType(contentType);
        String objectPath = "avatars/" + userId + "/avatar_" + System.currentTimeMillis() + "." + extension;

        try {
            Storage storage = StorageClient.getInstance().bucket(bucketName).getStorage();
            BlobId blobId = BlobId.of(bucketName, objectPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .setAcl(List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
                    .build();

            storage.create(blobInfo, imageBytes);

            String encodedPath = URLEncoder.encode(objectPath, StandardCharsets.UTF_8);
            String downloadUrl = "https://firebasestorage.googleapis.com/v0/b/"
                    + bucketName + "/o/" + encodedPath + "?alt=media";

            LOGGER.info("Avatar uploaded for user={} path={}", userId, objectPath);
            return downloadUrl;
        } catch (Exception ex) {
            LOGGER.warn("Failed to upload avatar for user={}: {}", userId, ex.getMessage(), ex);
            return placeholderUrl(userId);
        }
    }

    private String placeholderUrl(String userId) {
        String encoded = URLEncoder.encode(userId, StandardCharsets.UTF_8);
        return "https://ui-avatars.com/api/?name=" + encoded + "&size=256&background=6750A4&color=fff";
    }

    private String extensionForContentType(String contentType) {
        if (contentType == null)
            return "jpg";
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    /**
     * Upload ảnh kỷ niệm lên Firebase Storage.
     *
     * @param coupleId    ID couple
     * @param imageBytes  dữ liệu ảnh
     * @param contentType MIME type
     * @return public download URL, hoặc data URI nếu Firebase chưa cấu hình
     */
    public String uploadMomentImage(String coupleId, byte[] imageBytes, String contentType) {
        if (bucketName.isBlank() || FirebaseApp.getApps().isEmpty()) {
            LOGGER.warn("Firebase not configured — returning inline data url for moment in couple={}", coupleId);
            return dataUrlForBytes(imageBytes, contentType);
        }

        String extension = extensionForContentType(contentType);
        String objectPath = "moments/" + coupleId + "/moment_" + System.currentTimeMillis() + "." + extension;

        try {
            Storage storage = StorageClient.getInstance().bucket(bucketName).getStorage();
            BlobId blobId = BlobId.of(bucketName, objectPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .setAcl(List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
                    .build();

            storage.create(blobInfo, imageBytes);

            String encodedPath = URLEncoder.encode(objectPath, StandardCharsets.UTF_8);
            String downloadUrl = "https://firebasestorage.googleapis.com/v0/b/"
                    + bucketName + "/o/" + encodedPath + "?alt=media";

            LOGGER.info("Moment image uploaded for couple={} path={}", coupleId, objectPath);
            return downloadUrl;
        } catch (Exception ex) {
            LOGGER.warn("Failed to upload moment image for couple={}: {}", coupleId, ex.getMessage(), ex);
            return dataUrlForBytes(imageBytes, contentType);
        }
    }

    private String dataUrlForBytes(byte[] imageBytes, String contentType) {
        String safeContentType = (contentType == null || contentType.isBlank())
                ? "image/jpeg"
                : contentType;
        String encoded = java.util.Base64.getEncoder().encodeToString(imageBytes);
        return "data:" + safeContentType + ";base64," + encoded;
    }
}
