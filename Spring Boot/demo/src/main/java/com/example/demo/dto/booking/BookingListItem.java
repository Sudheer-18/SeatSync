package com.example.demo.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.BookingSeatEntity;

public record BookingListItem(
    Long bookingId,
    String source,
    String destination,
    LocalDateTime departureTime,
    List<String> seatNumbers,
    String status,
    BigDecimal totalAmount
) {
    public static BookingListItem from(BookingEntity booking, List<BookingSeatEntity> seats) {
        return new BookingListItem(booking.getId(), booking.getTrip().getRoute().getSource(),
            booking.getTrip().getRoute().getDestination(), booking.getTrip().getDepartureTime(),
            seats.stream().map(item -> item.getSeat().getSeatNumber()).toList(),
            booking.getStatus().name(), booking.getTotalAmount());
    }
}
