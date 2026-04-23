package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthIdentityService {

    private final AuthTokenService authTokenService;
    private final AuthUserCacheService authUserCacheService;

    public AuthIdentityService(AuthTokenService authTokenService, AuthUserCacheService authUserCacheService) {
        this.authTokenService = authTokenService;
        this.authUserCacheService = authUserCacheService;
    }

    public AuthUser requireCurrentUser(String authorizationHeader) {
        String username = authTokenService.resolveUsername(authorizationHeader)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid access token"));

        return authUserCacheService.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User session not found"));
    }
}
