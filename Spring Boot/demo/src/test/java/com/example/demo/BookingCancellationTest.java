package com.example.demo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import com.example.demo.exception.ApiException;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.BusEntity;
import com.example.demo.model.BusType;
import com.example.demo.model.RouteEntity;
import com.example.demo.model.TripEntity;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.BookingSeatRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.services.BookingService;
import com.example.demo.services.BookingWriter;
import com.example.demo.services.InMemoryRateLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class BookingCancellationTest {
    @Mock private BookingWriter writer;
    @Mock private BookingRepository bookings;
    @Mock private BookingSeatRepository bookingSeats;
    @Mock private PaymentRepository payments;
    @Mock private InMemoryRateLimiter limiter;
    @InjectMocks private BookingService service;

    @Test
    void cancellationIsRejectedInsideTwoHourWindow() {
        BookingEntity booking = booking(LocalDateTime.now().plusHours(1), BookingStatus.CONFIRMED);
        when(bookings.lockOwned(7L, 3L)).thenReturn(Optional.of(booking));
        ApiException error = assertThrows(ApiException.class,
            () -> service.cancel(7L, new AuthenticatedUser(3L, "user@example.com", com.example.demo.model.Role.USER)));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getStatus());
    }

    @Test
    void validCancellationFreesSeatsAndReturnsRefund() {
        BookingEntity booking = booking(LocalDateTime.now().plusHours(3), BookingStatus.CONFIRMED);
        when(bookings.lockOwned(7L, 3L)).thenReturn(Optional.of(booking));
        when(payments.findByBooking_Id(7L)).thenReturn(Optional.empty());
        var result = service.cancel(7L, new AuthenticatedUser(3L, "user@example.com", com.example.demo.model.Role.USER));
        assertEquals("CANCELLED", result.status());
        assertEquals(new BigDecimal("125.00"), result.refundAmount());
        verify(bookingSeats).deleteByBooking_Id(7L);
    }

    @Test
    void pendingPaymentCancellationDoesNotClaimARefund() {
        BookingEntity booking = booking(LocalDateTime.now().plusHours(3), BookingStatus.PENDING_PAYMENT);
        when(bookings.lockOwned(7L, 3L)).thenReturn(Optional.of(booking));
        when(payments.findByBooking_Id(7L)).thenReturn(Optional.empty());
        var result = service.cancel(7L, new AuthenticatedUser(3L, "user@example.com", com.example.demo.model.Role.USER));
        assertEquals(new BigDecimal("0"), result.refundAmount());
        assertEquals("CANCELLED", booking.getStatus().name());
    }

    private BookingEntity booking(LocalDateTime departure, BookingStatus status) {
        BusEntity bus = new BusEntity("Test", "TEST1", BusType.AC_SEATER, 40);
        RouteEntity route = new RouteEntity("A", "B");
        TripEntity trip = new TripEntity(bus, route, departure, departure.plusHours(3), new BigDecimal("125.00"));
        BookingEntity booking = new BookingEntity(
            new UserEntity("Passenger", "user@example.com", "hash"), trip, new BigDecimal("125.00"), "key",
            "+919876543210");
        booking.setStatus(status);
        return booking;
    }
}
