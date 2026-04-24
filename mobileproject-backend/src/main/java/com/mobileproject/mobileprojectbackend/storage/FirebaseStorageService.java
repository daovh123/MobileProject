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

@Service
public class FirebaseStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseStorageService.class);

    private final String bucketName;

    public FirebaseStorageService(@Value("${firebase.storage.bucket:}") String bucketName) {
        this.bucketName = bucketName == null ? "" : bucketName.trim();
    }

    /**
     * Uploads avatar bytes to Firebase Storage at path avatars/{userId}/avatar_{timestamp}.{ext}.
     * Returns the public download URL.
     * Falls back to a placeholder URL if Firebase Storage is not configured.
     */
    public String uploadAvatar(String userId, byte[] imageBytes, String contentType) {
        if (bucketName.isBlank()) {
            LOGGER.warn("firebase.storage.bucket is not configured — returning placeholder avatar URL for user={}", userId);
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
        if (contentType == null) return "jpg";
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
