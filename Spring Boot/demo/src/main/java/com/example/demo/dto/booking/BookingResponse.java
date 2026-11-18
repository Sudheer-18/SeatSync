package com.example.demo.dto.booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.BookingSeatEntity;
import com.example.demo.model.PaymentEntity;

public record BookingResponse(
    Long bookingId,
    Long tripId,
    String status,
    BigDecimal totalAmount,
    List<PassengerSeat> seats,
    PaymentInfo payment,
    RouteInfo route,
    BusInfo bus,
    LocalDateTime departureTime,
    Instant createdAt
) {
    public static BookingResponse from(BookingEntity booking, List<BookingSeatEntity> seats, PaymentEntity payment) {
        return new BookingResponse(booking.getId(), booking.getTrip().getId(), booking.getStatus().name(),
            booking.getTotalAmount(), seats.stream().map(PassengerSeat::from).toList(),
            new PaymentInfo(payment.getId(), payment.getStatus().name(), payment.getAmount(),
                payment.getPaymentReference(), payment.getExpiresAt()),
            new RouteInfo(booking.getTrip().getRoute().getSource(), booking.getTrip().getRoute().getDestination()),
            new BusInfo(booking.getTrip().getBus().getName(), booking.getTrip().getBus().getBusNumber(),
                booking.getTrip().getBus().getType().name()),
            booking.getTrip().getDepartureTime(), booking.getCreatedAt());
    }

    public record PassengerSeat(String seatNumber, String passengerName, int age, String gender) {
        static PassengerSeat from(BookingSeatEntity seat) {
            return new PassengerSeat(seat.getSeat().getSeatNumber(), seat.getPassengerName(), seat.getAge(), seat.getGender().name());
        }
    }
    public record PaymentInfo(Long id, String status, BigDecimal amount, String paymentReference, Instant expiresAt) { }
    public record RouteInfo(String source, String destination) { }
    public record BusInfo(String name, String busNumber, String type) { }
}
