package com.gulon.app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation", indexes = {
    @Index(name = "idx_reco_user", columnList = "user_id"),
    @Index(name = "idx_reco_book", columnList = "book_id"),
    @Index(name = "idx_reco_source", columnList = "source")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @CreationTimestamp
    @Column(name = "recommended_at", nullable = false, updatable = false)
    private LocalDateTime recommendedAt;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source = Source.CHATBOT;

    public enum Source {
        CHATBOT, MANUAL
    }

    public Recommendation(User user, Book book) {
        this.user = user;
        this.book = book;
    }

    public Recommendation(User user, Book book, String reason, Source source) {
        this.user = user;
        this.book = book;
        this.reason = reason;
        this.source = source;
    }
} 