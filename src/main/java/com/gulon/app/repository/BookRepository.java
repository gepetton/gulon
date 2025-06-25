package com.gulon.app.repository;

import com.gulon.app.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    
    // PublicId로 도서 조회
    Optional<Book> findByPublicId(UUID publicId);
    
    // PublicId 존재 여부 확인
    boolean existsByPublicId(UUID publicId);
    
    // ISBN으로 도서 조회
    Optional<Book> findByIsbn(String isbn);
    
    // ISBN 존재 여부 확인
    boolean existsByIsbn(String isbn);
    
    // 제목으로 도서 검색 (부분 일치, 페이징)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // 저자로 도서 검색 (부분 일치, 페이징)
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    
    // 출판사로 도서 검색 (페이징)
    Page<Book> findByPublisherContainingIgnoreCase(String publisher, Pageable pageable);
    
    // 특정 기간에 출간된 도서 조회 (페이징)
    Page<Book> findByPublishedDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // 제목과 저자로 도서 검색 (페이징)
    Page<Book> findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(
        String title, String author, Pageable pageable);
    
    // 최근 등록된 도서 조회 (페이징)
    @Query("SELECT b FROM Book b ORDER BY b.createdAt DESC")
    Page<Book> findRecentlyAdded(Pageable pageable);
    
    // 저자별 도서 수 조회
    @Query("SELECT COUNT(b) FROM Book b WHERE b.author = :author")
    long countByAuthor(@Param("author") String author);
    
    // 출판사별 도서 수 조회
    @Query("SELECT COUNT(b) FROM Book b WHERE b.publisher = :publisher")
    long countByPublisher(@Param("publisher") String publisher);
    
    // 특정 기간에 등록된 도서 조회 (페이징)
    Page<Book> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // 키워드로 도서 통합 검색 (제목, 저자, 출판사, 페이징)
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 캐시된 도서 조회
    List<Book> findByIsCachedTrue();
    
    // 캐시된 도서 수
    long countByIsCachedTrue();
    
    // 캐시 만료된 도서 조회
    @Query("SELECT b FROM Book b WHERE b.isCached = true AND b.lastSyncedAt < :expiredTime")
    List<Book> findExpiredCachedBooks(@Param("expiredTime") LocalDateTime expiredTime);
    
    // 최근 7일간 등록된 도서 수
    @Query("SELECT COUNT(b) FROM Book b WHERE b.createdAt >= :weekAgo")
    long countRecentlyAdded(@Param("weekAgo") LocalDateTime weekAgo);
    
    // 네이버 링크가 있는 도서 조회
    List<Book> findByNaverLinkIsNotNull();
    
    // 이미지 URL이 있는 도서 조회
    List<Book> findByImageUrlIsNotNull();
    
    // 가격 정보가 있는 도서 조회
    List<Book> findByPriceIsNotNull();
    
    // ISBN 목록으로 도서 조회
    List<Book> findByIsbnIn(List<String> isbns);
    
    // PublicId 목록으로 도서 조회
    List<Book> findByPublicIdIn(List<UUID> publicIds);
    
    // 특정 저자의 도서 목록 (페이징)
    @Query("SELECT b FROM Book b WHERE b.author = :author ORDER BY b.publishedDate DESC")
    Page<Book> findByAuthorOrderByPublishedDateDesc(@Param("author") String author, Pageable pageable);
    
    // 특정 출판사의 도서 목록 (페이징)
    @Query("SELECT b FROM Book b WHERE b.publisher = :publisher ORDER BY b.publishedDate DESC")
    Page<Book> findByPublisherOrderByPublishedDateDesc(@Param("publisher") String publisher, Pageable pageable);
    
    // 베스트셀러 (할인가 기준 정렬)
    @Query("SELECT b FROM Book b WHERE b.discountPrice IS NOT NULL ORDER BY b.discountPrice DESC")
    Page<Book> findBestsellers(Pageable pageable);
    
    // 신간 도서 (출간일 기준)
    @Query("SELECT b FROM Book b WHERE b.publishedDate >= :recentDate ORDER BY b.publishedDate DESC")
    Page<Book> findNewReleases(@Param("recentDate") LocalDate recentDate, Pageable pageable);
} 