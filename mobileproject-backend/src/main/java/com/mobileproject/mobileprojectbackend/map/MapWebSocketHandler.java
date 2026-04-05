package com.mobileproject.mobileprojectbackend.map;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class MapWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapWebSocketHandler.class);

    private final MapLocationService mapLocationService;
    private final ObjectMapper objectMapper;

    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> sessionsByCoupleId = new ConcurrentHashMap<>();

    public MapWebSocketHandler(MapLocationService mapLocationService, ObjectMapper objectMapper) {
        this.mapLocationService = mapLocationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String coupleId = coupleIdOf(session);
        if (coupleId == null) {
            safeClose(session, CloseStatus.BAD_DATA);
            return;
        }

        sessionsByCoupleId
                .computeIfAbsent(coupleId, ignored -> new CopyOnWriteArraySet<>())
                .add(session);

        broadcastPartnerStatus(coupleId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String coupleId = coupleIdOf(session);
        String userId = userIdOf(session);

        if (coupleId == null || userId == null) {
            safeClose(session, CloseStatus.BAD_DATA);
            return;
        }

        IncomingLocation payload = parseIncomingLocation(message.getPayload());
        if (payload == null) {
            return;
        }

        try {
            mapLocationService.upsertUserLocation(
                    coupleId,
                    userId,
                    payload.latitude,
                    payload.longitude,
                    payload.updatedAt
            );
        } catch (Exception exception) {
            LOGGER.debug("Failed to store location update for coupleId={} userId={}", coupleId, userId, exception);
            return;
        }

        broadcastPartnerLocation(coupleId, session, payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String coupleId = coupleIdOf(session);
        if (coupleId == null) {
            return;
        }

        CopyOnWriteArraySet<WebSocketSession> sessions = sessionsByCoupleId.get(coupleId);
        if (sessions == null) {
            return;
        }

        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByCoupleId.remove(coupleId);
            return;
        }

        broadcastPartnerStatus(coupleId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        LOGGER.debug("WebSocket transport error", exception);
        safeClose(session, CloseStatus.SERVER_ERROR);
    }

    private void broadcastPartnerLocation(String coupleId, WebSocketSession sender, IncomingLocation payload) {
        CopyOnWriteArraySet<WebSocketSession> sessions = sessionsByCoupleId.get(coupleId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", "partner_location");
        message.put("latitude", payload.latitude);
        message.put("longitude", payload.longitude);
        message.put("updatedAt", payload.updatedAt);

        String json = writeJson(message);
        if (json == null) {
            return;
        }

        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }
            if (sender != null && sender.getId().equals(session.getId())) {
                continue;
            }
            safeSend(session, json);
        }
    }

    private void broadcastPartnerStatus(String coupleId) {
        CopyOnWriteArraySet<WebSocketSession> sessions = sessionsByCoupleId.get(coupleId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }

            String myUserId = userIdOf(session);
            if (myUserId == null) {
                continue;
            }

            boolean partnerOnline = isPartnerOnlineForUser(sessions, myUserId);

            Map<String, Object> message = new HashMap<>();
            message.put("type", "partner_status");
            message.put("online", partnerOnline);
            message.put("updatedAt", Instant.now().toString());

            String json = writeJson(message);
            if (json == null) {
                continue;
            }

            safeSend(session, json);
        }
    }

    private boolean isPartnerOnlineForUser(Set<WebSocketSession> sessions, String userId) {
        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }
            String otherUserId = userIdOf(session);
            if (otherUserId == null) {
                continue;
            }
            if (!userId.equals(otherUserId)) {
                return true;
            }
        }
        return false;
    }

    private IncomingLocation parseIncomingLocation(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(payload);

            String type = readText(node, "type");
            if (type != null && !"location".equalsIgnoreCase(type)) {
                return null;
            }

            Double lat = readDouble(node, "lat");
            if (lat == null) {
                lat = readDouble(node, "latitude");
            }
            Double lng = readDouble(node, "lng");
            if (lng == null) {
                lng = readDouble(node, "longitude");
            }

            if (lat == null || lng == null) {
                return null;
            }

            String updatedAt = readText(node, "updatedAt");
            if (updatedAt == null) {
                updatedAt = readText(node, "timestamp");
            }
            if (updatedAt == null) {
                updatedAt = Instant.now().toString();
            }

            return new IncomingLocation(lat, lng, updatedAt);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String readText(JsonNode node, String field) {
        if (node == null || field == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText(null);
        if (text == null || text.isBlank()) {
            return null;
        }
        return text;
    }

    private Double readDouble(JsonNode node, String field) {
        if (node == null || field == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.doubleValue();
        }
        try {
            return Double.parseDouble(value.asText());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            LOGGER.debug("Failed to serialize WebSocket message", exception);
            return null;
        }
    }

    private void safeSend(WebSocketSession session, String payload) {
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private void safeClose(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private String coupleIdOf(WebSocketSession session) {
        return session == null ? null : (String) session.getAttributes().get("coupleId");
    }

    private String userIdOf(WebSocketSession session) {
        return session == null ? null : (String) session.getAttributes().get("userId");
    }

    private record IncomingLocation(double latitude, double longitude, String updatedAt) {
    }
}
