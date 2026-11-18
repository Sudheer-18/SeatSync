package com.example.demo.model;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "chat_pending_actions", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class ChatPendingActionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected ChatPendingActionEntity() { }

    public ChatPendingActionEntity(UserEntity user, Long bookingId, Instant expiresAt) {
        this.user = user;
        this.bookingId = bookingId;
        this.expiresAt = expiresAt;
    }

    public Long getBookingId() { return bookingId; }
    public Instant getExpiresAt() { return expiresAt; }
}
