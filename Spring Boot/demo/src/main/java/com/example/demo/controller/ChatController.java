package com.example.demo.controller;

import java.util.List;
import com.example.demo.dto.chat.ChatHistoryItem;
import com.example.demo.dto.chat.ChatRequest;
import com.example.demo.dto.chat.ChatResponse;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.services.ChatService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chat;

    public ChatController(ChatService chat) {
        this.chat = chat;
    }

    @PostMapping
    public ChatResponse send(@Valid @RequestBody ChatRequest request,
        @AuthenticationPrincipal AuthenticatedUser user) {
        return chat.send(request, user);
    }

    @GetMapping("/history")
    public List<ChatHistoryItem> history(@RequestParam(defaultValue = "50") int limit,
        @AuthenticationPrincipal AuthenticatedUser user) {
        return chat.history(limit, user);
    }
}
