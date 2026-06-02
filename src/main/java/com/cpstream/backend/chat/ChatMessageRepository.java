package com.cpstream.backend.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findTop50ByRoomNameOrderByCreatedAtDesc(String roomName);
}