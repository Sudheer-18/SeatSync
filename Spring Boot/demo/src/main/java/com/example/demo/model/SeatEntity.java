package com.example.demo.model;

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
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(columnNames = {"bus_id", "seat_number"}))
public class SeatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private BusEntity bus;
    @Column(name = "seat_number", nullable = false, length = 8)
    private String seatNumber;
    @Column(name = "seat_type", nullable = false, length = 10)
    private String seatType;
    @Column(name = "ladies_only", nullable = false)
    private boolean ladiesOnly;

    protected SeatEntity() { }

    public SeatEntity(BusEntity bus, String seatNumber, String seatType, boolean ladiesOnly) {
        this.bus = bus;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.ladiesOnly = ladiesOnly;
    }

    public Long getId() { return id; }
    public BusEntity getBus() { return bus; }
    public String getSeatNumber() { return seatNumber; }
    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }
    public boolean isLadiesOnly() { return ladiesOnly; }
}
