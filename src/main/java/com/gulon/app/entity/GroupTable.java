package com.gulon.app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_table", indexes = {
    @Index(name = "idx_group_owner", columnList = "owner_id"),
    @Index(name = "idx_group_privacy", columnList = "privacy"),
    @Index(name = "idx_group_public_id", columnList = "public_id")
})
@Getter
@Setter
@NoArgsConstructor
public class GroupTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "public_id", unique = true, nullable = false)
    private UUID publicId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Privacy privacy = Privacy.PRIVATE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 연관관계는 필요시 지연 로딩으로 조회

    public enum Privacy {
        PUBLIC, PRIVATE
    }

    @PrePersist
    protected void onCreate() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }

    public GroupTable(String name, User owner) {
        this.name = name;
        this.owner = owner;
    }

    public GroupTable(String name, String description, User owner, Privacy privacy) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.privacy = privacy;
    }
} 