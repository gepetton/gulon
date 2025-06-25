package com.gulon.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple in-memory broker를 활성화하고 "/topic"과 "/queue" prefix를 가진 destination을 처리
        config.enableSimpleBroker("/topic", "/queue");
        
        // 클라이언트에서 메시지를 보낼 때 사용할 prefix 설정
        config.setApplicationDestinationPrefixes("/app");
        
        // 특정 사용자에게 메시지를 보낼 때 사용할 prefix 설정
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint를 등록 - 클라이언트가 연결할 수 있는 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // CORS 설정 (개발 환경용, 프로덕션에서는 구체적으로 지정)
                .withSockJS();  // SockJS fallback 옵션 활성화
        
        // SockJS 없이 순수 WebSocket 연결을 위한 엔드포인트
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
} 