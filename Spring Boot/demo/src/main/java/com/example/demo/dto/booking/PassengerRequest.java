package com.example.demo.dto.booking;

import com.example.demo.model.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PassengerRequest(
    @NotNull Long seatId,
    @NotBlank @Size(min = 2, max = 100) String name,
    @Min(1) @Max(100) int age,
    @NotNull Gender gender
) { }
