package com.example.demo.services;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.demo.exception.ApiException;
import com.example.demo.model.SeatEntity;
import com.example.demo.model.SeatHoldEntity;
import com.example.demo.repository.SeatHoldRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseSeatHoldService {
    public static final int TTL_SECONDS = 300;
    private final SeatHoldRepository holds;
    private final TripRepository trips;
    private final UserRepository users;

    public DatabaseSeatHoldService(SeatHoldRepository holds, TripRepository trips, UserRepository users) {
        this.holds = holds;
        this.trips = trips;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Map<Long, String> owners(Long tripId, List<SeatEntity> seats) {
        return owners(seats, holds.findActive(tripId, Instant.now()));
    }

    @Transactional
    public Map<Long, String> ownersForBooking(Long tripId, List<SeatEntity> seats) {
        return owners(seats, holds.lockActive(tripId, Instant.now()));
    }

    @Transactional
    public void acquire(Long tripId, List<SeatEntity> seats, Long userId) {
        List<Long> ids = seats.stream().map(SeatEntity::getId).toList();
        List<SeatHoldEntity> existing = holds.lockByTripAndSeats(tripId, ids);
        Map<Long, SeatHoldEntity> bySeat = new LinkedHashMap<>();
        existing.forEach(hold -> bySeat.put(hold.getSeat().getId(), hold));
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(TTL_SECONDS);
        for (SeatEntity seat : seats) {
            SeatHoldEntity hold = bySeat.get(seat.getId());
            if (hold != null && hold.getExpiresAt().isAfter(now)
                && !hold.getUser().getId().equals(userId)) {
                throw ApiException.conflict("Seat " + seat.getSeatNumber() + " is already held or booked.");
            }
        }
        var owner = users.getReferenceById(userId);
        var trip = trips.getReferenceById(tripId);
        for (SeatEntity seat : seats) {
            SeatHoldEntity hold = bySeat.get(seat.getId());
            if (hold == null) holds.save(new SeatHoldEntity(trip, seat, owner, expiry));
            else hold.renew(owner, expiry);
        }
    }

    @Transactional
    public void release(Long tripId, List<Long> seatIds, Long userId) {
        if (!seatIds.isEmpty()) holds.deleteByTrip_IdAndSeat_IdInAndUser_Id(tripId, seatIds, userId);
    }

    private Map<Long, String> owners(List<SeatEntity> seats, List<SeatHoldEntity> activeHolds) {
        Map<Long, String> result = new LinkedHashMap<>();
        seats.forEach(seat -> result.put(seat.getId(), null));
        activeHolds.forEach(hold -> result.put(hold.getSeat().getId(), hold.getUser().getId().toString()));
        return result;
    }
}
