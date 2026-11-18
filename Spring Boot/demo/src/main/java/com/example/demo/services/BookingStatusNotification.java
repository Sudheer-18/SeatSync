package com.example.demo.services;

import java.math.BigDecimal;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.PaymentStatus;

public record BookingStatusNotification(
    Long bookingId,
    String customerName,
    String email,
    String phone,
    BigDecimal amount,
    String bookingStatus,
    String paymentStatus
) {
    public static BookingStatusNotification from(BookingEntity booking, PaymentStatus paymentStatus) {
        return new BookingStatusNotification(
            booking.getId(),
            booking.getUser().getName(),
            booking.getUser().getEmail(),
            booking.getContactPhone(),
            booking.getTotalAmount(),
            booking.getStatus().name(),
            paymentStatus.name()
        );
    }
}
