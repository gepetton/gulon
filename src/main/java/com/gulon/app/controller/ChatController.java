package com.gulon.app.controller;

import com.gulon.app.service.RedisStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Tag(name = "실시간 채팅", description = "WebSocket을 통한 실시간 그룹 채팅 및 알림 처리 API")
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final RedisStreamService redisStreamService;

    @Operation(
        summary = "그룹 채팅 메시지 처리",
        description = "클라이언트가 /app/chat/group/{groupId}로 메시지를 보내면 처리합니다. " +
                     "메시지는 Redis Stream을 통해 다른 사용자들에게 실시간으로 브로드캐스트됩니다."
    )
    @MessageMapping("/chat/group/{groupId}")
    public void sendGroupMessage(
            @Parameter(description = "그룹 ID", required = true)
            @DestinationVariable String groupId,
            @Parameter(description = "메시지 데이터 (senderId, message, messageType 포함)", required = true)
            @Payload Map<String, Object> message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            // 세션에서 사용자 정보 추출 (실제로는 JWT 토큰 등으로 인증된 사용자 정보를 사용)
            String senderId = (String) message.get("senderId");
            String messageContent = (String) message.get("message");
            String messageType = (String) message.getOrDefault("messageType", "TEXT");

            // 입력 검증
            if (senderId == null || messageContent == null || messageContent.trim().isEmpty()) {
                log.warn("잘못된 메시지 데이터 - GroupId: {}, SenderId: {}", groupId, senderId);
                return;
            }

            log.info("그룹 채팅 메시지 수신 - GroupId: {}, SenderId: {}, Type: {}", groupId, senderId, messageType);

            // Redis Stream에 메시지 발행
            redisStreamService.publishChatMessage(groupId, senderId, messageContent, messageType);

        } catch (Exception e) {
            log.error("그룹 채팅 메시지 처리 실패 - GroupId: {}", groupId, e);
        }
    }

    @Operation(
        summary = "사용자 채팅 참여 처리",
        description = "사용자가 그룹 채팅방에 참여할 때 처리합니다. " +
                     "다른 참여자들에게 입장 알림을 전송합니다."
    )
    @MessageMapping("/chat/join/{groupId}")
    public void joinChat(
            @Parameter(description = "그룹 ID", required = true)
            @DestinationVariable String groupId,
            @Parameter(description = "사용자 정보 (userId, username 포함)", required = true)
            @Payload Map<String, Object> message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            String userId = (String) message.get("userId");
            String username = (String) message.get("username");

            log.info("사용자 채팅 참여 - GroupId: {}, UserId: {}, Username: {}", groupId, userId, username);

            // 입장 메시지를 Redis Stream에 발행
            redisStreamService.publishChatMessage(
                    groupId, 
                    "SYSTEM", 
                    username + "님이 채팅에 참여했습니다.", 
                    "JOIN"
            );

        } catch (Exception e) {
            log.error("채팅 참여 처리 실패 - GroupId: {}", groupId, e);
        }
    }

    @Operation(
        summary = "사용자 채팅 퇴장 처리",
        description = "사용자가 그룹 채팅방을 떠날 때 처리합니다. " +
                     "다른 참여자들에게 퇴장 알림을 전송합니다."
    )
    @MessageMapping("/chat/leave/{groupId}")
    public void leaveChat(
            @Parameter(description = "그룹 ID", required = true)
            @DestinationVariable String groupId,
            @Parameter(description = "사용자 정보 (userId, username 포함)", required = true)
            @Payload Map<String, Object> message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            String userId = (String) message.get("userId");
            String username = (String) message.get("username");

            log.info("사용자 채팅 퇴장 - GroupId: {}, UserId: {}, Username: {}", groupId, userId, username);

            // 퇴장 메시지를 Redis Stream에 발행
            redisStreamService.publishChatMessage(
                    groupId, 
                    "SYSTEM", 
                    username + "님이 채팅을 떠났습니다.", 
                    "LEAVE"
            );

        } catch (Exception e) {
            log.error("채팅 퇴장 처리 실패 - GroupId: {}", groupId, e);
        }
    }

    @Operation(
        summary = "개인 알림 전송",
        description = "특정 사용자에게 개인 알림을 전송합니다. " +
                     "시스템 알림, 멘션, 개인 메시지 등에 사용됩니다."
    )
    @MessageMapping("/notification/send")
    public void sendNotification(
            @Parameter(description = "알림 데이터 (userId, title, content, notificationType 포함)", required = true)
            @Payload Map<String, Object> notification,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            String userId = (String) notification.get("userId");
            String title = (String) notification.get("title");
            String content = (String) notification.get("content");
            String notificationType = (String) notification.getOrDefault("notificationType", "INFO");

            // 입력 검증
            if (userId == null || title == null || content == null) {
                log.warn("잘못된 알림 데이터 - UserId: {}", userId);
                return;
            }

            log.info("알림 전송 요청 - UserId: {}, Type: {}, Title: {}", userId, notificationType, title);

            // Redis Stream에 알림 발행
            redisStreamService.publishNotification(userId, title, content, notificationType);

        } catch (Exception e) {
            log.error("알림 전송 실패", e);
        }
    }
} 