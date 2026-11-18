package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "buses", uniqueConstraints = @UniqueConstraint(columnNames = "bus_number"))
public class BusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(name = "bus_number", nullable = false, length = 30)
    private String busNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BusType type;
    @Column(nullable = false)
    private int totalSeats;

    protected BusEntity() { }

    public BusEntity(String name, String busNumber, BusType type, int totalSeats) {
        this.name = name;
        this.busNumber = busNumber;
        this.type = type;
        this.totalSeats = totalSeats;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBusNumber() { return busNumber; }
    public void setName(String name) { this.name = name; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }
    public BusType getType() { return type; }
    public void setType(BusType type) { this.type = type; }
    public int getTotalSeats() { return totalSeats; }
}
