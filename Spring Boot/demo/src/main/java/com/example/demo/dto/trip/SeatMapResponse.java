package com.example.demo.dto.trip;

import java.util.List;
import com.example.demo.model.SeatStatus;

public record SeatMapResponse(Long tripId, List<SeatInfo> seats) {
    public record SeatInfo(
        Long seatId,
        String seatNumber,
        String seatType,
        boolean ladiesOnly,
        SeatStatus status,
        boolean heldByMe
    ) { }
}
