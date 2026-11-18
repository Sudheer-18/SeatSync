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
@Table(name = "seat_holds", uniqueConstraints = @UniqueConstraint(columnNames = {"trip_id", "seat_id"}))
public class SeatHoldEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private SeatEntity seat;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected SeatHoldEntity() { }

    public SeatHoldEntity(TripEntity trip, SeatEntity seat, UserEntity user, Instant expiresAt) {
        this.trip = trip;
        this.seat = seat;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public TripEntity getTrip() { return trip; }
    public SeatEntity getSeat() { return seat; }
    public UserEntity getUser() { return user; }
    public Instant getExpiresAt() { return expiresAt; }
    public void renew(UserEntity owner, Instant expiry) {
        user = owner;
        expiresAt = expiry;
    }
}
