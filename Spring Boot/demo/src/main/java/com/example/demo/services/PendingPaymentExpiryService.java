package com.example.demo.services;

import java.time.Instant;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.PaymentStatus;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.BookingSeatRepository;
import com.example.demo.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

@Component
public class PendingPaymentExpiryService {
    private final BookingRepository bookings;
    private final BookingSeatRepository bookingSeats;
    private final PaymentRepository payments;
    private final ApplicationEventPublisher events;

    public PendingPaymentExpiryService(BookingRepository bookings,
        BookingSeatRepository bookingSeats, PaymentRepository payments, ApplicationEventPublisher events) {
        this.bookings = bookings;
        this.bookingSeats = bookingSeats;
        this.payments = payments;
        this.events = events;
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void expirePendingPayments() {
        Instant now = Instant.now();
        for (Long id : payments.findExpiredBookingIds(PaymentStatus.PENDING, now)) {
            var booking = bookings.lockById(id).orElse(null);
            var payment = payments.findByBooking_Id(id).orElse(null);
            if (booking == null || payment == null || payment.getStatus() != PaymentStatus.PENDING
                || payment.getExpiresAt() == null || payment.getExpiresAt().isAfter(now)) continue;
            booking.setStatus(BookingStatus.EXPIRED);
            payment.setStatus(PaymentStatus.EXPIRED);
            bookingSeats.deleteByBooking_Id(id);
            events.publishEvent(BookingStatusNotification.from(booking, payment.getStatus()));
        }
    }
}
