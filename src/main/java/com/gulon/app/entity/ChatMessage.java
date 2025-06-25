package com.gulon.app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_message", indexes = {
    @Index(name = "idx_chat_group", columnList = "group_id"),
    @Index(name = "idx_chat_user", columnList = "user_id"),
    @Index(name = "idx_chat_sent", columnList = "sent_at"),
    @Index(name = "idx_chat_public_id", columnList = "public_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "public_id", unique = true, nullable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupTable group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000) // 메시지 길이 증가
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum MessageType {
        TEXT,           // 일반 텍스트
        IMAGE,          // 이미지
        FILE,           // 파일
        SYSTEM,         // 시스템 메시지 (입장/퇴장 등)
        NOTIFICATION    // 알림 메시지
    }

    @PrePersist
    protected void onCreate() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }

    public ChatMessage(GroupTable group, User user, String content) {
        this.group = group;
        this.user = user;
        this.content = content;
    }

    public ChatMessage(GroupTable group, User user, String content, MessageType type) {
        this.group = group;
        this.user = user;
        this.content = content;
        this.type = type;
    }

    public void editMessage(String newContent) {
        this.content = newContent;
        this.editedAt = LocalDateTime.now();
    }

    public void deleteMessage() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.content = "삭제된 메시지입니다.";
    }

    public void restoreMessage(String originalContent) {
        this.isDeleted = false;
        this.deletedAt = null;
        this.content = originalContent;
    }

    public boolean isEdited() {
        return editedAt != null;
    }

    public boolean canEdit(UUID userPublicId) {
        return user.getPublicId().equals(userPublicId) && !isDeleted;
    }

    public boolean canDelete(UUID userPublicId) {
        return user.getPublicId().equals(userPublicId) && !isDeleted;
    }
} 