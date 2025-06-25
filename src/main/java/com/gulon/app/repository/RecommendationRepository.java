package com.gulon.app.repository;

import com.gulon.app.entity.Book;
import com.gulon.app.entity.Recommendation;
import com.gulon.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {
    
    // 사용자별 추천 조회
    List<Recommendation> findByUser(User user);
    
    // 사용자 ID로 추천 조회
    List<Recommendation> findByUserId(Integer userId);
    
    // 도서별 추천 조회
    List<Recommendation> findByBook(Book book);
    
    // 사용자와 도서로 추천 조회
    Optional<Recommendation> findByUserAndBook(User user, Book book);
    
    // 추천 소스별 조회
    List<Recommendation> findBySource(Recommendation.Source source);
    
    // 사용자의 특정 소스 추천 조회
    List<Recommendation> findByUserAndSource(User user, Recommendation.Source source);
    
    // 특정 기간의 추천 조회
    List<Recommendation> findByRecommendedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 사용자의 최근 추천 조회
    @Query("SELECT r FROM Recommendation r WHERE r.user = :user ORDER BY r.recommendedAt DESC")
    List<Recommendation> findRecentRecommendationsByUser(@Param("user") User user);
    
    // 도서의 추천 수 조회
    @Query("SELECT COUNT(r) FROM Recommendation r WHERE r.book = :book")
    long countRecommendationsByBook(@Param("book") Book book);
    
    // 사용자별 추천 수 조회
    @Query("SELECT COUNT(r) FROM Recommendation r WHERE r.user = :user")
    long countRecommendationsByUser(@Param("user") User user);
    
    // 챗봇 추천 조회
    @Query("SELECT r FROM Recommendation r WHERE r.source = 'CHATBOT' ORDER BY r.recommendedAt DESC")
    List<Recommendation> findChatbotRecommendations();
    
    // 수동 추천 조회
    @Query("SELECT r FROM Recommendation r WHERE r.source = 'MANUAL' ORDER BY r.recommendedAt DESC")
    List<Recommendation> findManualRecommendations();
    
    // 가장 많이 추천된 도서 조회
    @Query("SELECT r.book, COUNT(r) as recommendCount FROM Recommendation r " +
           "GROUP BY r.book ORDER BY recommendCount DESC")
    List<Object[]> findMostRecommendedBooks();
    
    // 특정 사용자에게 이미 추천된 도서인지 확인
    boolean existsByUserAndBook(User user, Book book);
    
    // 오늘의 추천 조회
    @Query("SELECT r FROM Recommendation r WHERE DATE(r.recommendedAt) = CURRENT_DATE")
    List<Recommendation> findTodayRecommendations();
    
    // 이유가 있는 추천 조회
    @Query("SELECT r FROM Recommendation r WHERE r.reason IS NOT NULL AND r.reason != ''")
    List<Recommendation> findRecommendationsWithReason();
} 