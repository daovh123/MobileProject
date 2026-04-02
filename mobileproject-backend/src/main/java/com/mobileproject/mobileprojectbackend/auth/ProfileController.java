package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.ProfileResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.ProfileUpsertRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(profileService.getProfile(authorizationHeader));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> upsertProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                         @Valid @RequestBody ProfileUpsertRequest request) {
        return ResponseEntity.ok(profileService.upsertProfile(authorizationHeader, request));
    }
}
