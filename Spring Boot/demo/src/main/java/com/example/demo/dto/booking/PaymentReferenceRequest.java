package com.example.demo.dto.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PaymentReferenceRequest(
    @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{8,100}") String paymentReference
) { }
