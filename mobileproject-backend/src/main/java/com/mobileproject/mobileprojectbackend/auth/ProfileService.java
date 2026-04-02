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

@Service
public class ProfileService {

    private static final List<DateTimeFormatter> ALLOWED_BIRTH_DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("dd/MM/uuuu"),
            DateTimeFormatter.ofPattern("d-M-uuuu"),
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
    );

    private final AuthIdentityService authIdentityService;
    private final AuthUserRepository authUserRepository;

    public ProfileService(AuthIdentityService authIdentityService, AuthUserRepository authUserRepository) {
        this.authIdentityService = authIdentityService;
        this.authUserRepository = authUserRepository;
    }

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
        user.setProfileCompleted(true);
        authUserRepository.save(user);

        return toProfileResponse(user, "Profile saved successfully");
    }

    public ProfileResponse getProfile(String authorizationHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);
        return toProfileResponse(user, "Profile fetched successfully");
    }

    private ProfileResponse toProfileResponse(AuthUser user, String message) {
        return ProfileResponse.success(
                message,
                user.getUsername(),
                user.getFullName(),
                user.getNickName(),
                user.getBirthDate(),
                user.getGender(),
                user.isProfileCompleted(),
                user.getPartnerUserId() != null && !user.getPartnerUserId().isBlank()
        );
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
                "Birth date must be yyyy-MM-dd or dd/MM/yyyy"
        );
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
                    "Gender must be MALE, FEMALE, or OTHER"
            );
        };
    }
}
