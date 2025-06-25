package com.gulon.app.repository;

import com.gulon.app.entity.GroupTable;
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
public interface GroupTableRepository extends JpaRepository<GroupTable, Integer> {
    
    // publicId로 그룹 조회
    Optional<GroupTable> findByPublicId(UUID publicId);
    
    // publicId 존재 여부 확인
    boolean existsByPublicId(UUID publicId);
    
    // 소유자별 그룹 조회
    List<GroupTable> findByOwner(User owner);
    
    // 소유자 publicId로 그룹 조회
    @Query("SELECT g FROM GroupTable g WHERE g.owner.publicId = :ownerPublicId")
    List<GroupTable> findByOwnerPublicId(@Param("ownerPublicId") UUID ownerPublicId);
    
    // 소유자 ID로 그룹 조회 (내부용)
    List<GroupTable> findByOwnerId(Integer ownerId);
    
    // 그룹명으로 검색 (부분 일치)
    List<GroupTable> findByNameContainingIgnoreCase(String name);
    
    // 공개/비공개별 그룹 조회
    List<GroupTable> findByPrivacy(GroupTable.Privacy privacy);
    
    // 공개 그룹만 조회
    List<GroupTable> findByPrivacyOrderByCreatedAtDesc(GroupTable.Privacy privacy);
    
    // 특정 기간에 생성된 그룹 조회
    List<GroupTable> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 그룹명과 공개 여부로 검색
    List<GroupTable> findByNameContainingIgnoreCaseAndPrivacy(String name, GroupTable.Privacy privacy);
    
    // 최근 생성된 그룹 조회
    @Query("SELECT g FROM GroupTable g ORDER BY g.createdAt DESC")
    List<GroupTable> findRecentlyCreated();
    
    // 소유자별 그룹 수 조회
    @Query("SELECT COUNT(g) FROM GroupTable g WHERE g.owner = :owner")
    long countByOwner(@Param("owner") User owner);
    
    // 소유자 publicId별 그룹 수 조회
    @Query("SELECT COUNT(g) FROM GroupTable g WHERE g.owner.publicId = :ownerPublicId")
    long countByOwnerPublicId(@Param("ownerPublicId") UUID ownerPublicId);
    
    // 공개 그룹 수 조회
    @Query("SELECT COUNT(g) FROM GroupTable g WHERE g.privacy = 'PUBLIC'")
    long countPublicGroups();
    
    // 활성 그룹 조회 (최근 30일 내 업데이트)
    @Query("SELECT g FROM GroupTable g WHERE g.updatedAt >= :thirtyDaysAgo")
    List<GroupTable> findActiveGroups(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    // 키워드로 그룹 검색 (이름 + 설명)
    @Query("SELECT g FROM GroupTable g WHERE " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<GroupTable> searchByKeyword(@Param("keyword") String keyword);
    
    // 공개 그룹 중 키워드 검색
    @Query("SELECT g FROM GroupTable g WHERE g.privacy = 'PUBLIC' AND " +
           "(LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<GroupTable> searchPublicGroupsByKeyword(@Param("keyword") String keyword);
} 