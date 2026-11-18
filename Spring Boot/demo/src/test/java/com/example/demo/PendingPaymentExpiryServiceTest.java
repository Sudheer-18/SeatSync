package com.example.demo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
import com.example.demo.services.PendingPaymentExpiryService;
import com.example.demo.services.BookingStatusNotification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class PendingPaymentExpiryServiceTest {
    @Mock private BookingRepository bookings;
    @Mock private BookingSeatRepository bookingSeats;
    @Mock private PaymentRepository payments;
    @Mock private ApplicationEventPublisher events;

    @Test
    void expiresUnpaidBookingAndFreesItsSeats() {
        var departure = LocalDateTime.now().plusDays(1);
        var trip = new TripEntity(new BusEntity("Test", "T1", BusType.AC_SEATER, 40),
            new RouteEntity("A", "B"), departure, departure.plusHours(2), new BigDecimal("650"));
        var booking = new BookingEntity(new UserEntity("Test User", "user@example.com", "hash"),
            trip, new BigDecimal("650"), "key", "+919876543210");
        var payment = new PaymentEntity(booking, booking.getTotalAmount(), Instant.now().minusSeconds(1));
        when(payments.findExpiredBookingIds(eq(PaymentStatus.PENDING), any(Instant.class))).thenReturn(List.of(1L));
        when(bookings.lockById(1L)).thenReturn(Optional.of(booking));
        when(payments.findByBooking_Id(1L)).thenReturn(Optional.of(payment));

        new PendingPaymentExpiryService(bookings, bookingSeats, payments, events).expirePendingPayments();

        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
        verify(bookingSeats).deleteByBooking_Id(1L);
        ArgumentCaptor<BookingStatusNotification> event = ArgumentCaptor.forClass(BookingStatusNotification.class);
        verify(events).publishEvent(event.capture());
        assertEquals("EXPIRED",
            event.getValue().paymentStatus());
    }
}
