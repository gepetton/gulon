package com.gulon.app.mapper;

import com.gulon.app.dto.GroupMemberDto;
import com.gulon.app.entity.GroupMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupMemberMapper {
    
    // Response DTO 매핑
    @Mapping(source = "group.publicId", target = "groupPublicId")
    @Mapping(source = "group.name", target = "groupName")
    @Mapping(source = "user.publicId", target = "userPublicId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    GroupMemberDto.Response toResponse(GroupMember groupMember);
    
    List<GroupMemberDto.Response> toResponseList(List<GroupMember> groupMembers);
    
    // Summary DTO 매핑
    @Mapping(source = "group.publicId", target = "groupPublicId")
    @Mapping(source = "group.name", target = "groupName")
    @Mapping(source = "user.publicId", target = "userPublicId")
    @Mapping(source = "user.name", target = "userName")
    GroupMemberDto.Summary toSummary(GroupMember groupMember);
    
    List<GroupMemberDto.Summary> toSummaryList(List<GroupMember> groupMembers);
    
    // MemberInfo DTO 매핑
    @Mapping(source = "user.publicId", target = "userPublicId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "groupMember.role", target = "role")
    @Mapping(source = "groupMember.status", target = "status")
    @Mapping(source = "groupMember.joinedAt", target = "joinedAt")
    @Mapping(source = "groupMember", target = "owner", qualifiedByName = "isOwner")
    GroupMemberDto.MemberInfo toMemberInfo(GroupMember groupMember);
    
    List<GroupMemberDto.MemberInfo> toMemberInfoList(List<GroupMember> groupMembers);
    
    // GroupInfo DTO 매핑
    @Mapping(source = "group.publicId", target = "groupPublicId")
    @Mapping(source = "group.name", target = "groupName")
    @Mapping(source = "group.description", target = "groupDescription")
    @Mapping(source = "groupMember.role", target = "userRole")
    @Mapping(source = "groupMember.status", target = "memberStatus")
    @Mapping(source = "groupMember.joinedAt", target = "joinedAt")
    @Mapping(source = "groupMember", target = "owner", qualifiedByName = "isOwner")
    GroupMemberDto.GroupInfo toGroupInfo(GroupMember groupMember);
    
    List<GroupMemberDto.GroupInfo> toGroupInfoList(List<GroupMember> groupMembers);
    
    // GroupMembersStatus 매핑을 위한 헬퍼 메소드들
    @Mapping(source = "group.publicId", target = "groupPublicId")
    @Mapping(source = "group.name", target = "groupName")
    @Mapping(source = "group.description", target = "groupDescription")
    @Mapping(source = "group.owner.publicId", target = "ownerPublicId")
    @Mapping(source = "group.owner.name", target = "ownerName")
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "totalMembers", ignore = true)
    @Mapping(target = "activeMembers", ignore = true)
    @Mapping(target = "leftMembers", ignore = true)
    @Mapping(target = "removedMembers", ignore = true)
    GroupMemberDto.GroupMembersStatus toGroupMembersStatus(GroupMember firstMember);
    
    // UserGroupsStatus 매핑을 위한 헬퍼 메소드들
    @Mapping(source = "user.publicId", target = "userPublicId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "totalGroups", ignore = true)
    @Mapping(target = "activeGroups", ignore = true)
    @Mapping(target = "ownedGroups", ignore = true)
    @Mapping(target = "adminGroups", ignore = true)
    GroupMemberDto.UserGroupsStatus toUserGroupsStatus(GroupMember firstMember);
    
    // Named 매핑 메소드들
    @Named("isOwner")
    default boolean isOwner(GroupMember groupMember) {
        return groupMember.getRole() == GroupMember.Role.OWNER;
    }
} 