package com.gulon.app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "book", indexes = {
    @Index(name = "idx_book_public_id", columnList = "public_id"),
    @Index(name = "idx_book_isbn", columnList = "isbn"),
    @Index(name = "idx_book_title", columnList = "title"),
    @Index(name = "idx_book_author", columnList = "author")
})
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "public_id", unique = true, nullable = false)
    private UUID publicId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 200)
    private String author;

    @Column(unique = true, length = 30)
    private String isbn;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(length = 100)
    private String publisher;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "naver_link", length = 500)
    private String naverLink;

    @Column
    private Integer price;

    @Column(name = "discount_price")
    private Integer discountPrice;

    @Column(name = "is_cached")
    private Boolean isCached = false;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }

    // Constructors
    public Book(String title) {
        this.title = title;
    }

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    // 네이버 API 응답으로부터 Book 생성하는 팩토리 메서드
    public static Book fromNaverApi(String title, String author, String isbn, 
                                   String publisher, String publishedDate, 
                                   String imageUrl, String description, 
                                   String naverLink, Integer price, Integer discountPrice) {
        Book book = new Book();
        book.title = title;
        book.author = author;
        book.isbn = isbn;
        book.publisher = publisher;
        book.imageUrl = imageUrl;
        book.description = description;
        book.naverLink = naverLink;
        book.price = price;
        book.discountPrice = discountPrice;
        book.isCached = true;
        book.lastSyncedAt = LocalDateTime.now();
        
        // publishedDate 파싱 (YYYYMMDD 형식)
        if (publishedDate != null && publishedDate.length() == 8) {
            try {
                int year = Integer.parseInt(publishedDate.substring(0, 4));
                int month = Integer.parseInt(publishedDate.substring(4, 6));
                int day = Integer.parseInt(publishedDate.substring(6, 8));
                book.publishedDate = LocalDate.of(year, month, day);
            } catch (Exception e) {
                // 파싱 실패 시 null로 유지
            }
        }
        
        return book;
    }

    // 캐시 만료 여부 확인
    public boolean isCacheExpired(int cacheExpiryMinutes) {
        if (!isCached || lastSyncedAt == null) {
            return true;
        }
        return lastSyncedAt.isBefore(LocalDateTime.now().minusMinutes(cacheExpiryMinutes));
    }

    // 캐시 업데이트
    public void updateCache() {
        this.isCached = true;
        this.lastSyncedAt = LocalDateTime.now();
    }
} 