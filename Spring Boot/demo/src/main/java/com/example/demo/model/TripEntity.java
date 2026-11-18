package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "trips")
public class TripEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private BusEntity bus;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteEntity route;
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected TripEntity() { }

    public TripEntity(BusEntity bus, RouteEntity route, LocalDateTime departureTime, LocalDateTime arrivalTime, BigDecimal price) {
        this.bus = bus;
        this.route = route;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
    }

    public Long getId() { return id; }
    public BusEntity getBus() { return bus; }
    public RouteEntity getRoute() { return route; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public BigDecimal getPrice() { return price; }
    public void setDepartureTime(LocalDateTime value) { departureTime = value; }
    public void setArrivalTime(LocalDateTime value) { arrivalTime = value; }
    public void setPrice(BigDecimal value) { price = value; }
    public void setBus(BusEntity bus) { this.bus = bus; }
    public void setRoute(RouteEntity route) { this.route = route; }
}
