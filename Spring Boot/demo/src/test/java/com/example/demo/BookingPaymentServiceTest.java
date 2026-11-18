package com.example.demo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import com.example.demo.exception.ApiException;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.BusEntity;
import com.example.demo.model.BusType;
import com.example.demo.model.PaymentEntity;
import com.example.demo.model.PaymentStatus;
import com.example.demo.model.RouteEntity;
import com.example.demo.model.TripEntity;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.BookingSeatRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.services.BookingStatusNotification;
import com.example.demo.services.BookingPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingPaymentServiceTest {
    @Mock private BookingRepository bookings;
    @Mock private PaymentRepository payments;
    @Mock private BookingSeatRepository bookingSeats;
    @Mock private ApplicationEventPublisher events;
    private BookingPaymentService service;
    private BookingEntity booking;
    private PaymentEntity payment;

    @BeforeEach
    void setUp() {
        service = new BookingPaymentService(bookings, payments, bookingSeats, events, "seatsync@upi");
        var trip = new TripEntity(new BusEntity("Test", "T1", BusType.AC_SEATER, 40),
            new RouteEntity("A", "B"), LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusHours(2), new BigDecimal("650.00"));
        booking = new BookingEntity(new UserEntity("Test User", "user@example.com", "hash"),
            trip, new BigDecimal("650.00"), "key", "+919876543210");
        payment = new PaymentEntity(booking, booking.getTotalAmount(), Instant.now().plusSeconds(900));
    }

    @Test
    void buildsPaymentLinkAndOnlyConfirmsAfterAdminVerification() {
        var user = new AuthenticatedUser(7L, "user@example.com", com.example.demo.model.Role.USER);
        when(bookings.findByIdAndUser_Id(10L, 7L)).thenReturn(Optional.of(booking));
        when(payments.findByBooking_Id(10L)).thenReturn(Optional.of(payment));
        var instructions = service.instructions(10L, user);
        assertEquals("seatsync@upi", instructions.upiId());
        assertEquals(true, instructions.paymentUri().startsWith("upi://pay?"));

        payment.setPaymentReference("UTR12345678");
        when(bookings.lockById(10L)).thenReturn(Optional.of(booking));
        service.verify(10L, true);
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        ArgumentCaptor<BookingStatusNotification> event = ArgumentCaptor.forClass(BookingStatusNotification.class);
        verify(events).publishEvent(event.capture());
        var notification = event.getValue();
        assertEquals("SUCCESS", notification.paymentStatus());
        assertEquals("user@example.com", notification.email());
        assertEquals("+919876543210", notification.phone());
    }

    @Test
    void rejectsUnverifiedReferenceAndFreesItsSeats() {
        when(bookings.lockById(10L)).thenReturn(Optional.of(booking));
        when(payments.findByBooking_Id(10L)).thenReturn(Optional.of(payment));
        assertThrows(ApiException.class, () -> service.verify(10L, true));

        payment.setPaymentReference("UTR12345678");
        service.verify(10L, false);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(PaymentStatus.REJECTED, payment.getStatus());
        verify(bookingSeats).deleteByBooking_Id(10L);
        ArgumentCaptor<BookingStatusNotification> event = ArgumentCaptor.forClass(BookingStatusNotification.class);
        verify(events).publishEvent(event.capture());
        assertEquals("REJECTED",
            event.getValue().paymentStatus());
    }
}
