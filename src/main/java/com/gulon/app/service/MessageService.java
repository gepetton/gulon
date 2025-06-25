package com.gulon.app.service;

import com.gulon.app.dto.MessageDto;
import com.gulon.app.entity.ChatMessage;
import com.gulon.app.entity.GroupMember;
import com.gulon.app.entity.GroupTable;
import com.gulon.app.entity.User;
import com.gulon.app.mapper.MessageMapper;
import com.gulon.app.repository.ChatMessageRepository;
import com.gulon.app.repository.GroupMemberRepository;
import com.gulon.app.repository.GroupTableRepository;
import com.gulon.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MessageService {
    
    private final ChatMessageRepository messageRepository;
    private final GroupTableRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MessageMapper messageMapper;
    
    /**
     * 메시지 전송
     */
    @Transactional
    public MessageDto.SendResponse sendMessage(MessageDto.SendRequest request) {
        log.info("Sending message to group: {}", request.getGroupPublicId());
        
        // 그룹 조회
        GroupTable group = groupRepository.findByPublicId(request.getGroupPublicId())
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다: " + request.getGroupPublicId()));
        
        // 사용자 조회
        User sender = userRepository.findByPublicId(request.getSenderPublicId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getSenderPublicId()));
        
        // 그룹 멤버십 확인
        boolean isMember = groupMemberRepository.existsActiveMembership(
                request.getGroupPublicId(), request.getSenderPublicId());
        if (!isMember) {
            throw new IllegalArgumentException("그룹 멤버만 메시지를 전송할 수 있습니다.");
        }
        
        // 메시지 생성
        ChatMessage message = new ChatMessage(
                group, 
                sender, 
                request.getContent(),
                request.getType() != null ? request.getType() : ChatMessage.MessageType.TEXT
        );
        
        ChatMessage savedMessage = messageRepository.save(message);
        
        // 응답 생성
        MessageDto.SendResponse response = messageMapper.toSendResponse(savedMessage);
        response.setMessage("메시지가 전송되었습니다.");
        
        log.info("Message sent successfully with publicId: {}", savedMessage.getPublicId());
        return response;
    }
    
    /**
     * 메시지 조회
     */
    public Optional<MessageDto.Response> getMessage(UUID messagePublicId, UUID requestUserPublicId) {
        log.info("Getting message: {}", messagePublicId);
        
        Optional<ChatMessage> messageOpt = messageRepository.findByPublicId(messagePublicId);
        if (messageOpt.isEmpty()) {
            return Optional.empty();
        }
        
        ChatMessage message = messageOpt.get();
        
        // 그룹 멤버십 확인
        if (requestUserPublicId != null) {
            boolean isMember = groupMemberRepository.existsActiveMembership(
                    message.getGroup().getPublicId(), requestUserPublicId);
            if (!isMember) {
                return Optional.empty(); // 권한 없음을 나타내기 위해 empty 반환
            }
        }
        
        return Optional.of(messageMapper.toResponseWithPermissions(message, requestUserPublicId));
    }
    
    /**
     * 메시지 수정
     */
    @Transactional
    public MessageDto.Response editMessage(UUID messagePublicId, MessageDto.EditRequest request, UUID requestUserPublicId) {
        log.info("Editing message: {}", messagePublicId);
        
        ChatMessage message = messageRepository.findByPublicId(messagePublicId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다: " + messagePublicId));
        
        // 수정 권한 확인
        if (!message.canEdit(requestUserPublicId)) {
            throw new IllegalArgumentException("메시지를 수정할 권한이 없습니다.");
        }
        
        // 메시지 수정
        message.editMessage(request.getContent());
        ChatMessage updatedMessage = messageRepository.save(message);
        
        log.info("Message edited successfully: {}", messagePublicId);
        return messageMapper.toResponseWithPermissions(updatedMessage, requestUserPublicId);
    }
    
    /**
     * 메시지 삭제
     */
    @Transactional
    public void deleteMessage(UUID messagePublicId, UUID requestUserPublicId) {
        log.info("Deleting message: {}", messagePublicId);
        
        ChatMessage message = messageRepository.findByPublicId(messagePublicId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다: " + messagePublicId));
        
        // 삭제 권한 확인 (작성자 또는 그룹 관리자)
        boolean canDelete = message.canDelete(requestUserPublicId);
        
        // 그룹 관리자 권한도 확인
        if (!canDelete) {
            GroupMember membership = groupMemberRepository.findByGroupPublicIdAndUserPublicId(
                    message.getGroup().getPublicId(), requestUserPublicId)
                    .orElse(null);
            
            canDelete = membership != null && 
                       (membership.getRole() == GroupMember.Role.OWNER || 
                        membership.getRole() == GroupMember.Role.ADMIN);
        }
        
        if (!canDelete) {
            throw new IllegalArgumentException("메시지를 삭제할 권한이 없습니다.");
        }
        
        // 메시지 삭제 (소프트 삭제)
        message.deleteMessage();
        messageRepository.save(message);
        
        log.info("Message deleted successfully: {}", messagePublicId);
    }
    
    /**
     * 그룹 채팅 히스토리 조회
     */
    public MessageDto.HistoryResponse getGroupChatHistory(UUID groupPublicId, int page, int size, UUID requestUserPublicId) {
        log.info("Getting chat history for group: {}, page: {}, size: {}", groupPublicId, page, size);
        
        // 그룹 존재 확인
        if (!groupRepository.existsByPublicId(groupPublicId)) {
            throw new IllegalArgumentException("그룹을 찾을 수 없습니다: " + groupPublicId);
        }
        
        // 그룹 멤버십 확인
        if (requestUserPublicId != null) {
            boolean isMember = groupMemberRepository.existsActiveMembership(groupPublicId, requestUserPublicId);
            if (!isMember) {
                throw new IllegalArgumentException("그룹 멤버만 채팅 히스토리를 조회할 수 있습니다.");
            }
        }
        
        // 페이징 조회
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<ChatMessage> messagesPage = messageRepository.findByGroupPublicIdAndNotDeleted(groupPublicId, pageable);
        
        List<MessageDto.Summary> summaries = messageMapper.toSummaryList(messagesPage.getContent());
        
        MessageDto.HistoryResponse response = new MessageDto.HistoryResponse();
        response.setMessages(summaries);
        response.setTotalCount(messagesPage.getTotalElements());
        response.setCurrentPage(page);
        response.setPageSize(size);
        response.setHasNext(messagesPage.hasNext());
        response.setHasPrevious(messagesPage.hasPrevious());
        
        // 마지막 메시지 정보 설정
        if (!summaries.isEmpty()) {
            MessageDto.Summary lastMessage = summaries.get(summaries.size() - 1);
            response.setLastMessageId(lastMessage.getPublicId());
            response.setLastMessageTime(lastMessage.getSentAt());
        }
        
        return response;
    }
    
    /**
     * 메시지 검색
     */
    public MessageDto.SearchResult searchMessages(MessageDto.SearchFilter filter, int page, int size) {
        log.info("Searching messages with keyword: {}", filter.getKeyword());
        
        List<ChatMessage> messages;
        
        if (filter.getGroupPublicId() != null) {
            // 특정 그룹 내 검색
            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                messages = messageRepository.searchByKeywordInGroupPublicId(
                        filter.getGroupPublicId(), filter.getKeyword().trim());
            } else {
                messages = messageRepository.findByGroupPublicId(filter.getGroupPublicId());
            }
        } else {
            // 전체 검색
            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                messages = messageRepository.searchByKeyword(filter.getKeyword().trim());
            } else {
                messages = messageRepository.findAll(Sort.by(Sort.Direction.DESC, "sentAt"));
            }
        }
        
        // 필터 적용
        if (filter.getSenderPublicId() != null) {
            messages = messages.stream()
                    .filter(msg -> msg.getUser().getPublicId().equals(filter.getSenderPublicId()))
                    .collect(Collectors.toList());
        }
        
        if (filter.getType() != null) {
            messages = messages.stream()
                    .filter(msg -> msg.getType().equals(filter.getType()))
                    .collect(Collectors.toList());
        }
        
        if (filter.getSentAfter() != null) {
            messages = messages.stream()
                    .filter(msg -> msg.getSentAt().isAfter(filter.getSentAfter()))
                    .collect(Collectors.toList());
        }
        
        if (filter.getSentBefore() != null) {
            messages = messages.stream()
                    .filter(msg -> msg.getSentAt().isBefore(filter.getSentBefore()))
                    .collect(Collectors.toList());
        }
        
        if (!filter.isIncludeDeleted()) {
            messages = messages.stream()
                    .filter(msg -> !msg.isDeleted())
                    .collect(Collectors.toList());
        }
        
        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, messages.size());
        List<ChatMessage> pageMessages = messages.subList(start, end);
        
        List<MessageDto.Summary> summaries = messageMapper.toSummaryList(pageMessages);
        
        MessageDto.SearchResult result = new MessageDto.SearchResult();
        result.setMessages(summaries);
        result.setTotalCount(messages.size());
        result.setCurrentPage(page);
        result.setPageSize(size);
        result.setTotalPages((int) Math.ceil((double) messages.size() / size));
        result.setHasNext(end < messages.size());
        result.setHasPrevious(page > 0);
        result.setSearchKeyword(filter.getKeyword());
        
        return result;
    }
    
    /**
     * 그룹 채팅 현황 조회
     */
    public MessageDto.GroupChatStatus getGroupChatStatus(UUID groupPublicId, UUID requestUserPublicId) {
        log.info("Getting chat status for group: {}", groupPublicId);
        
        GroupTable group = groupRepository.findByPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다: " + groupPublicId));
        
        // 그룹 멤버십 확인
        if (requestUserPublicId != null) {
            boolean isMember = groupMemberRepository.existsActiveMembership(groupPublicId, requestUserPublicId);
            if (!isMember) {
                throw new IllegalArgumentException("그룹 멤버만 채팅 현황을 조회할 수 있습니다.");
            }
        }
        
        MessageDto.GroupChatStatus status = new MessageDto.GroupChatStatus();
        status.setGroupPublicId(groupPublicId);
        status.setGroupName(group.getName());
        
        // 전체 메시지 수
        status.setTotalMessages(messageRepository.countActiveMessagesByGroupPublicId(groupPublicId));
        
        // 오늘 메시지 수
        List<ChatMessage> todayMessages = messageRepository.findTodayMessagesByGroupPublicId(groupPublicId);
        status.setTodayMessages(todayMessages.size());
        
        // 마지막 메시지
        messageRepository.findLastMessageByGroupPublicId(groupPublicId)
                .ifPresent(lastMessage -> {
                    status.setLastMessage(messageMapper.toSummary(lastMessage));
                    status.setLastActivity(lastMessage.getSentAt());
                });
        
        // 활성 사용자 목록 (그룹 멤버들)
        List<GroupMember> activeMembers = groupMemberRepository.findByGroupPublicIdAndStatus(
                groupPublicId, GroupMember.MemberStatus.ACTIVE);
        
        List<MessageDto.ActiveUser> activeUsers = activeMembers.stream()
                .map(member -> {
                    MessageDto.ActiveUser activeUser = messageMapper.toActiveUser(member.getUser());
                    // 마지막 메시지 시간을 lastSeen으로 설정 (간단한 구현)
                    List<ChatMessage> userMessages = messageRepository.findByGroupPublicIdAndUserPublicId(
                            groupPublicId, member.getUser().getPublicId());
                    if (!userMessages.isEmpty()) {
                        activeUser.setLastSeen(userMessages.get(0).getSentAt());
                    }
                    activeUser.setOnline(false); // 실시간 온라인 상태는 별도 구현 필요
                    return activeUser;
                })
                .collect(Collectors.toList());
        
        status.setActiveUsers(activeUsers);
        
        // 읽지 않은 메시지 수 (requestUserPublicId 기준, 간단한 구현)
        if (requestUserPublicId != null) {
            // 실제로는 별도의 read_receipt 테이블이 필요하지만, 여기서는 0으로 설정
            status.setUnreadCount(0);
        }
        
        return status;
    }
    
    /**
     * 메시지 통계 조회
     */
    public MessageDto.Statistics getMessageStatistics(UUID groupPublicId, UUID requestUserPublicId) {
        log.info("Getting message statistics for group: {}", groupPublicId);
        
        GroupTable group = groupRepository.findByPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다: " + groupPublicId));
        
        // 그룹 관리자 권한 확인
        GroupMember membership = groupMemberRepository.findByGroupPublicIdAndUserPublicId(groupPublicId, requestUserPublicId)
                .orElseThrow(() -> new IllegalArgumentException("그룹에 접근할 권한이 없습니다."));
        
        if (membership.getRole() != GroupMember.Role.OWNER && membership.getRole() != GroupMember.Role.ADMIN) {
            throw new IllegalArgumentException("통계를 조회할 권한이 없습니다.");
        }
        
        MessageDto.Statistics stats = new MessageDto.Statistics();
        stats.setGroupPublicId(groupPublicId);
        stats.setGroupName(group.getName());
        
        // 전체 메시지 조회
        List<ChatMessage> allMessages = messageRepository.findByGroupPublicId(groupPublicId);
        
        stats.setTotalMessages(allMessages.size());
        stats.setTextMessages(allMessages.stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.TEXT)
                .count());
        stats.setImageMessages(allMessages.stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.IMAGE)
                .count());
        stats.setFileMessages(allMessages.stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.FILE)
                .count());
        stats.setSystemMessages(allMessages.stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.SYSTEM)
                .count());
        stats.setDeletedMessages(allMessages.stream()
                .filter(ChatMessage::isDeleted)
                .count());
        stats.setEditedMessages(allMessages.stream()
                .filter(msg -> msg.getEditedAt() != null)
                .count());
        
        // 첫 번째와 마지막 메시지 시간
        allMessages.stream()
                .map(ChatMessage::getSentAt)
                .min(LocalDateTime::compareTo)
                .ifPresent(stats::setFirstMessageAt);
        
        allMessages.stream()
                .map(ChatMessage::getSentAt)
                .max(LocalDateTime::compareTo)
                .ifPresent(stats::setLastMessageAt);
        
        // 일별 메시지 수 (최근 7일)
        List<MessageDto.DailyMessageCount> dailyCounts = allMessages.stream()
                .filter(msg -> msg.getSentAt().isAfter(LocalDateTime.now().minusDays(7)))
                .collect(Collectors.groupingBy(
                        msg -> msg.getSentAt().toLocalDate().toString(),
                        Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    MessageDto.DailyMessageCount count = new MessageDto.DailyMessageCount();
                    count.setDate(entry.getKey());
                    count.setCount(entry.getValue());
                    return count;
                })
                .collect(Collectors.toList());
        
        stats.setDailyCounts(dailyCounts);
        
        // 사용자별 메시지 수
        List<MessageDto.UserMessageCount> userCounts = allMessages.stream()
                .collect(Collectors.groupingBy(
                        msg -> msg.getUser(),
                        Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    MessageDto.UserMessageCount count = messageMapper.toUserMessageCount(entry.getKey());
                    count.setMessageCount(entry.getValue());
                    
                    // 마지막 메시지 이후 일수 계산
                    allMessages.stream()
                            .filter(msg -> msg.getUser().equals(entry.getKey()))
                            .map(ChatMessage::getSentAt)
                            .max(LocalDateTime::compareTo)
                            .ifPresent(lastMessageTime -> {
                                long daysAgo = ChronoUnit.DAYS.between(lastMessageTime, LocalDateTime.now());
                                count.setLastMessageDaysAgo(daysAgo);
                            });
                    
                    return count;
                })
                .collect(Collectors.toList());
        
        stats.setUserCounts(userCounts);
        
        return stats;
    }
    
    /**
     * 실시간 메시지 생성 (WebSocket용)
     */
    public MessageDto.RealtimeMessage createRealtimeMessage(ChatMessage message, String action) {
        return messageMapper.toRealtimeMessageWithAction(message, action);
    }
} 