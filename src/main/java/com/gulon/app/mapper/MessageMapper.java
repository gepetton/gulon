package com.gulon.app.mapper;

import com.gulon.app.dto.MessageDto;
import com.gulon.app.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    
    // Response DTO 매핑
    @Mapping(source = "group.publicId", target = "groupPublicId")
    @Mapping(source = "group.name", target = "groupName")
    @Mapping(source = "user.publicId", target = "senderPublicId")
    @Mapping(source = "user.name", target = "senderName")
    @Mapping(source = "chatMessage", target = "edited", qualifiedByName = "mapIsEdited")
    @Mapping(target = "canEdit", ignore = true)
    @Mapping(target = "canDelete", ignore = true)
    MessageDto.Response toResponse(ChatMessage chatMessage);
    
    List<MessageDto.Response> toResponseList(List<ChatMessage> chatMessages);
    
    // Summary DTO 매핑
    @Mapping(source = "user.publicId", target = "senderPublicId")
    @Mapping(source = "user.name", target = "senderName")
    @Mapping(source = "chatMessage", target = "edited", qualifiedByName = "mapIsEdited")
    MessageDto.Summary toSummary(ChatMessage chatMessage);
    
    List<MessageDto.Summary> toSummaryList(List<ChatMessage> chatMessages);
    
    // RealtimeMessage DTO 매핑
    @Mapping(source = "group.publicId", target = "groupPublicId")
    @Mapping(source = "user.publicId", target = "senderPublicId")
    @Mapping(source = "user.name", target = "senderName")
    @Mapping(target = "action", ignore = true)
    MessageDto.RealtimeMessage toRealtimeMessage(ChatMessage chatMessage);
    
    List<MessageDto.RealtimeMessage> toRealtimeMessageList(List<ChatMessage> chatMessages);
    
    // SendResponse DTO 매핑
    @Mapping(source = "group.publicId", target = "groupPublicId")
    @Mapping(source = "user.publicId", target = "senderPublicId")
    @Mapping(source = "user.name", target = "senderName")
    @Mapping(target = "message", ignore = true)
    MessageDto.SendResponse toSendResponse(ChatMessage chatMessage);
    
    // ActiveUser DTO 매핑 (별도 처리 필요)
    @Mapping(source = "publicId", target = "userPublicId")
    @Mapping(source = "name", target = "userName")
    @Mapping(target = "lastSeen", ignore = true)
    @Mapping(target = "online", ignore = true)
    MessageDto.ActiveUser toActiveUser(com.gulon.app.entity.User user);
    
    List<MessageDto.ActiveUser> toActiveUserList(List<com.gulon.app.entity.User> users);
    
    // UserMessageCount DTO 매핑
    @Mapping(source = "publicId", target = "userPublicId")
    @Mapping(source = "name", target = "userName")
    @Mapping(target = "messageCount", ignore = true)
    @Mapping(target = "lastMessageDaysAgo", ignore = true)
    MessageDto.UserMessageCount toUserMessageCount(com.gulon.app.entity.User user);
    
    // ChatMessage 엔티티 생성을 위한 매핑 (SendRequest -> ChatMessage)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(source = "content", target = "content")
    @Mapping(source = "type", target = "type")
    ChatMessage toEntity(MessageDto.SendRequest sendRequest);
    
    // Named 매핑 메소드들
    @Named("mapIsEdited")
    default boolean mapIsEdited(ChatMessage chatMessage) {
        return chatMessage.getEditedAt() != null;
    }
    
    // 권한 체크를 위한 헬퍼 메소드들 (서비스에서 사용)
    default MessageDto.Response toResponseWithPermissions(ChatMessage chatMessage, UUID requestUserPublicId) {
        MessageDto.Response response = toResponse(chatMessage);
        if (requestUserPublicId != null) {
            response.setCanEdit(chatMessage.canEdit(requestUserPublicId));
            response.setCanDelete(chatMessage.canDelete(requestUserPublicId));
        } else {
            response.setCanEdit(false);
            response.setCanDelete(false);
        }
        return response;
    }
    
    default List<MessageDto.Response> toResponseListWithPermissions(List<ChatMessage> chatMessages, UUID requestUserPublicId) {
        return chatMessages.stream()
                .map(msg -> toResponseWithPermissions(msg, requestUserPublicId))
                .toList();
    }
    
    // RealtimeMessage에 action 설정을 위한 헬퍼
    default MessageDto.RealtimeMessage toRealtimeMessageWithAction(ChatMessage chatMessage, String action) {
        MessageDto.RealtimeMessage realtimeMessage = toRealtimeMessage(chatMessage);
        realtimeMessage.setAction(action);
        return realtimeMessage;
    }
} 