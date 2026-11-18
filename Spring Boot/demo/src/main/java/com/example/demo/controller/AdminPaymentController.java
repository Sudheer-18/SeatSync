package com.example.demo.controller;

import com.example.demo.services.BookingPaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/bookings")
public class AdminPaymentController {
    private final BookingPaymentService payments;

    public AdminPaymentController(BookingPaymentService payments) {
        this.payments = payments;
    }

    @PostMapping("/{id}/verify-payment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verify(@PathVariable Long id) {
        payments.verify(id, true);
    }

    @PostMapping("/{id}/reject-payment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable Long id) {
        payments.verify(id, false);
    }
}
