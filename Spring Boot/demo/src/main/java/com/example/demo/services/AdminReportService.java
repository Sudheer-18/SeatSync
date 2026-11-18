package com.example.demo.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.admin.AdminBookingItem;
import com.example.demo.dto.admin.AdminStatsResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.BookingStatus;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.PageRequests;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminReportService {
    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final TripRepository trips;
    private final UserRepository users;

    public AdminReportService(BookingRepository bookings, PaymentRepository payments,
        TripRepository trips, UserRepository users) {
        this.bookings = bookings;
        this.payments = payments;
        this.trips = trips;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminBookingItem> bookings(Long tripId, BookingStatus status, LocalDate from, LocalDate to,
        int page, int size) {
        DateRange range = range(from, to);
        Page<BookingEntity> result = bookings.searchAdmin(tripId, status, range.from(), range.to(),
            PageRequests.newest(page, size));
        return PageResponse.from(result.map(booking -> AdminBookingItem.from(booking,
            payments.findByBooking_Id(booking.getId()).orElseThrow())));
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse stats(LocalDate from, LocalDate to) {
        DateRange range = range(from, to);
        List<AdminStatsResponse.TopRoute> top = bookings.topRoutes(BookingStatus.CONFIRMED,
                range.from(), range.to(), PageRequest.of(0, 5))
            .stream().map(row -> new AdminStatsResponse.TopRoute(row[0] + " to " + row[1], ((Number) row[2]).longValue()))
            .toList();
        return new AdminStatsResponse(
            bookings.countCreatedBetween(range.from(), range.to()),
            bookings.countByStatusAndCreatedBetween(BookingStatus.CONFIRMED, range.from(), range.to()),
            bookings.countByStatusAndCreatedBetween(BookingStatus.CANCELLED, range.from(), range.to()),
            bookings.revenueByStatusAndCreatedBetween(BookingStatus.CONFIRMED, range.from(), range.to()),
            trips.count(), users.count(), top);
    }

    private DateRange range(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) throw ApiException.badRequest("to must be on or after from.");
        Instant start = from == null ? null : from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = to == null ? null : to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return new DateRange(start, end);
    }

    private record DateRange(Instant from, Instant to) { }
}
