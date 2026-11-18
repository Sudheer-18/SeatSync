package com.example.demo.model;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "booking_seats", uniqueConstraints = @UniqueConstraint(columnNames = {"trip_id", "seat_id"}))
public class BookingSeatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingEntity booking;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private SeatEntity seat;
    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;
    @Column(nullable = false)
    private int age;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    protected BookingSeatEntity() { }

    public BookingSeatEntity(BookingEntity booking, TripEntity trip, SeatEntity seat, String name, int age, Gender gender) {
        this.booking = booking;
        this.trip = trip;
        this.seat = seat;
        this.passengerName = name;
        this.age = age;
        this.gender = gender;
    }

    public Long getId() { return id; }
    public BookingEntity getBooking() { return booking; }
    public TripEntity getTrip() { return trip; }
    public SeatEntity getSeat() { return seat; }
    public String getPassengerName() { return passengerName; }
    public int getAge() { return age; }
    public Gender getGender() { return gender; }
}
