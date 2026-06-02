package com.cpstream.backend.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {

    private String roomName;

    private String senderIdentity;

    private String senderName;

    private String message;
}