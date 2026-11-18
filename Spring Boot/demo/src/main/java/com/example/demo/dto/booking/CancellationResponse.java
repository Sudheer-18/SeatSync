package com.example.demo.dto.booking;

import java.math.BigDecimal;

public record CancellationResponse(Long bookingId, String status, BigDecimal refundAmount, String message) { }
