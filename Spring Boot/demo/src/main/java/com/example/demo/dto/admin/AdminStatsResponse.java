package com.example.demo.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record AdminStatsResponse(
    long totalBookings,
    long confirmedBookings,
    long cancelledBookings,
    BigDecimal totalRevenue,
    long totalTrips,
    long totalUsers,
    List<TopRoute> topRoutes
) {
    public record TopRoute(String route, long bookings) { }
}
