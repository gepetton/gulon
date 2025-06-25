package com.gulon.app.service;

import com.gulon.app.dto.SocialAccountDto;
import com.gulon.app.entity.SocialAccount;
import com.gulon.app.entity.User;
import com.gulon.app.mapper.SocialAccountMapper;
import com.gulon.app.repository.SocialAccountRepository;
import com.gulon.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 소셜 계정 서비스 - MapStruct 활용
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SocialAccountService {
    
    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final SocialAccountMapper socialAccountMapper;
    
    @Transactional
    public SocialAccountDto.Response createSocialAccount(SocialAccountDto.CreateRequest request) {
        log.info("소셜 계정 연결 요청: userPublicId={}, provider={}", 
                request.getUserPublicId(), request.getProvider());
        
        // 사용자 조회
        User user = findUserByPublicIdOrThrow(request.getUserPublicId());
        
        // 중복 연결 확인
        validateSocialAccountLink(user, request.getProvider(), request.getProviderUserId());
        
        // 입력값 검증
        validateCreateRequest(request);
        
        // MapStruct를 사용한 엔티티 변환
        SocialAccount socialAccount = socialAccountMapper.toEntity(request);
        socialAccount.setUser(user);  // 사용자 설정
        
        SocialAccount savedAccount = socialAccountRepository.save(socialAccount);
        
        log.info("소셜 계정 연결 완료: id={}, userPublicId={}, provider={}", 
                savedAccount.getId(), user.getPublicId(), savedAccount.getProvider());
        
        return socialAccountMapper.toResponseDto(savedAccount);
    }
    
    @Transactional
    public SocialAccountDto.Response updateSocialAccount(Integer id, SocialAccountDto.UpdateRequest request) {
        log.info("소셜 계정 토큰 업데이트 요청: id={}", id);
        
        SocialAccount existingAccount = findSocialAccountByIdOrThrow(id);
        
        // MapStruct를 사용한 엔티티 업데이트
        socialAccountMapper.updateSocialAccountFromDto(request, existingAccount);
        
        SocialAccount updatedAccount = socialAccountRepository.save(existingAccount);
        
        log.info("소셜 계정 토큰 업데이트 완료: id={}", updatedAccount.getId());
        return socialAccountMapper.toResponseDto(updatedAccount);
    }
    
    public Optional<SocialAccountDto.Response> getSocialAccountById(Integer id) {
        log.debug("소셜 계정 조회: id={}", id);
        return socialAccountRepository.findById(id)
                .map(socialAccountMapper::toResponseDto);
    }
    
    public List<SocialAccountDto.Response> getUserSocialAccounts(UUID userPublicId) {
        log.debug("사용자 소셜 계정 조회: userPublicId={}", userPublicId);
        
        List<SocialAccount> socialAccounts = socialAccountRepository.findByUserPublicId(userPublicId);
        return socialAccountMapper.toResponseDtoList(socialAccounts);
    }
    
    public SocialAccountDto.UserSocialAccounts getUserSocialAccountsStatus(UUID userPublicId) {
        log.debug("사용자 소셜 계정 현황 조회: userPublicId={}", userPublicId);
        
        User user = findUserByPublicIdOrThrow(userPublicId);
        List<SocialAccount> socialAccounts = socialAccountRepository.findByUser(user);
        
        return socialAccountMapper.toUserSocialAccountsDto(user, socialAccounts);
    }
    
    @Transactional
    public SocialAccountDto.Response linkSocialAccount(UUID userPublicId, SocialAccountDto.CreateRequest request) {
        log.info("사용자 소셜 계정 연결: userPublicId={}, provider={}", userPublicId, request.getProvider());
        
        // userPublicId 설정
        request.setUserPublicId(userPublicId);
        
        return createSocialAccount(request);
    }
    
    @Transactional
    public void unlinkSocialAccount(UUID userPublicId, SocialAccount.Provider provider) {
        log.info("사용자 소셜 계정 연결 해제: userPublicId={}, provider={}", userPublicId, provider);
        
        SocialAccount socialAccount = socialAccountRepository
                .findByUserPublicIdAndProvider(userPublicId, provider)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("연결되지 않은 소셜 계정입니다: userPublicId=%s, provider=%s", 
                                userPublicId, provider)));
        
        socialAccountRepository.delete(socialAccount);
        
        log.info("소셜 계정 연결 해제 완료: id={}, userPublicId={}, provider={}", 
                socialAccount.getId(), userPublicId, provider);
    }
    
    @Transactional
    public void deleteSocialAccount(Integer id) {
        log.info("소셜 계정 삭제 요청: id={}", id);
        
        if (!socialAccountRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 소셜 계정입니다: " + id);
        }
        
        socialAccountRepository.deleteById(id);
        log.info("소셜 계정 삭제 완료: id={}", id);
    }
    
    @Transactional
    public SocialAccountDto.Response refreshTokens(Integer id, String newAccessToken, 
                                                   String newRefreshToken, LocalDateTime tokenExpiry) {
        log.info("토큰 갱신 요청: id={}", id);
        
        SocialAccount socialAccount = findSocialAccountByIdOrThrow(id);
        
        socialAccount.setAccessToken(newAccessToken);
        socialAccount.setRefreshToken(newRefreshToken);
        socialAccount.setTokenExpiry(tokenExpiry);
        
        SocialAccount updatedAccount = socialAccountRepository.save(socialAccount);
        
        log.info("토큰 갱신 완료: id={}", updatedAccount.getId());
        return socialAccountMapper.toResponseDto(updatedAccount);
    }
    
    public List<SocialAccountDto.Response> getSocialAccountsByProvider(SocialAccount.Provider provider) {
        log.debug("플랫폼별 소셜 계정 조회: provider={}", provider);
        
        List<SocialAccount> socialAccounts = socialAccountRepository.findByProvider(provider);
        return socialAccountMapper.toResponseDtoList(socialAccounts);
    }
    
    public List<SocialAccountDto.Response> getExpiredTokenAccounts() {
        log.debug("만료된 토큰 계정 조회");
        
        List<SocialAccount> expiredAccounts = socialAccountRepository
                .findByTokenExpiryBefore(LocalDateTime.now());
        return socialAccountMapper.toResponseDtoList(expiredAccounts);
    }
    
    public boolean isAccountLinked(UUID userPublicId, SocialAccount.Provider provider) {
        log.debug("소셜 계정 연결 여부 확인: userPublicId={}, provider={}", userPublicId, provider);
        
        return socialAccountRepository.existsByUserPublicIdAndProvider(userPublicId, provider);
    }
    
    public long getUserSocialAccountCount(UUID userPublicId) {
        log.debug("사용자 연결된 소셜 계정 수 조회: userPublicId={}", userPublicId);
        
        return socialAccountRepository.countByUserPublicId(userPublicId);
    }
    
    public long getProviderAccountCount(SocialAccount.Provider provider) {
        log.debug("플랫폼별 연결된 계정 수 조회: provider={}", provider);
        
        return socialAccountRepository.countByProvider(provider);
    }
    
    public List<SocialAccountDto.Summary> getRecentlyLinkedAccounts(int limit) {
        log.debug("최근 연결된 계정 조회: limit={}", limit);
        
        List<SocialAccount> recentAccounts = socialAccountRepository.findRecentlyLinked()
                .stream()
                .limit(limit)
                .toList();
        
        return socialAccountMapper.toSummaryDtoList(recentAccounts);
    }
    
    public List<SocialAccountDto.Summary> getAccountsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("기간별 연결된 계정 조회: start={}, end={}", startDate, endDate);
        
        List<SocialAccount> accounts = socialAccountRepository.findByLinkedAtBetween(startDate, endDate);
        return socialAccountMapper.toSummaryDtoList(accounts);
    }
    
    /**
     * ID로 소셜 계정 조회, 없으면 예외 발생
     */
    private SocialAccount findSocialAccountByIdOrThrow(Integer id) {
        return socialAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소셜 계정입니다: " + id));
    }
    
    /**
     * PublicId로 사용자 조회, 없으면 예외 발생
     */
    private User findUserByPublicIdOrThrow(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + publicId));
    }
    
    /**
     * 소셜 계정 연결 중복 검증
     */
    private void validateSocialAccountLink(User user, SocialAccount.Provider provider, String providerUserId) {
        // 동일 사용자가 같은 플랫폼에 중복 연결하는지 확인
        if (socialAccountRepository.existsByUserAndProvider(user, provider)) {
            throw new IllegalArgumentException(
                    String.format("이미 %s에 연결된 계정이 있습니다", provider.name()));
        }
        
        // 다른 사용자가 같은 소셜 계정을 사용하는지 확인
        if (socialAccountRepository.existsByProviderAndProviderUserId(provider, providerUserId)) {
            throw new IllegalArgumentException(
                    String.format("이미 다른 계정에 연결된 %s 계정입니다", provider.name()));
        }
    }
    
    /**
     * 소셜 계정 생성 요청 검증
     */
    private void validateCreateRequest(SocialAccountDto.CreateRequest request) {
        if (request.getUserPublicId() == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }
        
        if (request.getProvider() == null) {
            throw new IllegalArgumentException("소셜 플랫폼은 필수입니다");
        }
        
        if (request.getProviderUserId() == null || request.getProviderUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("소셜 플랫폼 사용자 ID는 필수입니다");
        }
        
        if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
            throw new IllegalArgumentException("액세스 토큰은 필수입니다");
        }
    }
} 