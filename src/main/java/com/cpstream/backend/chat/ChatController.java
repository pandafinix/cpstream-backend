package com.cpstream.backend.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/messages")
    public ChatMessageResponse saveMessage(@RequestBody ChatMessageRequest request) {
        return chatService.saveMessage(request);
    }

    @GetMapping("/messages/{roomName}")
    public List<ChatMessageResponse> getMessages(@PathVariable String roomName) {
        return chatService.getMessages(roomName);
    }
}