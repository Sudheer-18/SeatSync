package com.example.demo.dto.chat;

public record ChatResponse(String reply, PendingAction pendingAction) {
    public record PendingAction(String type, Long bookingId, boolean requiresConfirmation) { }
}
