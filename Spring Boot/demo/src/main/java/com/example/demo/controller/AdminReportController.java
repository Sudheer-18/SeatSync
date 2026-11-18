package com.example.demo.controller;

import java.time.LocalDate;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.admin.AdminBookingItem;
import com.example.demo.dto.admin.AdminStatsResponse;
import com.example.demo.model.BookingStatus;
import com.example.demo.services.AdminReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminReportController {
    private final AdminReportService reports;

    public AdminReportController(AdminReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/bookings")
    public PageResponse<AdminBookingItem> bookings(
        @RequestParam(required = false) Long tripId,
        @RequestParam(required = false) BookingStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) { return reports.bookings(tripId, status, from, to, page, size); }

    @GetMapping("/stats")
    public AdminStatsResponse stats(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) { return reports.stats(from, to); }
}
