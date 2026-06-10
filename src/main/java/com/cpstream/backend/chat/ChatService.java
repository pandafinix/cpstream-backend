package com.cpstream.backend.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${chat.rate-limit.window-seconds:10}")
    private long windowSeconds;

  @Value("${chat.rate-limit.max-messages:5}")
    private long maxMessages;

    public ChatMessageResponse saveMessage(ChatMessageRequest request) {

        if (request.getRoomName() == null || request.getRoomName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room name is required");
        }

        if (request.getSenderIdentity() == null || request.getSenderIdentity().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender identity is required");
        }

        if (request.getSenderName() == null || request.getSenderName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender name is required");
        }

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is required");
        }

        String cleanedMessage = request.getMessage().trim();

        if (cleanedMessage.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is too long");
        }

        checkRateLimit(request.getRoomName(), request.getSenderIdentity());

        ChatMessage chatMessage = ChatMessage.builder()
                .roomName(request.getRoomName())
                .senderIdentity(request.getSenderIdentity())
                .senderName(request.getSenderName())
                .message(cleanedMessage)
                .build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        return mapToResponse(saved);
    }

    public List<ChatMessageResponse> getMessages(String roomName) {
        List<ChatMessage> messages =
                chatMessageRepository.findTop50ByRoomNameOrderByCreatedAtDesc(roomName);

        Collections.reverse(messages);

        return messages.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void checkRateLimit(String roomName, String senderIdentity) {
        String key = "chat:rate:" + roomName + ":" + senderIdentity;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        if (count != null && count > maxMessages) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "You are sending messages too fast"
            );
        }
    }

    private ChatMessageResponse mapToResponse(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .roomName(chatMessage.getRoomName())
                .senderIdentity(chatMessage.getSenderIdentity())
                .senderName(chatMessage.getSenderName())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}