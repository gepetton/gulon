package com.gulon.app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_member", indexes = {
    @Index(name = "idx_member_user", columnList = "user_id"),
    @Index(name = "idx_member_group", columnList = "group_id"),
    @Index(name = "idx_member_status", columnList = "status")
})
@IdClass(GroupMember.GroupMemberId.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GroupTable group;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    public enum Role {
        OWNER, ADMIN, MEMBER
    }

    public enum MemberStatus {
        ACTIVE, LEFT, REMOVED
    }

    public GroupMember(GroupTable group, User user) {
        this.group = group;
        this.user = user;
    }

    public GroupMember(GroupTable group, User user, Role role) {
        this.group = group;
        this.user = user;
        this.role = role;
    }

    // 복합키 클래스
    public static class GroupMemberId implements Serializable {
        private Integer group;
        private Integer user;

        public GroupMemberId() {}

        public GroupMemberId(Integer group, Integer user) {
            this.group = group;
            this.user = user;
        }

        // equals and hashCode
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupMemberId that = (GroupMemberId) o;
            return group.equals(that.group) && user.equals(that.user);
        }

        @Override
        public int hashCode() {
            return group.hashCode() + user.hashCode();
        }
    }
} 