package com.example.demo.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record TripCreateRequest(
    @NotNull Long busId,
    @NotNull Long routeId,
    @NotNull LocalDateTime departureTime,
    @NotNull LocalDateTime arrivalTime,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price
) { }
