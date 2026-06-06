package com.mobileproject.mobileprojectbackend.map;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Cấu hình WebSocket cho module chia sẻ vị trí.
 * Đăng ký handler tại endpoint {@code /ws/map/share/{coupleId}}.
 */
@Configuration
@EnableWebSocket
public class MapWebSocketConfig implements WebSocketConfigurer {

    private final MapWebSocketHandler mapWebSocketHandler;
    private final MapHandshakeInterceptor mapHandshakeInterceptor;

    public MapWebSocketConfig(
            MapWebSocketHandler mapWebSocketHandler,
            MapHandshakeInterceptor mapHandshakeInterceptor
    ) {
        this.mapWebSocketHandler = mapWebSocketHandler;
        this.mapHandshakeInterceptor = mapHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(mapWebSocketHandler, "/ws/map/share/*")
                .addInterceptors(mapHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
