package com.gulon.app.dto;

import com.gulon.app.entity.ChatMessage;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 채팅 메시지 관련 DTO 모음
 */
public class MessageDto {
    
    /**
     * 메시지 전송 요청 DTO
     */
    @Getter
    @Setter
    public static class SendRequest {
        private UUID groupPublicId;
        private UUID senderPublicId;
        private String content;
        private ChatMessage.MessageType type;
    }
    
    /**
     * 메시지 수정 요청 DTO
     */
    @Getter
    @Setter
    public static class EditRequest {
        private String content;
    }
    
    /**
     * 메시지 상세 응답 DTO
     */
    @Getter
    @Setter
    public static class Response {
        private UUID publicId;
        private UUID groupPublicId;
        private String groupName;
        private UUID senderPublicId;
        private String senderName;
        private String content;
        private ChatMessage.MessageType type;
        private LocalDateTime sentAt;
        private LocalDateTime editedAt;
        private boolean isEdited;
        private boolean isDeleted;
        private LocalDateTime deletedAt;
        private boolean canEdit;
        private boolean canDelete;
    }
    
    /**
     * 메시지 간단 정보 DTO
     * 채팅 목록용
     */
    @Getter
    @Setter
    public static class Summary {
        private UUID publicId;
        private UUID senderPublicId;
        private String senderName;
        private String content;
        private ChatMessage.MessageType type;
        private LocalDateTime sentAt;
        private boolean isEdited;
        private boolean isDeleted;
    }
    
    /**
     * 실시간 채팅용 DTO
     * WebSocket 전송용
     */
    @Getter
    @Setter
    public static class RealtimeMessage {
        private UUID publicId;
        private UUID groupPublicId;
        private UUID senderPublicId;
        private String senderName;
        private String content;
        private ChatMessage.MessageType type;
        private LocalDateTime sentAt;
        private String action; // "SEND", "EDIT", "DELETE"
    }
    
    /**
     * 메시지 히스토리 조회 응답 DTO
     */
    @Getter
    @Setter
    public static class HistoryResponse {
        private java.util.List<Summary> messages;
        private long totalCount;
        private int currentPage;
        private int pageSize;
        private boolean hasNext;
        private boolean hasPrevious;
        private UUID lastMessageId; // 다음 페이지 로딩용
        private LocalDateTime lastMessageTime;
    }
    
    /**
     * 그룹별 채팅 현황 DTO
     */
    @Getter
    @Setter
    public static class GroupChatStatus {
        private UUID groupPublicId;
        private String groupName;
        private long totalMessages;
        private long todayMessages;
        private Summary lastMessage;
        private LocalDateTime lastActivity;
        private java.util.List<ActiveUser> activeUsers;
        private int unreadCount; // 특정 사용자 기준
    }
    
    /**
     * 활성 사용자 정보 DTO
     */
    @Getter
    @Setter
    public static class ActiveUser {
        private UUID userPublicId;
        private String userName;
        private LocalDateTime lastSeen;
        private boolean isOnline;
    }
    
    /**
     * 메시지 검색 필터 DTO
     */
    @Getter
    @Setter
    public static class SearchFilter {
        private UUID groupPublicId;
        private UUID senderPublicId;
        private String keyword;
        private ChatMessage.MessageType type;
        private LocalDateTime sentAfter;
        private LocalDateTime sentBefore;
        private boolean includeDeleted;
        private String sortBy; // "sent_at"
        private String sortDirection; // "asc", "desc"
    }
    
    /**
     * 메시지 검색 결과 DTO
     */
    @Getter
    @Setter
    public static class SearchResult {
        private java.util.List<Summary> messages;
        private long totalCount;
        private int currentPage;
        private int totalPages;
        private int pageSize;
        private boolean hasNext;
        private boolean hasPrevious;
        private String searchKeyword;
    }
    
    /**
     * 메시지 통계 DTO
     */
    @Getter
    @Setter
    public static class Statistics {
        private UUID groupPublicId;
        private String groupName;
        private long totalMessages;
        private long textMessages;
        private long imageMessages;
        private long fileMessages;
        private long systemMessages;
        private long deletedMessages;
        private long editedMessages;
        private LocalDateTime firstMessageAt;
        private LocalDateTime lastMessageAt;
        private java.util.List<DailyMessageCount> dailyCounts;
        private java.util.List<UserMessageCount> userCounts;
    }
    
    /**
     * 일별 메시지 수 DTO
     */
    @Getter
    @Setter
    public static class DailyMessageCount {
        private String date; // "2024-01-01"
        private long count;
    }
    
    /**
     * 사용자별 메시지 수 DTO
     */
    @Getter
    @Setter
    public static class UserMessageCount {
        private UUID userPublicId;
        private String userName;
        private long messageCount;
        private long lastMessageDaysAgo;
    }
    
    /**
     * 메시지 전송 응답 DTO
     */
    @Getter
    @Setter
    public static class SendResponse {
        private UUID publicId;
        private UUID groupPublicId;
        private UUID senderPublicId;
        private String senderName;
        private String content;
        private ChatMessage.MessageType type;
        private LocalDateTime sentAt;
        private String message; // 성공 메시지
    }
} 