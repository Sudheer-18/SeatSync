package com.example.demo.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.admin.AdminTripResponse;
import com.example.demo.dto.admin.TripCreateRequest;
import com.example.demo.dto.admin.TripUpdateRequest;
import com.example.demo.exception.ApiException;
import com.example.demo.model.BusEntity;
import com.example.demo.model.RouteEntity;
import com.example.demo.model.TripEntity;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.BusRepository;
import com.example.demo.repository.RouteRepository;
import com.example.demo.repository.SeatHoldRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.util.PageRequests;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminTripService {
    private final TripRepository trips;
    private final BusRepository buses;
    private final RouteRepository routes;
    private final BookingRepository bookings;
    private final SeatHoldRepository seatHolds;

    public AdminTripService(TripRepository trips, BusRepository buses, RouteRepository routes,
        BookingRepository bookings, SeatHoldRepository seatHolds) {
        this.trips = trips;
        this.buses = buses;
        this.routes = routes;
        this.bookings = bookings;
        this.seatHolds = seatHolds;
    }

    @Transactional
    public AdminTripResponse create(TripCreateRequest request) {
        validateTimes(request.departureTime(), request.arrivalTime(), request.price());
        BusEntity bus = buses.lockById(request.busId()).orElseThrow(() -> ApiException.notFound("Bus not found."));
        RouteEntity route = routes.findById(request.routeId()).orElseThrow(() -> ApiException.notFound("Route not found."));
        assertNoOverlap(bus.getId(), null, request.departureTime(), request.arrivalTime());
        return AdminTripResponse.from(trips.save(new TripEntity(
            bus, route, request.departureTime(), request.arrivalTime(), request.price())));
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminTripResponse> list(LocalDate date, int page, int size) {
        LocalDateTime start = date == null ? null : date.atStartOfDay();
        LocalDateTime end = date == null ? null : date.plusDays(1).atStartOfDay();
        return PageResponse.from(trips.findAdmin(start, end,
            PageRequests.of(page, size, Sort.by(Sort.Direction.DESC, "departureTime"))).map(AdminTripResponse::from));
    }

    @Transactional
    public AdminTripResponse update(Long id, TripUpdateRequest request) {
        TripEntity trip = trips.findById(id).orElseThrow(() -> ApiException.notFound("Trip not found."));
        validateTimes(request.departureTime(), request.arrivalTime(), request.price());
        boolean changed = !trip.getDepartureTime().equals(request.departureTime())
            || !trip.getArrivalTime().equals(request.arrivalTime()) || trip.getPrice().compareTo(request.price()) != 0;
        if (changed && bookings.existsByTrip_Id(id)) throw ApiException.conflict("Trip with bookings cannot be changed.");
        buses.lockById(trip.getBus().getId()).orElseThrow(() -> ApiException.notFound("Bus not found."));
        assertNoOverlap(trip.getBus().getId(), id, request.departureTime(), request.arrivalTime());
        trip.setDepartureTime(request.departureTime());
        trip.setArrivalTime(request.arrivalTime());
        trip.setPrice(request.price());
        return AdminTripResponse.from(trip);
    }

    @Transactional
    public void delete(Long id) {
        TripEntity trip = trips.findById(id).orElseThrow(() -> ApiException.notFound("Trip not found."));
        if (bookings.existsByTrip_Id(id)) throw ApiException.conflict("Trip has bookings and cannot be deleted.");
        seatHolds.deleteByTrip_Id(id);
        trips.delete(trip);
    }

    private void assertNoOverlap(Long busId, Long ignoredTrip, LocalDateTime departure, LocalDateTime arrival) {
        boolean overlap = ignoredTrip == null
            ? trips.existsByBus_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(busId, arrival, departure)
            : trips.existsByBus_IdAndIdNotAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
                busId, ignoredTrip, arrival, departure);
        if (overlap) throw ApiException.conflict("The bus already has a trip during this time.");
    }

    private void validateTimes(LocalDateTime departure, LocalDateTime arrival, java.math.BigDecimal price) {
        if (!departure.isAfter(LocalDateTime.now())) throw ApiException.badRequest("Departure time must be in the future.");
        if (!arrival.isAfter(departure)) throw ApiException.badRequest("Arrival must be after departure.");
        if (price.signum() <= 0) throw ApiException.badRequest("Price must be greater than zero.");
    }
}
