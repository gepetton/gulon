package com.gulon.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {
    
    private String secret;
    private Long accessTokenExpiry = 3600000L; // 1시간 (밀리초)
    private Long refreshTokenExpiry = 2592000000L; // 30일 (밀리초)
    
    // JWT 토큰 타입
    public static final String TOKEN_TYPE = "Bearer";
    public static final String HEADER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    // 토큰 만료 시간을 초 단위로 반환
    public Long getAccessTokenExpiryInSeconds() {
        return accessTokenExpiry / 1000;
    }
    
    public Long getRefreshTokenExpiryInSeconds() {
        return refreshTokenExpiry / 1000;
    }
} 