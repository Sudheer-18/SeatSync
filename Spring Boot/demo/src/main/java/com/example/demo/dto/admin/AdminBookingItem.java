package com.example.demo.dto.admin;

import java.math.BigDecimal;
import java.time.Instant;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.PaymentEntity;

public record AdminBookingItem(
    Long bookingId,
    String userName,
    String userEmail,
    Long tripId,
    String route,
    String status,
    BigDecimal totalAmount,
    Instant createdAt,
    String paymentStatus,
    String paymentReference
) {
    public static AdminBookingItem from(BookingEntity booking, PaymentEntity payment) {
        return new AdminBookingItem(booking.getId(), booking.getUser().getName(), booking.getUser().getEmail(),
            booking.getTrip().getId(), booking.getTrip().getRoute().getSource() + " to "
                + booking.getTrip().getRoute().getDestination(),
            booking.getStatus().name(), booking.getTotalAmount(), booking.getCreatedAt(),
            payment.getStatus().name(), payment.getPaymentReference());
    }
}
