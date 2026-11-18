package com.example.demo.dto.chat;

import java.time.Instant;
import com.example.demo.model.ChatMessageEntity;

public record ChatHistoryItem(String role, String content, Instant createdAt) {
    public static ChatHistoryItem from(ChatMessageEntity message) {
        return new ChatHistoryItem(message.getRole().name(), message.getContent(), message.getCreatedAt());
    }
}
