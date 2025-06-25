package com.gulon.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "redis.token")
@Getter
@Setter
public class RedisTokenConfig {
    
    private String accessPrefix = "access_token:";
    private String refreshPrefix = "refresh_token:";
    private String blacklistPrefix = "blacklist_token:";
    private String socialPrefix = "social_token:";
    
    // 추가적인 Redis 세션 설정
    private String sessionPrefix = "user_session:";
    
    // 토큰 키 생성 헬퍼 메서드들
    public String getAccessTokenKey(String userId) {
        return accessPrefix + userId;
    }
    
    public String getRefreshTokenKey(String userId) {
        return refreshPrefix + userId;
    }
    
    public String getBlacklistTokenKey(String tokenId) {
        return blacklistPrefix + tokenId;
    }
    
    public String getSocialTokenKey(String userId, String provider) {
        return socialPrefix + provider + ":" + userId;
    }
    
    public String getSessionKey(String sessionId) {
        return sessionPrefix + sessionId;
    }
} 