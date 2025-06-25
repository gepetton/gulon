package com.gulon.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStreamListener implements StreamListener<String, MapRecord<String, String, String>>, InitializingBean, DisposableBean {

    private final SimpMessagingTemplate messagingTemplate;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamContainer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterPropertiesSet() throws Exception {
        // 채팅 스트림 리스너 등록
        streamContainer.receive(
                StreamOffset.latest(RedisStreamService.CHAT_STREAM),
                this
        );

        // 알림 스트림 리스너 등록  
        streamContainer.receive(
                StreamOffset.latest(RedisStreamService.NOTIFICATION_STREAM),
                this
        );

        streamContainer.start();
        log.info("Redis Stream 리스너가 시작되었습니다.");
    }

    @Override
    public void destroy() throws Exception {
        if (streamContainer != null) {
            streamContainer.stop();
            log.info("Redis Stream 리스너가 종료되었습니다.");
        }
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            String streamKey = message.getStream();
            Map<String, String> messageBody = message.getValue();

            log.info("Redis Stream 메시지 수신 - Stream: {}, MessageId: {}", streamKey, message.getId());

            if (RedisStreamService.CHAT_STREAM.equals(streamKey)) {
                handleChatMessage(messageBody);
            } else if (RedisStreamService.NOTIFICATION_STREAM.equals(streamKey)) {
                handleNotificationMessage(messageBody);
            } else {
                handleGenericMessage(streamKey, messageBody);
            }

        } catch (Exception e) {
            log.error("Redis Stream 메시지 처리 실패: ", e);
        }
    }

    /**
     * 채팅 메시지 처리 및 WebSocket 전송
     */
    private void handleChatMessage(Map<String, String> messageBody) {
        try {
            String groupId = messageBody.get("groupId");
            String senderId = messageBody.get("senderId");
            String message = messageBody.get("message");
            String messageType = messageBody.get("messageType");
            String timestamp = messageBody.get("timestamp");

            // 그룹 채팅방에 메시지 전송
            messagingTemplate.convertAndSend(
                    "/topic/chat/group/" + groupId,
                    Map.of(
                            "senderId", senderId,
                            "message", message,
                            "messageType", messageType,
                            "timestamp", timestamp
                    )
            );

            log.info("채팅 메시지 WebSocket 전송 완료 - GroupId: {}, SenderId: {}", groupId, senderId);

        } catch (Exception e) {
            log.error("채팅 메시지 WebSocket 전송 실패: ", e);
        }
    }

    /**
     * 알림 메시지 처리 및 WebSocket 전송
     */
    private void handleNotificationMessage(Map<String, String> messageBody) {
        try {
            String userId = messageBody.get("userId");
            String title = messageBody.get("title");
            String content = messageBody.get("content");
            String notificationType = messageBody.get("notificationType");
            String timestamp = messageBody.get("timestamp");

            // 특정 사용자에게 알림 전송
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/notifications",
                    Map.of(
                            "title", title,
                            "content", content,
                            "notificationType", notificationType,
                            "timestamp", timestamp
                    )
            );

            log.info("알림 메시지 WebSocket 전송 완료 - UserId: {}, Type: {}", userId, notificationType);

        } catch (Exception e) {
            log.error("알림 메시지 WebSocket 전송 실패: ", e);
        }
    }

    /**
     * 일반 메시지 처리 및 WebSocket 전송
     */
    private void handleGenericMessage(String streamKey, Map<String, String> messageBody) {
        try {
            String data = messageBody.get("data");
            String timestamp = messageBody.get("timestamp");

            // 스트림별 토픽으로 메시지 전송
            messagingTemplate.convertAndSend(
                    "/topic/stream/" + streamKey.replace(":", "/"),
                    Map.of(
                            "data", data,
                            "timestamp", timestamp
                    )
            );

            log.info("일반 메시지 WebSocket 전송 완료 - Stream: {}", streamKey);

        } catch (Exception e) {
            log.error("일반 메시지 WebSocket 전송 실패: ", e);
        }
    }
} 