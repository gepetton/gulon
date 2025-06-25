package com.gulon.app.dto;

import com.gulon.app.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 관련 DTO 모음
 */
public class UserDto {
    
    /**
     * 사용자 생성 요청 DTO
     * 회원가입 시 사용 (상태는 서버에서 자동으로 ACTIVE 설정)
     */
    @Getter
    @Setter
    public static class CreateRequest {
        private String name;
        private String email;
    }
    
    /**
     * 사용자 수정 요청 DTO
     * 일반 사용자는 이름/이메일만, 관리자는 상태도 변경 가능
     */
    @Getter
    @Setter
    public static class UpdateRequest {
        private String name;
        private String email;
        private User.UserStatus status; // 관리자용
    }
    
    /**
     * 사용자 상세 응답 DTO
     * 프로필 페이지, 관리자 페이지에서 사용 (상태 포함)
     */
    @Getter
    @Setter
    public static class Response {
        private Integer id;
        private UUID publicId;
        private String name;
        private String email;
        private User.UserStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    /**
     * 사용자 요약 정보 DTO
     * 목록, 검색 결과에서 사용 (상태 제외)
     */
    @Getter
    @Setter
    public static class Summary {
        private Integer id;
        private UUID publicId;
        private String name;
        private String email;
    }
    
    /**
     * 공개 프로필 DTO
     * 다른 사용자가 보는 정보 (이메일, 상태 제외)
     */
    @Getter
    @Setter
    public static class PublicProfile {
        private Integer id;
        private UUID publicId;
        private String name;
    }
}