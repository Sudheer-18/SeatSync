package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.trip.HoldRequest;
import com.example.demo.dto.trip.HoldResponse;
import com.example.demo.dto.trip.SeatMapResponse;
import com.example.demo.dto.trip.TripDetailsResponse;
import com.example.demo.dto.trip.TripSearchItem;
import com.example.demo.model.BusType;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.services.TripService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trips")
public class TripController {
    private final TripService trips;

    public TripController(TripService trips) {
        this.trips = trips;
    }

    @GetMapping("/search")
    public PageResponse<TripSearchItem> search(
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false) List<BusType> type,
        @RequestParam(required = false) String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return trips.search(from, to, date, type, sort, page, size);
    }

    @GetMapping("/{id}")
    public TripDetailsResponse getTrip(@PathVariable Long id) {
        return trips.getTrip(id);
    }

    @GetMapping("/{id}/seats")
    public SeatMapResponse getSeats(@PathVariable Long id,
        @AuthenticationPrincipal(errorOnInvalidType = false) AuthenticatedUser user) {
        return trips.getSeats(id, user);
    }

    @PostMapping("/{id}/hold")
    public HoldResponse hold(@PathVariable Long id, @Valid @RequestBody HoldRequest request,
        @AuthenticationPrincipal AuthenticatedUser user) {
        return trips.hold(id, request, user);
    }

    @DeleteMapping("/{id}/hold")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@PathVariable Long id, @Valid @RequestBody(required = false) HoldRequest request,
        @AuthenticationPrincipal AuthenticatedUser user) {
        trips.release(id, request, user);
    }
}
