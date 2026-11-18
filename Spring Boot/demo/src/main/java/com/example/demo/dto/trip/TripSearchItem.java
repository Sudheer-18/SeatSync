package com.example.demo.dto.trip;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.model.TripEntity;

public record TripSearchItem(
    Long tripId,
    String busName,
    String busType,
    String source,
    String destination,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    BigDecimal price,
    int availableSeats
) {
    public static TripSearchItem from(TripEntity trip, int availableSeats) {
        return new TripSearchItem(
            trip.getId(), trip.getBus().getName(), trip.getBus().getType().name(),
            trip.getRoute().getSource(), trip.getRoute().getDestination(),
            trip.getDepartureTime(), trip.getArrivalTime(), trip.getPrice(), availableSeats);
    }
}
