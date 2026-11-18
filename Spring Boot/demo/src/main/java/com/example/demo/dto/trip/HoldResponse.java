package com.example.demo.dto.trip;

import java.time.Instant;
import java.util.List;

public record HoldResponse(Long tripId, List<Long> seatIds, int ttlSeconds, Instant expiresAt) { }
