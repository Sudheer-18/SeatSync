package com.example.demo.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.trip.HoldRequest;
import com.example.demo.dto.trip.HoldResponse;
import com.example.demo.dto.trip.SeatMapResponse;
import com.example.demo.dto.trip.TripDetailsResponse;
import com.example.demo.dto.trip.TripSearchItem;
import com.example.demo.exception.ApiException;
import com.example.demo.model.BusType;
import com.example.demo.model.SeatEntity;
import com.example.demo.model.SeatStatus;
import com.example.demo.model.TripEntity;
import com.example.demo.repository.BookingSeatRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.util.PageRequests;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripService {
    private final TripRepository trips;
    private final SeatRepository seats;
    private final BookingSeatRepository bookingSeats;
    private final DatabaseSeatHoldService holds;
    private final InMemoryRateLimiter limiter;

    public TripService(TripRepository trips, SeatRepository seats, BookingSeatRepository bookingSeats,
        DatabaseSeatHoldService holds, InMemoryRateLimiter limiter) {
        this.trips = trips;
        this.seats = seats;
        this.bookingSeats = bookingSeats;
        this.holds = holds;
        this.limiter = limiter;
    }

    @Transactional(readOnly = true)
    public PageResponse<TripSearchItem> search(String from, String to, LocalDate date, List<BusType> types,
        String sort, int page, int size) {
        if (from == null || from.isBlank() || to == null || to.isBlank() || date == null) {
            throw ApiException.badRequest("from, to, and date are required.");
        }
        Sort ordering = switch (sort == null || sort.isBlank() ? "departure" : sort) {
            case "price" -> Sort.by("price").ascending();
            case "departure" -> Sort.by("departureTime").ascending();
            default -> throw ApiException.badRequest("sort must be price or departure.");
        };
        PageRequest request = PageRequests.of(page, size, ordering.and(Sort.by("id")));
        if (date.isBefore(LocalDate.now())) return PageResponse.from(Page.empty(request));
        List<BusType> filters = types == null || types.isEmpty() ? List.of(BusType.values()[0]) : types.stream().distinct().toList();
        Page<TripEntity> results = trips.search(from.trim(), to.trim(), date.atStartOfDay(),
            date.plusDays(1).atStartOfDay(), types != null && !types.isEmpty(), filters, request);
        List<TripSearchItem> items = results.getContent().stream().map(trip ->
            TripSearchItem.from(trip, Math.max(0, trip.getBus().getTotalSeats() - Math.toIntExact(bookingSeats.countByTrip_Id(trip.getId()))))
        ).toList();
        return PageResponse.from(new PageImpl<>(items, request, results.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public TripDetailsResponse getTrip(Long tripId) {
        return TripDetailsResponse.from(findTrip(tripId));
    }

    @Transactional(readOnly = true)
    public SeatMapResponse getSeats(Long tripId, AuthenticatedUser user) {
        TripEntity trip = findTrip(tripId);
        List<SeatEntity> tripSeats = seats.findByBus_IdOrderBySeatNumber(trip.getBus().getId());
        Set<Long> booked = new HashSet<>(bookingSeats.findByTrip_Id(tripId).stream()
            .map(item -> item.getSeat().getId()).toList());
        Map<Long, String> owners = holds.owners(tripId, tripSeats);
        List<SeatMapResponse.SeatInfo> response = tripSeats.stream().map(seat -> {
            String owner = owners.get(seat.getId());
            SeatStatus status = booked.contains(seat.getId()) ? SeatStatus.BOOKED
                : owner != null ? SeatStatus.HELD : SeatStatus.AVAILABLE;
            boolean heldByMe = user != null && user.id().toString().equals(owner);
            return new SeatMapResponse.SeatInfo(seat.getId(), seat.getSeatNumber(), seat.getSeatType(),
                seat.isLadiesOnly(), status, heldByMe);
        }).toList();
        return new SeatMapResponse(tripId, response);
    }

    @Transactional
    public HoldResponse hold(Long tripId, HoldRequest request, AuthenticatedUser user) {
        TripEntity trip = findTrip(tripId);
        List<Long> ids = validateIds(request.seatIds());
        limiter.enforce("hold", user.id(), 20, Duration.ofMinutes(1));
        List<SeatEntity> selected = seats.lockByBusAndIds(trip.getBus().getId(), ids);
        if (selected.size() != ids.size()) throw ApiException.badRequest("Every seat must belong to this trip's bus.");
        Set<Long> booked = new HashSet<>(bookingSeats.lockByTripAndSeats(tripId, ids).stream()
            .map(item -> item.getSeat().getId()).toList());
        for (SeatEntity seat : selected) {
            if (booked.contains(seat.getId()))
                throw ApiException.conflict("Seat " + seat.getSeatNumber() + " is already held or booked.");
        }
        holds.acquire(tripId, selected, user.id());
        return new HoldResponse(tripId, ids, DatabaseSeatHoldService.TTL_SECONDS,
            Instant.now().plusSeconds(DatabaseSeatHoldService.TTL_SECONDS));
    }

    @Transactional
    public void release(Long tripId, HoldRequest request, AuthenticatedUser user) {
        TripEntity trip = findTrip(tripId);
        List<SeatEntity> locked;
        if (request == null) {
            locked = seats.lockByBus(trip.getBus().getId());
        } else {
            List<Long> requestedIds = validateIds(request.seatIds());
            locked = seats.lockByBusAndIds(trip.getBus().getId(), requestedIds);
            if (locked.size() != requestedIds.size()) {
                throw ApiException.badRequest("Every seat must belong to this trip's bus.");
            }
        }
        List<Long> ids = locked.stream().map(SeatEntity::getId).toList();
        holds.release(tripId, ids, user.id());
    }

    private TripEntity findTrip(Long tripId) {
        return trips.findById(tripId).orElseThrow(() -> ApiException.notFound("Trip not found."));
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 6 || ids.stream().anyMatch(id -> id == null)
            || new HashSet<>(ids).size() != ids.size()) {
            throw ApiException.badRequest("seatIds must contain 1 to 6 unique seat IDs.");
        }
        return ids;
    }
}
