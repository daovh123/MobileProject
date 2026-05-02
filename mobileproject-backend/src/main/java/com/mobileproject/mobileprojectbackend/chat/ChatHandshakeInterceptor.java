package com.mobileproject.mobileprojectbackend.chat;

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

import java.net.URI;
import java.util.Map;

@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final AuthIdentityService authIdentityService;
    private final AuthUserRepository authUserRepository;

    public ChatHandshakeInterceptor(
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

        if (user == null || isBlank(user.getId())) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        if (isBlank(user.getPartnerUserId())) {
            response.setStatusCode(HttpStatus.CONFLICT);
            return false;
        }

        AuthUser partner = authUserRepository.findById(user.getPartnerUserId()).orElse(null);
        if (partner == null || isBlank(partner.getId())) {
            response.setStatusCode(HttpStatus.CONFLICT);
            return false;
        }

        String coupleId = coupleIdForUsers(user.getId(), partner.getId());

        attributes.put("userId", user.getId());
        attributes.put("username", user.getUsername());
        attributes.put("avatarUrl", user.getAvatarUrl());
        attributes.put("partnerUserId", partner.getId());
        attributes.put("partnerUsername", partner.getUsername());
        attributes.put("partnerAvatarUrl", partner.getAvatarUrl());
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

    private String coupleIdForUsers(String a, String b) {
        String user1 = a.compareTo(b) <= 0 ? a : b;
        String user2 = a.compareTo(b) <= 0 ? b : a;
        return "couple:" + user1 + ":" + user2;
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
