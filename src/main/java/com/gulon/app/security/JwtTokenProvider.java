package com.gulon.app.security;

import com.gulon.app.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 토큰 생성, 검증, 파싱을 담당하는 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    private final JwtConfig jwtProperties;
    
    /**
     * JWT 서명용 비밀키 생성
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }
    
    /**
     * 액세스 토큰 생성
     */
    public String generateAccessToken(UUID userPublicId, String email, String name) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiry());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userPublicId", userPublicId.toString());
        claims.put("email", email);
        claims.put("name", name);
        claims.put("type", "ACCESS");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userPublicId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setIssuer("gulon")
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 리프레시 토큰 생성
     */
    public String generateRefreshToken(UUID userPublicId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiry());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userPublicId", userPublicId.toString());
        claims.put("type", "REFRESH");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userPublicId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setIssuer("gulon")
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 토큰에서 사용자 PublicId 추출
     */
    public UUID getUserPublicIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            String userPublicIdStr = claims.get("userPublicId", String.class);
            return UUID.fromString(userPublicIdStr);
        } catch (Exception e) {
            log.error("토큰에서 사용자 PublicId 추출 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰입니다", e);
        }
    }
    
    /**
     * 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("email", String.class);
        } catch (Exception e) {
            log.error("토큰에서 이메일 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 토큰에서 이름 추출
     */
    public String getNameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("name", String.class);
        } catch (Exception e) {
            log.error("토큰에서 이름 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 토큰 타입 확인 (ACCESS, REFRESH)
     */
    public String getTokenType(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            log.error("토큰 타입 확인 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 토큰 ID 추출 (JTI)
     */
    public String getTokenId(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getId();
        } catch (Exception e) {
            log.error("토큰 ID 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 토큰 만료시간 추출
     */
    public LocalDateTime getExpirationFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            log.error("토큰 만료시간 추출 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 토큰 유효성 검증
     */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("토큰이 만료되었습니다: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 토큰 형식입니다: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("잘못된 토큰 형식입니다: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.error("토큰 서명이 유효하지 않습니다: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("토큰이 비어있거나 null입니다: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("토큰 검증 중 알 수 없는 오류 발생: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("토큰 만료 확인 실패: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * 액세스 토큰인지 확인
     */
    public boolean isAccessToken(String token) {
        return "ACCESS".equals(getTokenType(token));
    }
    
    /**
     * 리프레시 토큰인지 확인
     */
    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(getTokenType(token));
    }
    
    /**
     * 토큰 파싱 (내부 메서드)
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
} 