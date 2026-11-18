package com.example.demo.controller;

import java.time.LocalDate;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.admin.AdminTripResponse;
import com.example.demo.dto.admin.TripCreateRequest;
import com.example.demo.dto.admin.TripUpdateRequest;
import com.example.demo.services.AdminTripService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/trips")
public class AdminTripController {
    private final AdminTripService trips;

    public AdminTripController(AdminTripService trips) {
        this.trips = trips;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminTripResponse create(@Valid @RequestBody TripCreateRequest request) { return trips.create(request); }

    @GetMapping
    public PageResponse<AdminTripResponse> list(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) { return trips.list(date, page, size); }

    @PutMapping("/{id}")
    public AdminTripResponse update(@PathVariable Long id, @Valid @RequestBody TripUpdateRequest request) {
        return trips.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { trips.delete(id); }
}
