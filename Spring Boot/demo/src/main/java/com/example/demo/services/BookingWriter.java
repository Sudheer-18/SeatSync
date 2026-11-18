package com.example.demo.services;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.example.demo.dto.booking.BookingCreateRequest;
import com.example.demo.dto.booking.BookingResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.BookingSeatEntity;
import com.example.demo.model.Gender;
import com.example.demo.model.PaymentEntity;
import com.example.demo.model.SeatEntity;
import com.example.demo.model.TripEntity;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.BookingSeatRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class BookingWriter {
    private final BookingRepository bookings;
    private final BookingSeatRepository bookingSeats;
    private final PaymentRepository payments;
    private final SeatRepository seats;
    private final TripRepository trips;
    private final UserRepository users;
    private final DatabaseSeatHoldService holds;
    private final ApplicationEventPublisher events;
    private final int paymentWindowMinutes;

    public BookingWriter(BookingRepository bookings, BookingSeatRepository bookingSeats, PaymentRepository payments,
        SeatRepository seats, TripRepository trips, UserRepository users, DatabaseSeatHoldService holds,
        ApplicationEventPublisher events,
        @Value("${seatsync.payment.pending-minutes:15}") int paymentWindowMinutes) {
        this.bookings = bookings;
        this.bookingSeats = bookingSeats;
        this.payments = payments;
        this.seats = seats;
        this.trips = trips;
        this.users = users;
        this.holds = holds;
        this.events = events;
        this.paymentWindowMinutes = paymentWindowMinutes;
    }

    @Transactional
    public BookingResponse create(Long userId, String key, BookingCreateRequest request) {
        BookingEntity replay = bookings.findByIdempotencyKey(key).orElse(null);
        if (replay != null) {
            if (!replay.getUser().getId().equals(userId)) throw ApiException.conflict("Idempotency-Key already used.");
            return response(replay);
        }
        TripEntity trip = trips.findById(request.tripId())
            .orElseThrow(() -> ApiException.notFound("Trip not found."));
        List<Long> ids = request.passengers().stream().map(item -> item.seatId()).toList();
        Set<Long> unique = new HashSet<>(ids);
        if (unique.size() != ids.size()) throw ApiException.badRequest("Each passenger must have a unique seat.");
        List<SeatEntity> allSeats = seats.lockByBus(trip.getBus().getId());
        Map<Long, SeatEntity> seatById = new HashMap<>();
        allSeats.forEach(seat -> seatById.put(seat.getId(), seat));
        if (!seatById.keySet().containsAll(ids)) throw ApiException.badRequest("Every seat must belong to this trip's bus.");
        Map<Long, String> owners = holds.ownersForBooking(trip.getId(), allSeats);
        Set<Long> userHolds = owners.entrySet().stream()
            .filter(entry -> Long.toString(userId).equals(entry.getValue())).map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());
        if (!userHolds.containsAll(unique)) throw ApiException.conflict("One or more seat holds expired or are not yours.");
        if (!unique.containsAll(userHolds)) throw ApiException.badRequest("Passenger seats must exactly match your held seats.");
        List<BookingSeatEntity> bookedSeats = bookingSeats.lockByTripAndSeats(trip.getId(), ids);
        for (var passenger : request.passengers()) {
            if (passenger.name().trim().length() < 2) {
                throw ApiException.badRequest("Passenger name must be between 2 and 100 characters.");
            }
            SeatEntity seat = seatById.get(passenger.seatId());
            if (bookedSeats.stream().anyMatch(item -> item.getSeat().getId().equals(seat.getId()))) {
                throw ApiException.conflict("Seat " + seat.getSeatNumber() + " is already booked.");
            }
            if (seat.isLadiesOnly() && passenger.gender() == Gender.MALE) {
                throw ApiException.unprocessable("Seat " + seat.getSeatNumber() + " is reserved for female passengers.");
            }
        }
        UserEntity user = users.findById(userId).orElseThrow(() -> ApiException.unauthorized("Invalid or expired token."));
        BigDecimal total = trip.getPrice().multiply(BigDecimal.valueOf(ids.size()));
        BookingEntity booking = bookings.saveAndFlush(new BookingEntity(user, trip, total, key, request.contactPhone().trim()));
        List<BookingSeatEntity> assigned = request.passengers().stream().map(passenger ->
            new BookingSeatEntity(booking, trip, seatById.get(passenger.seatId()), passenger.name().trim(),
                passenger.age(), passenger.gender())).toList();
        bookingSeats.saveAll(assigned);
        bookingSeats.flush();
        PaymentEntity payment = payments.saveAndFlush(new PaymentEntity(
            booking, total, Instant.now().plus(Duration.ofMinutes(paymentWindowMinutes))));
        holds.release(trip.getId(), ids, userId);
        events.publishEvent(BookingStatusNotification.from(booking, payment.getStatus()));
        return BookingResponse.from(booking, assigned, payment);
    }

    private BookingResponse response(BookingEntity booking) {
        return BookingResponse.from(booking, bookingSeats.findByBooking_IdOrderById(booking.getId()),
            payments.findByBooking_Id(booking.getId()).orElseThrow());
    }
}
