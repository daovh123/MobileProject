package com.mobileproject.mobileprojectbackend.favorite;

import com.mobileproject.mobileprojectbackend.auth.AuthIdentityService;
import com.mobileproject.mobileprojectbackend.auth.AuthUser;
import com.mobileproject.mobileprojectbackend.favorite.dto.FavoriteListResponse;
import com.mobileproject.mobileprojectbackend.favorite.dto.FavoriteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final AuthIdentityService authIdentityService;

    @PostMapping("/toggle")
    public ResponseEntity<FavoriteResponse> toggleFavorite(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam String placeId) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        return ResponseEntity.ok(favoriteService.toggleFavorite(user.getId(), placeId));
    }

    @GetMapping
    public ResponseEntity<FavoriteListResponse> getFavorites(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        return ResponseEntity.ok(favoriteService.getUserFavorites(user.getId()));
    }

    @GetMapping("/check")
    public ResponseEntity<FavoriteResponse> checkFavorite(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam String placeId) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        boolean isFavorite = favoriteService.isFavorite(user.getId(), placeId);
        return ResponseEntity.ok(
                new FavoriteResponse(
                        isFavorite,
                        isFavorite ? "Is favorite" : "Not favorite",
                        null,
                        placeId,
                        null));
    }
}
