package com.example.demo.dto.booking;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentInstructionsResponse(
    String upiId,
    String paymentUri,
    BigDecimal amount,
    Instant expiresAt
) { }
