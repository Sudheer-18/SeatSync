package com.example.demo.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.trip.TripSearchItem;
import com.example.demo.model.BusType;
import com.example.demo.repository.RouteRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatTripSearchService {
    private final RouteRepository routes;
    private final TripService trips;

    public ChatTripSearchService(RouteRepository routes, TripService trips) {
        this.routes = routes;
        this.trips = trips;
    }

    public String reply(String message) {
        String text = message.toLowerCase(Locale.ROOT);
        var route = routes.findAll().stream()
            .filter(item -> containsCity(text, item.getSource()) && containsCity(text, item.getDestination()))
            .findFirst().orElse(null);
        if (route == null) return "Please include the departure city and destination so I can search available trips.";
        LocalDate date = requestedDate(text);
        List<BusType> types = requestedBusType(text);
        PageResponse<TripSearchItem> result = trips.search(route.getSource(), route.getDestination(), date, types, "price", 0, 10);
        if (result.content().isEmpty()) return "I couldn't find available buses for that route and date.";
        TripSearchItem cheapest = result.content().get(0);
        return "Found " + result.totalElements() + " available trip(s). Cheapest is " + cheapest.busName()
            + " at " + cheapest.departureTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            + ", " + cheapest.price().toPlainString() + " rupees.";
    }

    private boolean containsCity(String message, String city) {
        String normalized = city.toLowerCase(Locale.ROOT);
        if (message.contains(normalized)) return true;
        return (normalized.contains("visakhapatnam") || normalized.equals("vizag"))
            && (message.contains("vizag") || message.contains("visakhapatnam"));
    }

    private LocalDate requestedDate(String message) {
        if (message.matches(".*\\b(tomorrow|repu)\\b.*")) return LocalDate.now().plusDays(1);
        var matcher = java.util.regex.Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b").matcher(message);
        return matcher.find() ? LocalDate.parse(matcher.group(1)) : LocalDate.now();
    }

    private List<BusType> requestedBusType(String message) {
        if (message.contains("non ac") && message.contains("sleeper")) return List.of(BusType.NON_AC_SLEEPER);
        if (message.contains("non ac") && message.contains("seater")) return List.of(BusType.NON_AC_SEATER);
        if (message.contains("ac") && message.contains("sleeper")) return List.of(BusType.AC_SLEEPER);
        if (message.contains("ac") && message.contains("seater")) return List.of(BusType.AC_SEATER);
        return List.of();
    }
}
