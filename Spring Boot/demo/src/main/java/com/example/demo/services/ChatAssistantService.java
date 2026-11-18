package com.example.demo.services;

import com.example.demo.dto.chat.ChatResponse;
import com.example.demo.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

@Service
public class ChatAssistantService {
    private final ChatCancelIntentService cancellations;
    private final ChatTripSearchService tripSearch;

    public ChatAssistantService(ChatCancelIntentService cancellations, ChatTripSearchService tripSearch) {
        this.cancellations = cancellations;
        this.tripSearch = tripSearch;
    }

    public ChatResponse reply(String message, AuthenticatedUser user) {
        return cancellations.handle(message, user)
            .orElseGet(() -> new ChatResponse(tripSearch.reply(message), null));
    }
}
