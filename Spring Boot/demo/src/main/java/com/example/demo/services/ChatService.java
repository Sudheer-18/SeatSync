package com.example.demo.services;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import com.example.demo.dto.chat.ChatHistoryItem;
import com.example.demo.dto.chat.ChatRequest;
import com.example.demo.dto.chat.ChatResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.model.ChatMessageEntity;
import com.example.demo.model.ChatRole;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthenticatedUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {
    private final ChatMessageRepository messages;
    private final ChatAssistantService assistant;
    private final InMemoryRateLimiter limiter;
    private final UserRepository users;

    public ChatService(ChatMessageRepository messages, ChatAssistantService assistant,
        InMemoryRateLimiter limiter, UserRepository users) {
        this.messages = messages;
        this.assistant = assistant;
        this.limiter = limiter;
        this.users = users;
    }

    @Transactional
    public ChatResponse send(ChatRequest request, AuthenticatedUser user) {
        limiter.enforce("chat", user.id(), 30, Duration.ofMinutes(1));
        String text = request.message().trim();
        var account = users.findById(user.id())
            .orElseThrow(() -> ApiException.unauthorized("Invalid or expired token."));
        messages.save(new ChatMessageEntity(account, ChatRole.USER, text));
        ChatResponse response = assistant.reply(text, user);
        messages.save(new ChatMessageEntity(account, ChatRole.ASSISTANT, response.reply()));
        return response;
    }

    @Transactional(readOnly = true)
    public List<ChatHistoryItem> history(int limit, AuthenticatedUser user) {
        if (limit < 1 || limit > 100) throw ApiException.badRequest("limit must be between 1 and 100.");
        List<ChatMessageEntity> result = messages.findByUser_IdOrderByCreatedAtDescIdDesc(user.id(), PageRequest.of(0, limit));
        Collections.reverse(result);
        return result.stream().map(ChatHistoryItem::from).toList();
    }
}
