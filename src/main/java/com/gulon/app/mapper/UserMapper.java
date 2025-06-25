package com.gulon.app.mapper;

import com.gulon.app.dto.UserDto;
import com.gulon.app.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * User 엔티티와 DTO 간의 매핑을 담당하는 MapStruct 매퍼
 */
@Mapper(
    componentModel = "spring",  // Spring Bean으로 등록
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE  // null 값은 무시
)
public interface UserMapper {
    
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    /**
     * CreateRequest DTO를 User 엔티티로 변환
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)  // @PrePersist에서 자동 생성
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDto.CreateRequest request);
    
    /**
     * User 엔티티를 Response DTO로 변환
     */
    UserDto.Response toResponseDto(User user);
    
    /**
     * User 엔티티를 Summary DTO로 변환
     */
    UserDto.Summary toSummaryDto(User user);
    
    /**
     * User 엔티티를 PublicProfile DTO로 변환
     */
    UserDto.PublicProfile toPublicProfileDto(User user);
    
    /**
     * User 엔티티 리스트를 Response DTO 리스트로 변환
     */
    List<UserDto.Response> toResponseDtoList(List<User> users);
    
    /**
     * User 엔티티 리스트를 Summary DTO 리스트로 변환
     */
    List<UserDto.Summary> toSummaryDtoList(List<User> users);
    
    /**
     * User 엔티티 리스트를 PublicProfile DTO 리스트로 변환
     */
    List<UserDto.PublicProfile> toPublicProfileDtoList(List<User> users);
    
    /**
     * UpdateRequest DTO의 값을 기존 User 엔티티에 업데이트
     * null이 아닌 값만 업데이트됨
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)  // publicId는 변경 불가
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromDto(UserDto.UpdateRequest request, @MappingTarget User user);
} 