package com.gulon.app.service;

import com.gulon.app.dto.GroupDto;
import com.gulon.app.entity.GroupMember;
import com.gulon.app.entity.GroupTable;
import com.gulon.app.entity.User;
import com.gulon.app.mapper.GroupMapper;
import com.gulon.app.repository.GroupMemberRepository;
import com.gulon.app.repository.GroupTableRepository;
import com.gulon.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupService {
    
    private final GroupTableRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMapper groupMapper;
    
    /**
     * 그룹 생성
     */
    @Transactional
    public GroupDto.CreateResponse createGroup(GroupDto.CreateRequest request) {
        log.info("Creating group with name: {}", request.getName());
        
        // 소유자 조회
        User owner = userRepository.findByPublicId(request.getOwnerPublicId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getOwnerPublicId()));
        
        // 소유자의 동일한 이름 그룹 존재 여부 확인
        List<GroupTable> existingGroups = groupRepository.findByOwnerPublicId(request.getOwnerPublicId());
        boolean duplicateName = existingGroups.stream()
                .anyMatch(group -> group.getName().equals(request.getName()));
        
        if (duplicateName) {
            throw new IllegalArgumentException("이미 동일한 이름의 그룹이 존재합니다: " + request.getName());
        }
        
        // 그룹 생성
        GroupTable group = new GroupTable(
                request.getName(), 
                request.getDescription(), 
                owner, 
                request.getPrivacy() != null ? request.getPrivacy() : GroupTable.Privacy.PRIVATE
        );
        
        GroupTable savedGroup = groupRepository.save(group);
        
        // 소유자를 OWNER 역할로 GroupMember에 추가
        GroupMember ownerMember = new GroupMember(savedGroup, owner, GroupMember.Role.OWNER);
        groupMemberRepository.save(ownerMember);
        
        // 응답 생성
        GroupDto.CreateResponse response = groupMapper.toCreateResponse(savedGroup);
        response.setMessage("그룹이 성공적으로 생성되었습니다.");
        
        log.info("Group created successfully with publicId: {}", savedGroup.getPublicId());
        return response;
    }
    
    /**
     * 그룹 상세 조회
     */
    public Optional<GroupDto.Response> getGroup(UUID groupPublicId, UUID requestUserPublicId) {
        log.info("Getting group details for publicId: {}", groupPublicId);
        
        Optional<GroupTable> groupOpt = groupRepository.findByPublicId(groupPublicId);
        if (groupOpt.isEmpty()) {
            return Optional.empty();
        }
        
        GroupTable group = groupOpt.get();
        
        // 비공개 그룹의 경우 멤버 권한 확인
        if (group.getPrivacy() == GroupTable.Privacy.PRIVATE && requestUserPublicId != null) {
            boolean isMember = groupMemberRepository.existsActiveMembership(groupPublicId, requestUserPublicId);
            if (!isMember) {
                return Optional.empty(); // 권한 없음을 나타내기 위해 empty 반환
            }
        }
        
        GroupDto.Response response = groupMapper.toResponse(group);
        
        // 멤버 수 설정
        response.setMemberCount(groupMemberRepository.countByGroupPublicId(groupPublicId));
        response.setActiveMemberCount(groupMemberRepository.countActiveByGroupPublicId(groupPublicId));
        
        return Optional.of(response);
    }
    
    /**
     * 그룹 수정
     */
    @Transactional
    public GroupDto.Response updateGroup(UUID groupPublicId, GroupDto.UpdateRequest request, UUID requestUserPublicId) {
        log.info("Updating group: {}", groupPublicId);
        
        GroupTable group = groupRepository.findByPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다: " + groupPublicId));
        
        // 소유자 권한 확인
        if (!group.getOwner().getPublicId().equals(requestUserPublicId)) {
            throw new IllegalArgumentException("그룹을 수정할 권한이 없습니다.");
        }
        
        // 그룹명 중복 확인 (변경되는 경우)
        if (request.getName() != null && !group.getName().equals(request.getName())) {
            List<GroupTable> ownerGroups = groupRepository.findByOwnerPublicId(requestUserPublicId);
            boolean duplicateName = ownerGroups.stream()
                    .anyMatch(g -> !g.getPublicId().equals(groupPublicId) && g.getName().equals(request.getName()));
            
            if (duplicateName) {
                throw new IllegalArgumentException("이미 동일한 이름의 그룹이 존재합니다: " + request.getName());
            }
        }
        
        // 그룹 정보 업데이트
        groupMapper.updateEntity(request, group);
        GroupTable updatedGroup = groupRepository.save(group);
        
        GroupDto.Response response = groupMapper.toResponse(updatedGroup);
        response.setMemberCount(groupMemberRepository.countByGroupPublicId(groupPublicId));
        response.setActiveMemberCount(groupMemberRepository.countActiveByGroupPublicId(groupPublicId));
        
        log.info("Group updated successfully: {}", groupPublicId);
        return response;
    }
    
    /**
     * 그룹 삭제
     */
    @Transactional
    public void deleteGroup(UUID groupPublicId, UUID requestUserPublicId) {
        log.info("Deleting group: {}", groupPublicId);
        
        GroupTable group = groupRepository.findByPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다: " + groupPublicId));
        
        // 소유자 권한 확인
        if (!group.getOwner().getPublicId().equals(requestUserPublicId)) {
            throw new IllegalArgumentException("그룹을 삭제할 권한이 없습니다.");
        }
        
        // 그룹 멤버들 삭제 (CASCADE 관계 없이 직접 삭제)
        List<GroupMember> members = groupMemberRepository.findByGroupPublicId(groupPublicId);
        groupMemberRepository.deleteAll(members);
        
        // 그룹 삭제
        groupRepository.delete(group);
        
        log.info("Group deleted successfully: {}", groupPublicId);
    }
    
    /**
     * 사용자별 소유 그룹 조회
     */
    public List<GroupDto.Summary> getOwnedGroups(UUID userPublicId) {
        log.info("Getting owned groups for user: {}", userPublicId);
        
        List<GroupTable> groups = groupRepository.findByOwnerPublicId(userPublicId);
        List<GroupDto.Summary> summaries = groupMapper.toSummaryList(groups);
        
        // 각 그룹의 멤버 수와 소유 정보 설정
        summaries.forEach(summary -> {
            summary.setMemberCount(groupMemberRepository.countByGroupPublicId(summary.getPublicId()));
            summary.setOwner(true);
            summary.setMember(true);
        });
        
        return summaries;
    }
    
    /**
     * 사용자별 가입 그룹 조회
     */
    public List<GroupDto.Summary> getUserGroups(UUID userPublicId) {
        log.info("Getting user groups for: {}", userPublicId);
        
        List<GroupMember> memberships = groupMemberRepository.findByUserPublicIdAndStatus(
                userPublicId, GroupMember.MemberStatus.ACTIVE);
        
        return memberships.stream()
                .map(membership -> {
                    GroupDto.Summary summary = groupMapper.toSummary(membership.getGroup());
                    summary.setMemberCount(groupMemberRepository.countByGroupPublicId(summary.getPublicId()));
                    summary.setOwner(membership.getRole() == GroupMember.Role.OWNER);
                    summary.setMember(true);
                    return summary;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 공개 그룹 목록 조회
     */
    public GroupDto.SearchResult getPublicGroups(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting public groups - page: {}, size: {}", page, size);
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        
        String sortField = switch (sortBy != null ? sortBy : "created_at") {
            case "name" -> "name";
            case "member_count" -> "createdAt"; // member_count는 계산 필드라 createdAt 사용
            default -> "createdAt";
        };
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        List<GroupTable> groups = groupRepository.findByPrivacyOrderByCreatedAtDesc(GroupTable.Privacy.PUBLIC);
        
        // 페이징 처리 (간단한 구현)
        int start = page * size;
        int end = Math.min(start + size, groups.size());
        List<GroupTable> pageGroups = groups.subList(start, end);
        
        List<GroupDto.PublicInfo> publicInfos = groupMapper.toPublicInfoList(pageGroups);
        
        // 멤버 수 설정
        publicInfos.forEach(info -> {
            info.setMemberCount(groupMemberRepository.countByGroupPublicId(info.getPublicId()));
            info.setMember(false);
        });
        
        GroupDto.SearchResult result = new GroupDto.SearchResult();
        result.setGroups(publicInfos);
        result.setTotalCount(groups.size());
        result.setCurrentPage(page);
        result.setPageSize(size);
        result.setTotalPages((int) Math.ceil((double) groups.size() / size));
        result.setHasNext(end < groups.size());
        result.setHasPrevious(page > 0);
        
        return result;
    }
    
    /**
     * 그룹 검색
     */
    public GroupDto.SearchResult searchGroups(String keyword, int page, int size) {
        log.info("Searching groups with keyword: {}", keyword);
        
        List<GroupTable> groups;
        if (keyword == null || keyword.trim().isEmpty()) {
            groups = groupRepository.findByPrivacyOrderByCreatedAtDesc(GroupTable.Privacy.PUBLIC);
        } else {
            groups = groupRepository.searchPublicGroupsByKeyword(keyword.trim());
        }
        
        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, groups.size());
        List<GroupTable> pageGroups = groups.subList(start, end);
        
        List<GroupDto.PublicInfo> publicInfos = groupMapper.toPublicInfoList(pageGroups);
        
        // 멤버 수 설정
        publicInfos.forEach(info -> {
            info.setMemberCount(groupMemberRepository.countByGroupPublicId(info.getPublicId()));
            info.setMember(false);
        });
        
        GroupDto.SearchResult result = new GroupDto.SearchResult();
        result.setGroups(publicInfos);
        result.setTotalCount(groups.size());
        result.setCurrentPage(page);
        result.setPageSize(size);
        result.setTotalPages((int) Math.ceil((double) groups.size() / size));
        result.setHasNext(end < groups.size());
        result.setHasPrevious(page > 0);
        
        return result;
    }
    
    /**
     * 그룹 통계 조회
     */
    public GroupDto.Statistics getGroupStatistics(UUID groupPublicId, UUID requestUserPublicId) {
        log.info("Getting group statistics for: {}", groupPublicId);
        
        GroupTable group = groupRepository.findByPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다: " + groupPublicId));
        
        // 소유자 또는 관리자 권한 확인
        GroupMember membership = groupMemberRepository.findByGroupPublicIdAndUserPublicId(groupPublicId, requestUserPublicId)
                .orElseThrow(() -> new IllegalArgumentException("그룹에 접근할 권한이 없습니다."));
        
        if (membership.getRole() != GroupMember.Role.OWNER && membership.getRole() != GroupMember.Role.ADMIN) {
            throw new IllegalArgumentException("통계를 조회할 권한이 없습니다.");
        }
        
        GroupDto.Statistics stats = groupMapper.toStatistics(group);
        
        // 통계 정보 설정
        stats.setTotalMembers(groupMemberRepository.countByGroupPublicId(groupPublicId));
        stats.setActiveMembers(groupMemberRepository.countActiveByGroupPublicId(groupPublicId));
        
        List<GroupMember> allMembers = groupMemberRepository.findByGroupPublicId(groupPublicId);
        stats.setLeftMembers(allMembers.stream()
                .filter(member -> member.getStatus() == GroupMember.MemberStatus.LEFT)
                .count());
        stats.setRemovedMembers(allMembers.stream()
                .filter(member -> member.getStatus() == GroupMember.MemberStatus.REMOVED)
                .count());
        stats.setOwnerCount(allMembers.stream()
                .filter(member -> member.getRole() == GroupMember.Role.OWNER)
                .count());
        stats.setAdminCount(allMembers.stream()
                .filter(member -> member.getRole() == GroupMember.Role.ADMIN)
                .count());
        stats.setMemberCount(allMembers.stream()
                .filter(member -> member.getRole() == GroupMember.Role.MEMBER)
                .count());
        
        // 최근 활동 시간 (최근 가입한 멤버 기준)
        stats.setLastActivityAt(allMembers.stream()
                .map(GroupMember::getJoinedAt)
                .max(LocalDateTime::compareTo)
                .orElse(group.getCreatedAt()));
        
        return stats;
    }
} 