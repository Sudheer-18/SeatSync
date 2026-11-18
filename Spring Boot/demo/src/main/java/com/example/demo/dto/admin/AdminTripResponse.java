package com.example.demo.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.model.TripEntity;

public record AdminTripResponse(
    Long id,
    Long busId,
    String busName,
    String busNumber,
    Long routeId,
    String source,
    String destination,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    BigDecimal price
) {
    public static AdminTripResponse from(TripEntity trip) {
        return new AdminTripResponse(trip.getId(), trip.getBus().getId(), trip.getBus().getName(),
            trip.getBus().getBusNumber(), trip.getRoute().getId(), trip.getRoute().getSource(),
            trip.getRoute().getDestination(), trip.getDepartureTime(), trip.getArrivalTime(), trip.getPrice());
    }
}
