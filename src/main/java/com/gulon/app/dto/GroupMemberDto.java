package com.gulon.app.dto;

import com.gulon.app.entity.GroupMember;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 그룹 멤버 관련 DTO 모음
 */
public class GroupMemberDto {
    
    /**
     * 그룹 멤버 추가 요청 DTO
     */
    @Getter
    @Setter
    public static class CreateRequest {
        private UUID groupPublicId;
        private UUID userPublicId;
        private GroupMember.Role role;
    }
    
    /**
     * 그룹 멤버 역할/상태 업데이트 요청 DTO
     */
    @Getter
    @Setter
    public static class UpdateRequest {
        private GroupMember.Role role;
        private GroupMember.MemberStatus status;
    }
    
    /**
     * 그룹 멤버 상세 응답 DTO
     */
    @Getter
    @Setter
    public static class Response {
        private UUID groupPublicId;
        private String groupName;
        private UUID userPublicId;
        private String userName;
        private String userEmail;
        private GroupMember.Role role;
        private GroupMember.MemberStatus status;
        private LocalDateTime joinedAt;
    }
    
    /**
     * 그룹 멤버 요약 정보 DTO
     * 목록 조회시 사용
     */
    @Getter
    @Setter
    public static class Summary {
        private UUID groupPublicId;
        private String groupName;
        private UUID userPublicId;
        private String userName;
        private GroupMember.Role role;
        private GroupMember.MemberStatus status;
        private LocalDateTime joinedAt;
    }
    
    /**
     * 그룹별 멤버 현황 DTO
     */
    @Getter
    @Setter
    public static class GroupMembersStatus {
        private UUID groupPublicId;
        private String groupName;
        private String groupDescription;
        private UUID ownerPublicId;
        private String ownerName;
        private java.util.List<MemberInfo> members;
        private long totalMembers;
        private long activeMembers;
        private long leftMembers;
        private long removedMembers;
    }
    
    /**
     * 멤버 기본 정보 DTO
     */
    @Getter
    @Setter
    public static class MemberInfo {
        private UUID userPublicId;
        private String userName;
        private String userEmail;
        private GroupMember.Role role;
        private GroupMember.MemberStatus status;
        private LocalDateTime joinedAt;
        private boolean isOwner;
    }
    
    /**
     * 사용자별 그룹 가입 현황 DTO
     */
    @Getter
    @Setter
    public static class UserGroupsStatus {
        private UUID userPublicId;
        private String userName;
        private java.util.List<GroupInfo> groups;
        private long totalGroups;
        private long activeGroups;
        private long ownedGroups;
        private long adminGroups;
    }
    
    /**
     * 그룹 기본 정보 DTO
     */
    @Getter
    @Setter
    public static class GroupInfo {
        private UUID groupPublicId;
        private String groupName;
        private String groupDescription;
        private GroupMember.Role userRole;
        private GroupMember.MemberStatus memberStatus;
        private LocalDateTime joinedAt;
        private boolean isOwner;
    }
    
    /**
     * 그룹 가입 신청 DTO
     */
    @Getter
    @Setter
    public static class JoinRequest {
        private UUID groupPublicId;
        private UUID userPublicId;
        private String message; // 가입 신청 메시지
    }
    
    /**
     * 멤버 초대 DTO
     */
    @Getter
    @Setter
    public static class InviteRequest {
        private UUID groupPublicId;
        private UUID inviterPublicId;
        private UUID inviteePublicId;
        private GroupMember.Role role;
        private String message; // 초대 메시지
    }
} 