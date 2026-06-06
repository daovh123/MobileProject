package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.ProfileResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.ProfileUpsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service xử lý nghiệp vụ hồ sơ cá nhân (profile).
 *
 * <p>Nghiệp vụ chính:</p>
 * <ul>
 *   <li><b>Xem hồ sơ</b> - Lấy thông tin profile người dùng hiện tại</li>
 *   <li><b>Cập nhật hồ sơ</b> - Lưu fullName, nickName, birthDate, gender, email;
 *       đánh dấu {@code profileCompleted = true}</li>
 *   <li><b>Cập nhật avatar</b> - Lưu URL avatar (Base64 Data URL)</li>
 *   <li><b>Cập nhật khung avatar</b> - Chọn khung viền từ catalog</li>
 * </ul>
 *
 * <p>Validation nghiệp vụ:</p>
 * <ul>
 *   <li>Ngày sinh hỗ trợ nhiều định dạng: yyyy-MM-dd, dd/MM/yyyy, d/M/yyyy, dd-MM-yyyy</li>
 *   <li>Giới tính hỗ trợ cả tiếng Việt (nam/nữ/khác) và tiếng Anh</li>
 *   <li>Email được validate format và kiểm tra trùng lặp</li>
 * </ul>
 */
@Service
public class ProfileService {

    private static final List<DateTimeFormatter> ALLOWED_BIRTH_DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("dd/MM/uuuu"),
            DateTimeFormatter.ofPattern("d-M-uuuu"),
            DateTimeFormatter.ofPattern("dd-MM-uuuu"));

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final AuthIdentityService authIdentityService;
    private final AuthUserCacheService authUserCacheService;
    private final AuthUserRepository authUserRepository;
    private final AvatarFrameCatalog avatarFrameCatalog;

    public ProfileService(AuthIdentityService authIdentityService,
                          AuthUserCacheService authUserCacheService,
                          AuthUserRepository authUserRepository,
                          AvatarFrameCatalog avatarFrameCatalog) {
        this.authIdentityService = authIdentityService;
        this.authUserCacheService = authUserCacheService;
        this.authUserRepository = authUserRepository;
        this.avatarFrameCatalog = avatarFrameCatalog;
    }

    /**
     * Tạo mới hoặc cập nhật hồ sơ cá nhân.
     * Normalize birthDate, gender, kiểm tra email trùng lặp, đánh dấu profileCompleted.
     *
     * @param authorizationHeader header Authorization
     * @param request             thông tin hồ sơ cần cập nhật
     * @return ProfileResponse chứa thông tin hồ sơ đã cập nhật
     */
    public ProfileResponse upsertProfile(String authorizationHeader, ProfileUpsertRequest request) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);

        String fullName = request.fullName().trim();
        if (fullName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }

        String nickName = trimToNull(request.nickName());
        String birthDate = normalizeBirthDate(request.birthDate());
        String gender = normalizeGender(request.gender());

        user.setFullName(fullName);
        user.setNickName(nickName);
        user.setBirthDate(birthDate);
        user.setGender(gender);
        applyEmailUpdate(user, request.email());
        user.setProfileCompleted(true);
        AuthUser savedUser = authUserCacheService.save(user);

        return toProfileResponse(savedUser, "Profile saved successfully");
    }

    /**
     * Lấy thông tin hồ sơ cá nhân của người dùng hiện tại.
     *
     * @param authorizationHeader header Authorization
     * @return ProfileResponse chứa thông tin hồ sơ
     */
    public ProfileResponse getProfile(String authorizationHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);
        return toProfileResponse(user, "Profile fetched successfully");
    }

    /**
     * Cập nhật URL avatar (Base64 Data URL) cho người dùng.
     *
     * @param authorizationHeader header Authorization
     * @param avatarUrl           URL avatar dạng data:image/...;base64,...
     * @return ProfileResponse chứa thông tin hồ sơ đã cập nhật
     */
    public ProfileResponse updateAvatarUrl(String authorizationHeader, String avatarUrl) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);
        user.setAvatarUrl(avatarUrl);
        AuthUser saved = authUserCacheService.save(user);
        return toProfileResponse(saved, "Avatar updated successfully");
    }

    /**
     * Cập nhật khung viền avatar. Xóa khung nếu frameId là null hoặc rỗng.
     *
     * @param authorizationHeader header Authorization
     * @param frameId             ID khung viền (phải tồn tại trong {@link AvatarFrameCatalog})
     * @return ProfileResponse chứa thông tin hồ sơ đã cập nhật
     * @throws ResponseStatusException HTTP 400 nếu frameId không tồn tại
     */
    public ProfileResponse updateAvatarFrame(String authorizationHeader, String frameId) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);

        if (frameId != null && !frameId.isBlank()) {
            boolean valid = avatarFrameCatalog.findById(frameId).isPresent();
            if (!valid) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Unknown frame id: " + frameId);
            }
            user.setAvatarFrameId(frameId);
        } else {
            user.setAvatarFrameId(null);
        }

        AuthUser saved = authUserCacheService.save(user);
        return toProfileResponse(saved, "Avatar frame updated successfully");
    }

    private ProfileResponse toProfileResponse(AuthUser user, String message) {
        return ProfileResponse.success(
                message,
                user.getUsername(),
                user.getFullName(),
                user.getNickName(),
                user.getBirthDate(),
                user.getGender(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.isProfileCompleted(),
                user.getPartnerUserId() != null && !user.getPartnerUserId().isBlank(),
                user.getAvatarUrl(),
                user.getAvatarFrameId());
    }

    private void applyEmailUpdate(AuthUser user, String rawEmail) {
        if (rawEmail == null) {
            return;
        }
        String trimmed = rawEmail.trim();
        if (trimmed.isBlank()) {
            return;
        }
        String normalized = trimmed.toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }
        String currentEmail = user.getEmail();
        if (currentEmail != null && currentEmail.equalsIgnoreCase(normalized)) {
            return;
        }
        Optional<AuthUser> existing = authUserRepository.findByEmail(normalized);
        if (existing.isPresent() && !Objects.equals(existing.get().getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        user.setEmail(normalized);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizeBirthDate(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Birth date is required");
        }

        for (DateTimeFormatter formatter : ALLOWED_BIRTH_DATE_FORMATS) {
            try {
                LocalDate parsed = LocalDate.parse(value, formatter);
                return parsed.toString();
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Birth date must be yyyy-MM-dd or dd/MM/yyyy");
    }

    private String normalizeGender(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim().toLowerCase();
        if (value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gender is required");
        }

        return switch (value) {
            case "male", "nam" -> "MALE";
            case "female", "nu" -> "FEMALE";
            case "other", "khac" -> "OTHER";
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Gender must be MALE, FEMALE, or OTHER");
        };
    }
}
