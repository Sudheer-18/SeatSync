package com.example.demo.dto.trip;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.model.TripEntity;

public record TripDetailsResponse(
    Long tripId,
    BusInfo bus,
    RouteInfo route,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    BigDecimal price
) {
    public static TripDetailsResponse from(TripEntity trip) {
        return new TripDetailsResponse(
            trip.getId(),
            new BusInfo(trip.getBus().getId(), trip.getBus().getName(), trip.getBus().getBusNumber(),
                trip.getBus().getType().name(), trip.getBus().getTotalSeats()),
            new RouteInfo(trip.getRoute().getId(), trip.getRoute().getSource(), trip.getRoute().getDestination()),
            trip.getDepartureTime(), trip.getArrivalTime(), trip.getPrice());
    }

    public record BusInfo(Long id, String name, String busNumber, String type, int totalSeats) { }
    public record RouteInfo(Long id, String source, String destination) { }
}
