package com.example.demo.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.booking.BookingCreateRequest;
import com.example.demo.dto.booking.BookingCreateResult;
import com.example.demo.dto.booking.BookingListItem;
import com.example.demo.dto.booking.BookingResponse;
import com.example.demo.dto.booking.CancellationResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.BookingStatus;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.BookingSeatRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.util.PageRequests;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final BookingWriter writer;
    private final BookingRepository bookings;
    private final BookingSeatRepository bookingSeats;
    private final PaymentRepository payments;
    private final InMemoryRateLimiter limiter;

    public BookingService(BookingWriter writer, BookingRepository bookings, BookingSeatRepository bookingSeats,
        PaymentRepository payments, InMemoryRateLimiter limiter) {
        this.writer = writer;
        this.bookings = bookings;
        this.bookingSeats = bookingSeats;
        this.payments = payments;
        this.limiter = limiter;
    }

    public BookingCreateResult create(String key, BookingCreateRequest request, AuthenticatedUser user) {
        if (key == null || key.isBlank()) throw ApiException.badRequest("Idempotency-Key header is required.");
        try {
            UUID parsed = UUID.fromString(key);
            if (!parsed.toString().equalsIgnoreCase(key)) throw ApiException.badRequest("Idempotency-Key must be a UUID.");
        } catch (IllegalArgumentException exception) {
            throw ApiException.badRequest("Idempotency-Key must be a UUID.");
        }
        BookingEntity existing = bookings.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            if (!existing.getUser().getId().equals(user.id())) throw ApiException.conflict("Idempotency-Key already used.");
            return new BookingCreateResult(BookingResponse.from(existing,
                bookingSeats.findByBooking_IdOrderById(existing.getId()),
                payments.findByBooking_Id(existing.getId()).orElseThrow()), true);
        }
        limiter.enforce("booking", user.id(), 10, Duration.ofMinutes(1));
        try {
            return new BookingCreateResult(writer.create(user.id(), key, request), false);
        } catch (DataIntegrityViolationException exception) {
            BookingEntity replay = bookings.findByIdempotencyKey(key).orElse(null);
            if (replay == null || !replay.getUser().getId().equals(user.id())) {
                throw ApiException.conflict("A seat was booked concurrently or the idempotency key is already in use.");
            }
            return new BookingCreateResult(BookingResponse.from(replay,
                bookingSeats.findByBooking_IdOrderById(replay.getId()),
                payments.findByBooking_Id(replay.getId()).orElseThrow()), true);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingListItem> mine(BookingStatus status, int page, int size, AuthenticatedUser user) {
        Page<BookingEntity> result = bookings.findMine(user.id(), status, PageRequests.newest(page, size));
        List<BookingListItem> content = result.getContent().stream().map(booking ->
            BookingListItem.from(booking, bookingSeats.findByBooking_IdOrderById(booking.getId()))).toList();
        return PageResponse.from(new PageImpl<>(content, result.getPageable(), result.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id, AuthenticatedUser user) {
        BookingEntity booking = bookings.findByIdAndUser_Id(id, user.id())
            .orElseThrow(() -> ApiException.notFound("Booking not found."));
        return BookingResponse.from(booking, bookingSeats.findByBooking_IdOrderById(id),
            payments.findByBooking_Id(id).orElseThrow());
    }

    @Transactional
    public CancellationResponse cancel(Long id, AuthenticatedUser user) {
        BookingEntity booking = bookings.lockOwned(id, user.id())
            .orElseThrow(() -> ApiException.notFound("Booking not found."));
        if (booking.getStatus() != BookingStatus.CONFIRMED
            && booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw ApiException.conflict("Booking is already cancelled or expired.");
        }
        boolean paid = booking.getStatus() == BookingStatus.CONFIRMED;
        if (paid && booking.getTrip().getDepartureTime().isBefore(LocalDateTime.now().plusHours(2))) {
            throw ApiException.unprocessable("Cancellation is closed less than two hours before departure.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingSeats.deleteByBooking_Id(id);
        payments.findByBooking_Id(id).ifPresent(payment -> payment.setStatus(paid
            ? com.example.demo.model.PaymentStatus.REFUNDED : com.example.demo.model.PaymentStatus.CANCELLED));
        return new CancellationResponse(id, BookingStatus.CANCELLED.name(), paid ? booking.getTotalAmount() : java.math.BigDecimal.ZERO,
            paid ? "Booking cancelled and refunded" : "Pending payment booking cancelled.");
    }
}
