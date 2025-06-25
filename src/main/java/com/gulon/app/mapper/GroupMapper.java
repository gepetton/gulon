package com.gulon.app.mapper;

import com.gulon.app.dto.GroupDto;
import com.gulon.app.entity.GroupTable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    
    // Response DTO 매핑
    @Mapping(source = "owner.publicId", target = "ownerPublicId")
    @Mapping(source = "owner.name", target = "ownerName")
    @Mapping(source = "owner.email", target = "ownerEmail")
    @Mapping(target = "memberCount", ignore = true)
    @Mapping(target = "activeMemberCount", ignore = true)
    GroupDto.Response toResponse(GroupTable group);
    
    List<GroupDto.Response> toResponseList(List<GroupTable> groups);
    
    // Summary DTO 매핑
    @Mapping(source = "owner.publicId", target = "ownerPublicId")
    @Mapping(source = "owner.name", target = "ownerName")
    @Mapping(target = "memberCount", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "member", ignore = true)
    GroupDto.Summary toSummary(GroupTable group);
    
    List<GroupDto.Summary> toSummaryList(List<GroupTable> groups);
    
    // PublicInfo DTO 매핑
    @Mapping(source = "owner.name", target = "ownerName")
    @Mapping(target = "memberCount", ignore = true)
    @Mapping(target = "member", ignore = true)
    GroupDto.PublicInfo toPublicInfo(GroupTable group);
    
    List<GroupDto.PublicInfo> toPublicInfoList(List<GroupTable> groups);
    
    // CreateResponse DTO 매핑
    @Mapping(source = "owner.publicId", target = "ownerPublicId")
    @Mapping(source = "owner.name", target = "ownerName")
    @Mapping(target = "message", ignore = true)
    GroupDto.CreateResponse toCreateResponse(GroupTable group);
    
    // Statistics DTO 매핑
    @Mapping(target = "totalMembers", ignore = true)
    @Mapping(target = "activeMembers", ignore = true)
    @Mapping(target = "leftMembers", ignore = true)
    @Mapping(target = "removedMembers", ignore = true)
    @Mapping(target = "ownerCount", ignore = true)
    @Mapping(target = "adminCount", ignore = true)
    @Mapping(target = "memberCount", ignore = true)
    @Mapping(target = "lastActivityAt", ignore = true)
    GroupDto.Statistics toStatistics(GroupTable group);
    
    // CreateRequest에서 GroupTable로 매핑
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GroupTable toEntity(GroupDto.CreateRequest createRequest);
    
    // UpdateRequest를 기존 GroupTable에 적용
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(GroupDto.UpdateRequest updateRequest, @org.mapstruct.MappingTarget GroupTable group);
} 