package com.example.demo.controller;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.booking.BookingCreateRequest;
import com.example.demo.dto.booking.BookingListItem;
import com.example.demo.dto.booking.BookingResponse;
import com.example.demo.dto.booking.CancellationResponse;
import com.example.demo.dto.booking.PaymentInstructionsResponse;
import com.example.demo.dto.booking.PaymentReferenceRequest;
import com.example.demo.model.BookingStatus;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.services.BookingService;
import com.example.demo.services.BookingPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookings;
    private final BookingPaymentService payments;

    public BookingController(BookingService bookings, BookingPaymentService payments) {
        this.bookings = bookings;
        this.payments = payments;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(
        @RequestHeader(value = "Idempotency-Key", required = false) String key,
        @Valid @RequestBody BookingCreateRequest request,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        var result = bookings.create(key, request, user);
        return ResponseEntity.status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED).body(result.booking());
    }

    @GetMapping("/me")
    public PageResponse<BookingListItem> mine(
        @RequestParam(required = false) BookingStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return bookings.mine(status, page, size, user);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return bookings.get(id, user);
    }

    @GetMapping("/{id}/payment-instructions")
    public PaymentInstructionsResponse paymentInstructions(
        @PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return payments.instructions(id, user);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/payment-reference")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void paymentReference(@PathVariable Long id, @Valid @RequestBody PaymentReferenceRequest request,
        @AuthenticationPrincipal AuthenticatedUser user) {
        payments.submitReference(id, request.paymentReference().trim(), user);
    }

    @DeleteMapping("/{id}")
    public CancellationResponse cancel(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return bookings.cancel(id, user);
    }
}
