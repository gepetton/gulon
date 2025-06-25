package com.gulon.app.repository;

import com.gulon.app.entity.SocialAccount;
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
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Integer> {
    
    // 사용자별 소셜 계정 조회
    List<SocialAccount> findByUser(User user);
    
    // 사용자의 publicId로 소셜 계정 조회
    @Query("SELECT sa FROM SocialAccount sa WHERE sa.user.publicId = :publicId")
    List<SocialAccount> findByUserPublicId(@Param("publicId") UUID publicId);
    
    // 특정 사용자의 특정 플랫폼 계정 조회
    Optional<SocialAccount> findByUserAndProvider(User user, SocialAccount.Provider provider);
    
    // publicId와 플랫폼으로 계정 조회
    @Query("SELECT sa FROM SocialAccount sa WHERE sa.user.publicId = :publicId AND sa.provider = :provider")
    Optional<SocialAccount> findByUserPublicIdAndProvider(
            @Param("publicId") UUID publicId, 
            @Param("provider") SocialAccount.Provider provider);
    
    // 플랫폼별 계정 조회
    List<SocialAccount> findByProvider(SocialAccount.Provider provider);
    
    // 플랫폼과 플랫폼 사용자 ID로 계정 조회 (중복 방지용)
    Optional<SocialAccount> findByProviderAndProviderUserId(
            SocialAccount.Provider provider, 
            String providerUserId);
    
    // 사용자가 특정 플랫폼에 연결되어 있는지 확인
    boolean existsByUserAndProvider(User user, SocialAccount.Provider provider);
    
    // publicId로 특정 플랫폼 연결 여부 확인
    @Query("SELECT COUNT(sa) > 0 FROM SocialAccount sa WHERE sa.user.publicId = :publicId AND sa.provider = :provider")
    boolean existsByUserPublicIdAndProvider(
            @Param("publicId") UUID publicId, 
            @Param("provider") SocialAccount.Provider provider);
    
    // 플랫폼 사용자 ID 중복 확인
    boolean existsByProviderAndProviderUserId(
            SocialAccount.Provider provider, 
            String providerUserId);
    
    // 만료된 토큰을 가진 계정 조회
    List<SocialAccount> findByTokenExpiryBefore(LocalDateTime expiry);
    
    // 사용자별 연결된 플랫폼 수 조회
    @Query("SELECT COUNT(sa) FROM SocialAccount sa WHERE sa.user.publicId = :publicId")
    long countByUserPublicId(@Param("publicId") UUID publicId);
    
    // 플랫폼별 연결된 사용자 수 조회
    long countByProvider(SocialAccount.Provider provider);
    
    // 최근 연결된 계정 조회
    @Query("SELECT sa FROM SocialAccount sa ORDER BY sa.linkedAt DESC")
    List<SocialAccount> findRecentlyLinked();
    
    // 특정 기간에 연결된 계정 조회
    List<SocialAccount> findByLinkedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
} 