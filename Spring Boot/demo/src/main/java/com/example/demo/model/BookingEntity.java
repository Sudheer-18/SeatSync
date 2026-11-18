package com.example.demo.model;

import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bookings")
public class BookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 36)
    private String idempotencyKey;
    @Column(name = "contact_phone", length = 16)
    private String contactPhone;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected BookingEntity() { }

    public BookingEntity(UserEntity user, TripEntity trip, BigDecimal totalAmount, String idempotencyKey, String contactPhone) {
        this.user = user;
        this.trip = trip;
        this.totalAmount = totalAmount;
        this.idempotencyKey = idempotencyKey;
        this.contactPhone = contactPhone;
        this.status = BookingStatus.PENDING_PAYMENT;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public TripEntity getTrip() { return trip; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getContactPhone() { return contactPhone; }
    public Instant getCreatedAt() { return createdAt; }
}
