package com.example.demo.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import com.example.demo.dto.booking.PaymentInstructionsResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.PaymentEntity;
import com.example.demo.model.PaymentStatus;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.BookingSeatRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingPaymentService {
    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final BookingSeatRepository bookingSeats;
    private final ApplicationEventPublisher events;
    private final String upiId;

    public BookingPaymentService(BookingRepository bookings, PaymentRepository payments,
        BookingSeatRepository bookingSeats, ApplicationEventPublisher events,
        @Value("${seatsync.payment.upi-id:}") String upiId) {
        this.bookings = bookings;
        this.payments = payments;
        this.bookingSeats = bookingSeats;
        this.events = events;
        this.upiId = upiId.trim();
    }

    @Transactional(readOnly = true)
    public PaymentInstructionsResponse instructions(Long id, AuthenticatedUser user) {
        BookingEntity booking = owned(id, user);
        PaymentEntity payment = payment(id);
        ensurePending(booking, payment);
        ensureNotExpired(payment);
        String uri = upiId.isBlank() ? null : "upi://pay?pa=" + encode(upiId)
            + "&pn=SeatSync&am=" + payment.getAmount().toPlainString()
            + "&cu=INR&tn=" + encode("SeatSync booking " + id);
        return new PaymentInstructionsResponse(upiId.isBlank() ? null : upiId,
            uri, payment.getAmount(), payment.getExpiresAt());
    }

    @Transactional
    public void submitReference(Long id, String reference, AuthenticatedUser user) {
        BookingEntity booking = bookings.lockOwned(id, user.id())
            .orElseThrow(() -> ApiException.notFound("Booking not found."));
        PaymentEntity payment = payment(id);
        ensurePending(booking, payment);
        ensureNotExpired(payment);
        if (payment.getPaymentReference() != null && !payment.getPaymentReference().equals(reference)) {
            throw ApiException.conflict("A payment reference has already been submitted.");
        }
        if (payment.getPaymentReference() == null && payments.existsByPaymentReference(reference)) {
            throw ApiException.conflict("This payment reference was already submitted.");
        }
        payment.setPaymentReference(reference);
    }

    @Transactional
    public void verify(Long id, boolean approved) {
        BookingEntity booking = bookings.lockById(id).orElseThrow(() -> ApiException.notFound("Booking not found."));
        PaymentEntity payment = payment(id);
        ensurePending(booking, payment);
        if (payment.getPaymentReference() == null) throw ApiException.conflict("Customer has not submitted a payment reference.");
        ensureNotExpired(payment);
        payment.setStatus(approved ? PaymentStatus.SUCCESS : PaymentStatus.REJECTED);
        booking.setStatus(approved ? BookingStatus.CONFIRMED : BookingStatus.CANCELLED);
        if (!approved) bookingSeats.deleteByBooking_Id(id);
        events.publishEvent(BookingStatusNotification.from(booking, payment.getStatus()));
    }

    private BookingEntity owned(Long id, AuthenticatedUser user) {
        return bookings.findByIdAndUser_Id(id, user.id())
            .orElseThrow(() -> ApiException.notFound("Booking not found."));
    }

    private PaymentEntity payment(Long bookingId) {
        return payments.findByBooking_Id(bookingId)
            .orElseThrow(() -> ApiException.notFound("Payment not found."));
    }

    private void ensurePending(BookingEntity booking, PaymentEntity payment) {
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT || payment.getStatus() != PaymentStatus.PENDING) {
            throw ApiException.conflict("This booking is not awaiting payment.");
        }
    }

    private void ensureNotExpired(PaymentEntity payment) {
        if (payment.getExpiresAt() == null || !payment.getExpiresAt().isAfter(Instant.now())) {
            throw ApiException.conflict("Payment window expired.");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
