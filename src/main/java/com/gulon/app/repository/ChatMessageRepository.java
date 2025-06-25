package com.gulon.app.repository;

import com.gulon.app.entity.ChatMessage;
import com.gulon.app.entity.GroupTable;
import com.gulon.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    
    // publicId로 메시지 조회
    Optional<ChatMessage> findByPublicId(UUID publicId);
    
    // publicId 존재 여부 확인
    boolean existsByPublicId(UUID publicId);
    
    // 그룹별 메시지 조회
    List<ChatMessage> findByGroup(GroupTable group);
    
    // 그룹 publicId로 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId ORDER BY cm.sentAt DESC")
    List<ChatMessage> findByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 그룹 publicId로 메시지 조회 (페이징)
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.isDeleted = false ORDER BY cm.sentAt DESC")
    Page<ChatMessage> findByGroupPublicIdAndNotDeleted(@Param("groupPublicId") UUID groupPublicId, Pageable pageable);
    
    // 그룹 ID로 메시지 조회 (내부용)
    List<ChatMessage> findByGroupId(Integer groupId);
    
    // 사용자별 메시지 조회
    List<ChatMessage> findByUser(User user);
    
    // 사용자 publicId로 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.user.publicId = :userPublicId ORDER BY cm.sentAt DESC")
    List<ChatMessage> findByUserPublicId(@Param("userPublicId") UUID userPublicId);
    
    // 사용자 ID로 메시지 조회 (내부용)
    List<ChatMessage> findByUserId(Integer userId);
    
    // 그룹의 최근 메시지 조회 (시간 순 정렬)
    List<ChatMessage> findByGroupOrderBySentAtDesc(GroupTable group);
    
    // 그룹 publicId의 최근 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.isDeleted = false ORDER BY cm.sentAt DESC")
    List<ChatMessage> findRecentByGroupPublicId(@Param("groupPublicId") UUID groupPublicId, Pageable pageable);
    
    // 그룹의 특정 시간 이후 메시지 조회
    List<ChatMessage> findByGroupAndSentAtAfter(GroupTable group, LocalDateTime after);
    
    // 그룹 publicId의 특정 시간 이후 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.sentAt > :after AND cm.isDeleted = false ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByGroupPublicIdAndSentAtAfter(@Param("groupPublicId") UUID groupPublicId, @Param("after") LocalDateTime after);
    
    // 특정 기간의 메시지 조회
    List<ChatMessage> findBySentAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 그룹의 특정 기간 메시지 조회
    List<ChatMessage> findByGroupAndSentAtBetween(GroupTable group, LocalDateTime startDate, LocalDateTime endDate);
    
    // 그룹 publicId의 특정 기간 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.sentAt BETWEEN :startDate AND :endDate ORDER BY cm.sentAt DESC")
    List<ChatMessage> findByGroupPublicIdAndSentAtBetween(@Param("groupPublicId") UUID groupPublicId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 최근 N개 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group = :group ORDER BY cm.sentAt DESC LIMIT :limit")
    List<ChatMessage> findRecentMessagesByGroup(@Param("group") GroupTable group, @Param("limit") int limit);
    
    // 그룹 publicId의 최근 N개 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.isDeleted = false ORDER BY cm.sentAt DESC LIMIT :limit")
    List<ChatMessage> findRecentMessagesByGroupPublicId(@Param("groupPublicId") UUID groupPublicId, @Param("limit") int limit);
    
    // 그룹의 메시지 수 조회
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.group = :group")
    long countMessagesByGroup(@Param("group") GroupTable group);
    
    // 그룹 publicId의 메시지 수 조회
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId")
    long countMessagesByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 그룹 publicId의 활성 메시지 수 조회
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.isDeleted = false")
    long countActiveMessagesByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 사용자의 메시지 수 조회
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.user = :user")
    long countMessagesByUser(@Param("user") User user);
    
    // 사용자 publicId의 메시지 수 조회
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.user.publicId = :userPublicId")
    long countMessagesByUserPublicId(@Param("userPublicId") UUID userPublicId);
    
    // 편집된 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.editedAt IS NOT NULL")
    List<ChatMessage> findEditedMessages();
    
    // 그룹의 편집된 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group = :group AND cm.editedAt IS NOT NULL")
    List<ChatMessage> findEditedMessagesByGroup(@Param("group") GroupTable group);
    
    // 그룹 publicId의 편집된 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.editedAt IS NOT NULL")
    List<ChatMessage> findEditedMessagesByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 키워드로 메시지 검색
    @Query("SELECT cm FROM ChatMessage cm WHERE " +
           "LOWER(cm.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND cm.isDeleted = false")
    List<ChatMessage> searchByKeyword(@Param("keyword") String keyword);
    
    // 그룹 내 키워드 검색
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group = :group AND " +
           "LOWER(cm.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND cm.isDeleted = false")
    List<ChatMessage> searchByKeywordInGroup(@Param("group") GroupTable group, @Param("keyword") String keyword);
    
    // 그룹 publicId 내 키워드 검색
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND " +
           "LOWER(cm.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND cm.isDeleted = false")
    List<ChatMessage> searchByKeywordInGroupPublicId(@Param("groupPublicId") UUID groupPublicId, @Param("keyword") String keyword);
    
    // 오늘의 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE DATE(cm.sentAt) = CURRENT_DATE AND cm.isDeleted = false")
    List<ChatMessage> findTodayMessages();
    
    // 그룹의 오늘 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group = :group AND DATE(cm.sentAt) = CURRENT_DATE AND cm.isDeleted = false")
    List<ChatMessage> findTodayMessagesByGroup(@Param("group") GroupTable group);
    
    // 그룹 publicId의 오늘 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND DATE(cm.sentAt) = CURRENT_DATE AND cm.isDeleted = false")
    List<ChatMessage> findTodayMessagesByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 사용자의 그룹별 메시지 조회
    List<ChatMessage> findByGroupAndUser(GroupTable group, User user);
    
    // 그룹 publicId와 사용자 publicId로 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.user.publicId = :userPublicId ORDER BY cm.sentAt DESC")
    List<ChatMessage> findByGroupPublicIdAndUserPublicId(@Param("groupPublicId") UUID groupPublicId, @Param("userPublicId") UUID userPublicId);
    
    // 메시지 타입별 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.type = :type AND cm.isDeleted = false ORDER BY cm.sentAt DESC")
    List<ChatMessage> findByGroupPublicIdAndType(@Param("groupPublicId") UUID groupPublicId, @Param("type") ChatMessage.MessageType type);
    
    // 삭제된 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.isDeleted = true")
    List<ChatMessage> findDeletedMessagesByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 그룹의 마지막 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.isDeleted = false ORDER BY cm.sentAt DESC LIMIT 1")
    Optional<ChatMessage> findLastMessageByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 특정 메시지 이후의 메시지들 조회 (무한 스크롤용)
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group.publicId = :groupPublicId AND cm.sentAt < :beforeTime AND cm.isDeleted = false ORDER BY cm.sentAt DESC")
    Page<ChatMessage> findByGroupPublicIdBeforeTime(@Param("groupPublicId") UUID groupPublicId, @Param("beforeTime") LocalDateTime beforeTime, Pageable pageable);
} 