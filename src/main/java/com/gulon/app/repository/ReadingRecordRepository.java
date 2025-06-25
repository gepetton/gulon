package com.gulon.app.repository;

import com.gulon.app.entity.Book;
import com.gulon.app.entity.ReadingRecord;
import com.gulon.app.entity.User;
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

@Repository
public interface ReadingRecordRepository extends JpaRepository<ReadingRecord, Integer> {
    
    // 사용자별 독서 기록 조회
    List<ReadingRecord> findByUser(User user);
    Page<ReadingRecord> findByUser(User user, Pageable pageable);
    
    // 사용자 ID로 독서 기록 조회
    List<ReadingRecord> findByUserId(Integer userId);
    
    // 도서별 독서 기록 조회
    List<ReadingRecord> findByBook(Book book);
    Page<ReadingRecord> findByBook(Book book, Pageable pageable);
    
    // 사용자와 도서로 독서 기록 조회
    Optional<ReadingRecord> findByUserAndBook(User user, Book book);
    boolean existsByUserAndBook(User user, Book book);
    
    // 독서 상태별 조회
    List<ReadingRecord> findByStatus(ReadingRecord.ReadingStatus status);
    Page<ReadingRecord> findByStatus(ReadingRecord.ReadingStatus status, Pageable pageable);
    
    // 사용자의 특정 상태 독서 기록 조회
    List<ReadingRecord> findByUserAndStatus(User user, ReadingRecord.ReadingStatus status);
    List<ReadingRecord> findByUserAndStatusOrderByUpdatedAtDesc(User user, ReadingRecord.ReadingStatus status);
    List<ReadingRecord> findByUserAndStatusAndUpdatedAtAfterOrderByUpdatedAtDesc(
            User user, ReadingRecord.ReadingStatus status, LocalDateTime updatedAt);
    
    // 특정 기간에 시작한 독서 기록 조회
    List<ReadingRecord> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    // 특정 기간에 완료한 독서 기록 조회
    List<ReadingRecord> findByEndDateBetween(LocalDate startDate, LocalDate endDate);
    
    // 사용자의 완료된 독서 기록 수 조회
    @Query("SELECT COUNT(rr) FROM ReadingRecord rr WHERE rr.user = :user AND rr.status = 'COMPLETED'")
    long countCompletedByUser(@Param("user") User user);
    
    // 사용자의 현재 읽고 있는 책 조회
    @Query("SELECT rr FROM ReadingRecord rr WHERE rr.user = :user AND rr.status = 'READING'")
    List<ReadingRecord> findCurrentlyReadingByUser(@Param("user") User user);
    
    // 진도율 계산 (현재 페이지 / 총 페이지)
    @Query("SELECT rr FROM ReadingRecord rr WHERE rr.user = :user AND " +
           "(CAST(rr.currentPage AS double) / CAST(rr.totalPages AS double)) >= :progressRate")
    List<ReadingRecord> findByUserAndProgressGreaterThan(@Param("user") User user, @Param("progressRate") double progressRate);
    
    // 최근 업데이트된 독서 기록 조회
    @Query("SELECT rr FROM ReadingRecord rr WHERE rr.user = :user ORDER BY rr.updatedAt DESC")
    List<ReadingRecord> findRecentlyUpdatedByUser(@Param("user") User user);
    
    // 특정 도서의 완료 독자 수 조회
    @Query("SELECT COUNT(rr) FROM ReadingRecord rr WHERE rr.book = :book AND rr.status = 'COMPLETED'")
    long countCompletedReadersByBook(@Param("book") Book book);
    
    // 월별 독서 완료 통계
    @Query("SELECT COUNT(rr) FROM ReadingRecord rr WHERE rr.user = :user AND rr.status = 'COMPLETED' " +
           "AND YEAR(rr.endDate) = :year AND MONTH(rr.endDate) = :month")
    long countCompletedByUserAndYearMonth(@Param("user") User user, @Param("year") int year, @Param("month") int month);
    
    // 추가 카운팅 메서드들
    long countByUser(User user);
    long countByUserAndStatus(User user, ReadingRecord.ReadingStatus status);
    long countByBook(Book book);
    long countByBookAndStatus(Book book, ReadingRecord.ReadingStatus status);
    long countByStatus(ReadingRecord.ReadingStatus status);
    
    // 최근 기록 조회
    List<ReadingRecord> findTop10ByBookOrderByUpdatedAtDesc(Book book);
    
    // 통계 계산을 위한 쿼리들
    @Query("SELECT SUM(rr.currentPage) FROM ReadingRecord rr WHERE rr.user = :user")
    Integer getTotalPagesReadByUser(@Param("user") User user);
    
    @Query("SELECT AVG(CAST(rr.currentPage AS double) / CAST(rr.totalPages AS double) * 100) FROM ReadingRecord rr WHERE rr.user = :user AND rr.totalPages > 0")
    Double getAverageProgressByUser(@Param("user") User user);
    
    @Query("SELECT AVG(CAST(rr.currentPage AS double) / CAST(rr.totalPages AS double) * 100) FROM ReadingRecord rr WHERE rr.book = :book AND rr.totalPages > 0")
    Double getAverageProgressByBook(@Param("book") Book book);
    
    @Query("SELECT AVG(DATEDIFF(rr.endDate, rr.startDate)) FROM ReadingRecord rr WHERE rr.status = 'COMPLETED' AND rr.startDate IS NOT NULL AND rr.endDate IS NOT NULL")
    Double getAverageCompletionTimeInDays();
    
    @Query("SELECT AVG(CAST(rr.currentPage AS double) / DATEDIFF(CURRENT_DATE, rr.startDate)) FROM ReadingRecord rr WHERE rr.startDate IS NOT NULL AND DATEDIFF(CURRENT_DATE, rr.startDate) > 0")
    Double getAveragePagesPerDay();
} 