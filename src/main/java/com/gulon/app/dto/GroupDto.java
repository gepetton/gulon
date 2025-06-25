package com.gulon.app.dto;

import com.gulon.app.entity.GroupTable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 그룹(GroupTable) 관련 DTO 모음
 */
public class GroupDto {
    
    /**
     * 그룹 생성 요청 DTO
     */
    @Getter
    @Setter
    public static class CreateRequest {
        private String name;
        private String description;
        private UUID ownerPublicId;
        private GroupTable.Privacy privacy;
    }
    
    /**
     * 그룹 수정 요청 DTO
     */
    @Getter
    @Setter
    public static class UpdateRequest {
        private String name;
        private String description;
        private GroupTable.Privacy privacy;
    }
    
    /**
     * 그룹 상세 응답 DTO
     */
    @Getter
    @Setter
    public static class Response {
        private UUID publicId;
        private String name;
        private String description;
        private UUID ownerPublicId;
        private String ownerName;
        private String ownerEmail;
        private GroupTable.Privacy privacy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private long memberCount;
        private long activeMemberCount;
    }
    
    /**
     * 그룹 요약 정보 DTO
     * 목록 조회시 사용
     */
    @Getter
    @Setter
    public static class Summary {
        private UUID publicId;
        private String name;
        private String description;
        private UUID ownerPublicId;
        private String ownerName;
        private GroupTable.Privacy privacy;
        private LocalDateTime createdAt;
        private long memberCount;
        private boolean isOwner;
        private boolean isMember;
    }
    
    /**
     * 공개 그룹 정보 DTO
     * 그룹 검색시 사용
     */
    @Getter
    @Setter
    public static class PublicInfo {
        private UUID publicId;
        private String name;
        private String description;
        private String ownerName;
        private LocalDateTime createdAt;
        private long memberCount;
        private boolean isMember;
    }
    
    /**
     * 그룹 생성 응답 DTO
     */
    @Getter
    @Setter
    public static class CreateResponse {
        private UUID publicId;
        private String name;
        private String description;
        private UUID ownerPublicId;
        private String ownerName;
        private GroupTable.Privacy privacy;
        private LocalDateTime createdAt;
        private String message; // 생성 성공 메시지
    }
    
    /**
     * 그룹 통계 DTO
     */
    @Getter
    @Setter
    public static class Statistics {
        private UUID publicId;
        private String name;
        private long totalMembers;
        private long activeMembers;
        private long leftMembers;
        private long removedMembers;
        private long ownerCount;
        private long adminCount;
        private long memberCount;
        private LocalDateTime createdAt;
        private LocalDateTime lastActivityAt;
    }
    
    /**
     * 그룹 검색 필터 DTO
     */
    @Getter
    @Setter
    public static class SearchFilter {
        private String keyword; // 이름 + 설명 검색
        private GroupTable.Privacy privacy;
        private UUID ownerPublicId;
        private LocalDateTime createdAfter;
        private LocalDateTime createdBefore;
        private Integer minMembers;
        private Integer maxMembers;
        private String sortBy; // "created_at", "member_count", "name"
        private String sortDirection; // "asc", "desc"
    }
    
    /**
     * 그룹 검색 결과 DTO
     */
    @Getter
    @Setter
    public static class SearchResult {
        private java.util.List<PublicInfo> groups;
        private long totalCount;
        private int currentPage;
        private int totalPages;
        private int pageSize;
        private boolean hasNext;
        private boolean hasPrevious;
    }
} 