package com.gulon.app.repository;

import com.gulon.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);
    
    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
    
    // PublicId로 사용자 조회
    Optional<User> findByPublicId(UUID publicId);
    
    // PublicId 존재 여부 확인
    boolean existsByPublicId(UUID publicId);
    
    // 상태별 사용자 조회
    List<User> findByStatus(User.UserStatus status);
    
    // 이름으로 사용자 검색 (부분 일치)
    List<User> findByNameContainingIgnoreCase(String name);
    
    // 특정 기간에 가입한 사용자 조회
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 활성 사용자 수 조회
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.UserStatus status);
    
    // 최근 가입한 사용자 조회
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findRecentUsers();
    
    // 이메일과 상태로 사용자 조회
    Optional<User> findByEmailAndStatus(String email, User.UserStatus status);
} 