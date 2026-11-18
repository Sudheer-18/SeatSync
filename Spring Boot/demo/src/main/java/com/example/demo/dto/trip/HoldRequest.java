package com.example.demo.dto.trip;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record HoldRequest(
    @NotEmpty @Size(max = 6) List<Long> seatIds
) { }
