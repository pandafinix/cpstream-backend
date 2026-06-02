package com.cpstream.backend.chat;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {

    private String id;

    private String roomName;

    private String senderIdentity;

    private String senderName;

    private String message;

    private LocalDateTime createdAt;
}