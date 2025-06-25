package com.gulon.app.repository;

import com.gulon.app.entity.GroupMember;
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
public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMember.GroupMemberId> {
    
    // 그룹별 멤버 조회
    List<GroupMember> findByGroup(GroupTable group);
    
    // 그룹 publicId로 멤버 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId")
    List<GroupMember> findByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 그룹 ID로 멤버 조회 (내부용)
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId")
    List<GroupMember> findByGroupId(@Param("groupId") Integer groupId);
    
    // 사용자별 가입 그룹 조회
    List<GroupMember> findByUser(User user);
    
    // 사용자 publicId로 가입 그룹 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.publicId = :publicId")
    List<GroupMember> findByUserPublicId(@Param("publicId") UUID publicId);
    
    // 특정 그룹의 특정 사용자 멤버십 조회
    Optional<GroupMember> findByGroupAndUser(GroupTable group, User user);
    
    // 그룹 publicId와 사용자 publicId로 멤버십 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId AND gm.user.publicId = :userPublicId")
    Optional<GroupMember> findByGroupPublicIdAndUserPublicId(@Param("groupPublicId") UUID groupPublicId, @Param("userPublicId") UUID userPublicId);
    
    // 그룹별 활성 멤버 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId AND gm.status = :status")
    List<GroupMember> findByGroupPublicIdAndStatus(@Param("groupPublicId") UUID groupPublicId, @Param("status") GroupMember.MemberStatus status);
    
    // 사용자별 활성 그룹 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.publicId = :publicId AND gm.status = :status")
    List<GroupMember> findByUserPublicIdAndStatus(@Param("publicId") UUID publicId, @Param("status") GroupMember.MemberStatus status);
    
    // 역할별 멤버 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId AND gm.role = :role")
    List<GroupMember> findByGroupPublicIdAndRole(@Param("groupPublicId") UUID groupPublicId, @Param("role") GroupMember.Role role);
    
    // 그룹 소유자 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId AND gm.role = 'OWNER'")
    Optional<GroupMember> findGroupOwner(@Param("groupPublicId") UUID groupPublicId);
    
    // 그룹 관리자들 조회 (OWNER + ADMIN)
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId AND gm.role IN ('OWNER', 'ADMIN') AND gm.status = 'ACTIVE'")
    List<GroupMember> findGroupAdmins(@Param("groupPublicId") UUID groupPublicId);
    
    // 사용자가 소유한 그룹들 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.publicId = :publicId AND gm.role = 'OWNER'")
    List<GroupMember> findOwnedGroupsByUser(@Param("publicId") UUID publicId);
    
    // 멤버십 존재 여부 확인
    boolean existsByGroupAndUser(GroupTable group, User user);
    
    // 그룹 publicId와 사용자 publicId로 멤버십 존재 여부 확인
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId AND gm.user.publicId = :userPublicId")
    boolean existsByGroupPublicIdAndUserPublicId(@Param("groupPublicId") UUID groupPublicId, @Param("userPublicId") UUID userPublicId);
    
    // 활성 멤버십 존재 여부 확인
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId AND gm.user.publicId = :userPublicId AND gm.status = 'ACTIVE'")
    boolean existsActiveMembership(@Param("groupPublicId") UUID groupPublicId, @Param("userPublicId") UUID userPublicId);
    
    // 그룹별 멤버 수 조회
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId")
    long countByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 그룹별 활성 멤버 수 조회
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId AND gm.status = 'ACTIVE'")
    long countActiveByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 사용자별 가입 그룹 수 조회
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.user.publicId = :publicId")
    long countByUserPublicId(@Param("publicId") UUID publicId);
    
    // 사용자별 활성 그룹 수 조회
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.user.publicId = :publicId AND gm.status = 'ACTIVE'")
    long countActiveByUserPublicId(@Param("publicId") UUID publicId);
    
    // 특정 기간에 가입한 멤버 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId AND gm.joinedAt BETWEEN :startDate AND :endDate")
    List<GroupMember> findByGroupPublicIdAndJoinedAtBetween(
            @Param("groupPublicId") UUID groupPublicId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    // 최근 가입한 멤버 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId ORDER BY gm.joinedAt DESC")
    List<GroupMember> findRecentMembersByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
    
    // 사용자가 관리자 권한을 가진 그룹들 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.publicId = :publicId AND gm.role IN ('OWNER', 'ADMIN') AND gm.status = 'ACTIVE'")
    List<GroupMember> findAdminGroupsByUser(@Param("publicId") UUID publicId);
    
    // 그룹에서 탈퇴하거나 제거된 멤버들 조회
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.publicId = :groupPublicId AND gm.status IN ('LEFT', 'REMOVED')")
    List<GroupMember> findInactiveMembersByGroupPublicId(@Param("groupPublicId") UUID groupPublicId);
} 