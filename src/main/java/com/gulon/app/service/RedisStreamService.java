package com.gulon.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStreamService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final String CHAT_STREAM = "chat:stream";
    public static final String NOTIFICATION_STREAM = "notification:stream";

    /**
     * 채팅 메시지를 Redis Stream에 발행
     */
    public void publishChatMessage(String groupId, String senderId, String message, String messageType) {
        try {
            Map<String, String> messageData = Map.of(
                "groupId", groupId,
                "senderId", senderId,
                "message", message,
                "messageType", messageType,
                "timestamp", String.valueOf(System.currentTimeMillis())
            );

            StringRecord record = StreamRecords.string(messageData)
                    .withStreamKey(CHAT_STREAM);

            RecordId messageId = redisTemplate.opsForStream().add(record);
            log.info("채팅 메시지 발행 완료 - Stream: {}, MessageId: {}", CHAT_STREAM, messageId.getValue());

        } catch (Exception e) {
            log.error("채팅 메시지 발행 실패: ", e);
            throw new RuntimeException("채팅 메시지 발행에 실패했습니다.", e);
        }
    }

    /**
     * 알림 메시지를 Redis Stream에 발행
     */
    public void publishNotification(String userId, String title, String content, String notificationType) {
        try {
            Map<String, String> notificationData = Map.of(
                "userId", userId,
                "title", title,
                "content", content,
                "notificationType", notificationType,
                "timestamp", String.valueOf(System.currentTimeMillis())
            );

            StringRecord record = StreamRecords.string(notificationData)
                    .withStreamKey(NOTIFICATION_STREAM);

            RecordId messageId = redisTemplate.opsForStream().add(record);
            log.info("알림 메시지 발행 완료 - Stream: {}, MessageId: {}", NOTIFICATION_STREAM, messageId.getValue());

        } catch (Exception e) {
            log.error("알림 메시지 발행 실패: ", e);
            throw new RuntimeException("알림 메시지 발행에 실패했습니다.", e);
        }
    }

    /**
     * 일반적인 메시지를 Redis Stream에 발행
     */
    public void publishMessage(String streamKey, Object messageData) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(messageData);
            
            Map<String, String> data = Map.of(
                "data", jsonMessage,
                "timestamp", String.valueOf(System.currentTimeMillis())
            );

            StringRecord record = StreamRecords.string(data)
                    .withStreamKey(streamKey);

            RecordId messageId = redisTemplate.opsForStream().add(record);
            log.info("메시지 발행 완료 - Stream: {}, MessageId: {}", streamKey, messageId.getValue());

        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: ", e);
            throw new RuntimeException("메시지 직렬화에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("메시지 발행 실패: ", e);
            throw new RuntimeException("메시지 발행에 실패했습니다.", e);
        }
    }
} 