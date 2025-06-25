package com.gulon.app.dto;

import com.gulon.app.entity.SocialAccount;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 소셜 계정 관련 DTO 모음
 */
public class SocialAccountDto {
    
    /**
     * 소셜 계정 연결 요청 DTO
     */
    @Getter
    @Setter
    public static class CreateRequest {
        private UUID userPublicId;
        private SocialAccount.Provider provider;
        private String providerUserId;
        private String accessToken;
        private String refreshToken;
        private LocalDateTime tokenExpiry;
    }
    
    /**
     * 소셜 계정 토큰 업데이트 요청 DTO
     */
    @Getter
    @Setter
    public static class UpdateRequest {
        private String accessToken;
        private String refreshToken;
        private LocalDateTime tokenExpiry;
    }
    
    /**
     * 소셜 계정 상세 응답 DTO
     * 토큰 정보는 보안상 제외
     */
    @Getter
    @Setter
    public static class Response {
        private Integer id;
        private UUID userPublicId;
        private SocialAccount.Provider provider;
        private String providerUserId;
        private LocalDateTime tokenExpiry;
        private LocalDateTime linkedAt;
    }
    
    /**
     * 소셜 계정 요약 정보 DTO
     * 목록 조회시 사용
     */
    @Getter
    @Setter
    public static class Summary {
        private Integer id;
        private SocialAccount.Provider provider;
        private String providerUserId;
        private LocalDateTime linkedAt;
    }
    
    /**
     * 사용자별 소셜 계정 현황 DTO
     */
    @Getter
    @Setter
    public static class UserSocialAccounts {
        private UUID userPublicId;
        private String userName;
        private java.util.List<Summary> socialAccounts;
        private boolean hasKakao;
        private boolean hasGoogle;
        private boolean hasNaver;
    }
    
    /**
     * 소셜 로그인 결과 DTO
     */
    @Getter
    @Setter
    public static class LoginResult {
        private UUID userPublicId;
        private String userName;
        private String email;
        private SocialAccount.Provider provider;
        private boolean isNewUser;  // 새로 가입한 사용자인지
        private String accessToken; // JWT 토큰 등
    }
} 