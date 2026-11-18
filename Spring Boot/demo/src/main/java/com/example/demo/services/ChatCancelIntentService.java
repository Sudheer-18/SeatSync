package com.example.demo.services;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.demo.dto.chat.ChatResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.ChatPendingActionEntity;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.ChatPendingActionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatCancelIntentService {
    private static final Pattern BOOKING_ID = Pattern.compile("(?i)booking\\s*#?\\s*(\\d+)");
    private final ChatPendingActionRepository pendingActions;
    private final BookingService bookings;
    private final UserRepository users;

    public ChatCancelIntentService(ChatPendingActionRepository pendingActions, BookingService bookings, UserRepository users) {
        this.pendingActions = pendingActions;
        this.bookings = bookings;
        this.users = users;
    }

    @Transactional
    public Optional<ChatResponse> handle(String message, AuthenticatedUser user) {
        String normalized = message.toLowerCase().trim();
        ChatPendingActionEntity pending = pendingActions.findByUser_Id(user.id()).orElse(null);
        if (pending != null) {
            if (pending.getExpiresAt().isBefore(Instant.now())) {
                pendingActions.delete(pending);
                return Optional.of(new ChatResponse("That confirmation expired. Please start the cancellation again.", null));
            }
            if (normalized.matches(".*\\b(yes|confirm|confirm it)\\b.*")) {
                pendingActions.delete(pending);
                return Optional.of(new ChatResponse(bookings.cancel(pending.getBookingId(), user).message(), null));
            }
            if (normalized.matches(".*\\b(no|dismiss|cancel it)\\b.*")) {
                pendingActions.delete(pending);
                return Optional.of(new ChatResponse("Okay, I won't cancel that booking.", null));
            }
        }
        if (!normalized.contains("cancel")) return Optional.empty();
        Matcher matcher = BOOKING_ID.matcher(message);
        if (!matcher.find()) return Optional.of(new ChatResponse("Please include the booking ID you want to cancel.", null));
        long id;
        try {
            id = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException exception) {
            throw ApiException.badRequest("Booking ID is invalid.");
        }
        var booking = bookings.get(id, user);
        if (!BookingStatus.CONFIRMED.name().equals(booking.status())) {
            return Optional.of(new ChatResponse("That booking is not confirmed and cannot be cancelled.", null));
        }
        if (booking.departureTime().isBefore(java.time.LocalDateTime.now().plusHours(2))) {
            return Optional.of(new ChatResponse("Cancellation is closed less than two hours before departure.", null));
        }
        pendingActions.deleteByUser_Id(user.id());
        UserEntity account = users.findById(user.id()).orElseThrow(() -> ApiException.unauthorized("Invalid or expired token."));
        pendingActions.save(new ChatPendingActionEntity(account, id, Instant.now().plusSeconds(300)));
        return Optional.of(new ChatResponse("Booking " + id + " cancel cheyala? Confirm cheyandi.",
            new ChatResponse.PendingAction("CANCEL_BOOKING", id, true)));
    }
}
