package com.mobileproject.mobileprojectbackend.map;

import com.mobileproject.mobileprojectbackend.auth.AuthIdentityService;
import com.mobileproject.mobileprojectbackend.auth.AuthUser;
import com.mobileproject.mobileprojectbackend.auth.AuthUserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class MapHandshakeInterceptor implements HandshakeInterceptor {

    private static final String COUPLE_PREFIX = "/ws/map/share/";

    private final AuthIdentityService authIdentityService;
    private final AuthUserRepository authUserRepository;

    public MapHandshakeInterceptor(
            AuthIdentityService authIdentityService,
            AuthUserRepository authUserRepository
    ) {
        this.authIdentityService = authIdentityService;
        this.authUserRepository = authUserRepository;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (isBlank(authorizationHeader)) {
            String tokenParam = getQueryParam(request.getURI(), "token");
            if (!isBlank(tokenParam)) {
                authorizationHeader = "Bearer " + tokenParam;
            }
        }

        AuthUser user;
        try {
            user = authIdentityService.requireCurrentUser(authorizationHeader);
        } catch (Exception ex) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String coupleId = extractCoupleId(request.getURI());
        if (isBlank(coupleId)) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        if (!isUserInCouple(user, coupleId)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        attributes.put("userId", user.getId());
        attributes.put("username", user.getUsername());
        attributes.put("coupleId", coupleId);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }

    private boolean isUserInCouple(AuthUser user, String coupleId) {
        if (user == null || isBlank(user.getPartnerUserId())) {
            return false;
        }

        AuthUser partner = authUserRepository.findById(user.getPartnerUserId()).orElse(null);
        if (partner == null) {
            return false;
        }

        String expectedCoupleId = coupleIdForUsers(user.getId(), partner.getId());
        return expectedCoupleId.equals(coupleId);
    }

    private String coupleIdForUsers(String a, String b) {
        String user1 = a.compareTo(b) <= 0 ? a : b;
        String user2 = a.compareTo(b) <= 0 ? b : a;
        return "couple:" + user1 + ":" + user2;
    }

    private String extractCoupleId(URI uri) {
        if (uri == null) {
            return null;
        }

        String path = uri.getPath();
        if (isBlank(path) || !path.startsWith(COUPLE_PREFIX)) {
            return null;
        }

        String raw = path.substring(COUPLE_PREFIX.length());
        if (raw.endsWith("/")) {
            raw = raw.substring(0, raw.length() - 1);
        }

        if (raw.isBlank()) {
            return null;
        }

        return URLDecoder.decode(raw, StandardCharsets.UTF_8);
    }

    private String getQueryParam(URI uri, String key) {
        if (uri == null || isBlank(key)) {
            return null;
        }
        String query = uri.getQuery();
        if (isBlank(query)) {
            return null;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length != 2) {
                continue;
            }
            if (key.equals(parts[0])) {
                return parts[1];
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
