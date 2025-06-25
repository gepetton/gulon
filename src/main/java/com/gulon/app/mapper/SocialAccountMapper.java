package com.gulon.app.mapper;

import com.gulon.app.dto.SocialAccountDto;
import com.gulon.app.entity.SocialAccount;
import com.gulon.app.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

/**
 * SocialAccount 엔티티와 DTO 간의 매핑을 담당하는 MapStruct 매퍼
 */
@Mapper(
    componentModel = "spring",  // Spring Bean으로 등록
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,  // null 값은 무시
    uses = {UserMapper.class}  // UserMapper 활용
)
public interface SocialAccountMapper {
    
    SocialAccountMapper INSTANCE = Mappers.getMapper(SocialAccountMapper.class);
    
    /**
     * CreateRequest DTO를 SocialAccount 엔티티로 변환
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)  // Service에서 별도 설정
    @Mapping(target = "linkedAt", ignore = true)  // @CreationTimestamp에서 자동 생성
    SocialAccount toEntity(SocialAccountDto.CreateRequest request);
    
    /**
     * SocialAccount 엔티티를 Response DTO로 변환
     */
    @Mapping(target = "userPublicId", source = "user.publicId")
    SocialAccountDto.Response toResponseDto(SocialAccount socialAccount);
    
    /**
     * SocialAccount 엔티티를 Summary DTO로 변환
     */
    SocialAccountDto.Summary toSummaryDto(SocialAccount socialAccount);
    
    /**
     * SocialAccount 엔티티 리스트를 Response DTO 리스트로 변환
     */
    List<SocialAccountDto.Response> toResponseDtoList(List<SocialAccount> socialAccounts);
    
    /**
     * SocialAccount 엔티티 리스트를 Summary DTO 리스트로 변환
     */
    List<SocialAccountDto.Summary> toSummaryDtoList(List<SocialAccount> socialAccounts);
    
    /**
     * UpdateRequest DTO의 값을 기존 SocialAccount 엔티티에 업데이트
     * null이 아닌 값만 업데이트됨
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerUserId", ignore = true)
    @Mapping(target = "linkedAt", ignore = true)
    void updateSocialAccountFromDto(SocialAccountDto.UpdateRequest request, @MappingTarget SocialAccount socialAccount);
    
    /**
     * 사용자별 소셜 계정 현황 DTO 생성
     */
    @Mapping(target = "userPublicId", source = "user.publicId")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "socialAccounts", source = "socialAccounts")
    @Mapping(target = "hasKakao", source = "socialAccounts", qualifiedByName = "hasKakao")
    @Mapping(target = "hasGoogle", source = "socialAccounts", qualifiedByName = "hasGoogle")
    @Mapping(target = "hasNaver", source = "socialAccounts", qualifiedByName = "hasNaver")
    SocialAccountDto.UserSocialAccounts toUserSocialAccountsDto(User user, List<SocialAccount> socialAccounts);
    
    /**
     * 카카오 연결 여부 확인
     */
    @Named("hasKakao")
    default boolean hasKakao(List<SocialAccount> socialAccounts) {
        return socialAccounts.stream()
                .anyMatch(sa -> sa.getProvider() == SocialAccount.Provider.KAKAO);
    }
    
    /**
     * 구글 연결 여부 확인
     */
    @Named("hasGoogle")
    default boolean hasGoogle(List<SocialAccount> socialAccounts) {
        return socialAccounts.stream()
                .anyMatch(sa -> sa.getProvider() == SocialAccount.Provider.GOOGLE);
    }
    
    /**
     * 네이버 연결 여부 확인
     */
    @Named("hasNaver")
    default boolean hasNaver(List<SocialAccount> socialAccounts) {
        return socialAccounts.stream()
                .anyMatch(sa -> sa.getProvider() == SocialAccount.Provider.NAVER);
    }
} 