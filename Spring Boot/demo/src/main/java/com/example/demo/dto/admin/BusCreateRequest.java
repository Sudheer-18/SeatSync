package com.example.demo.dto.admin;

import java.util.List;
import com.example.demo.model.BusType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BusCreateRequest(
    @NotBlank @Size(min = 2, max = 100) String name,
    @NotBlank @Size(max = 30) String busNumber,
    @NotNull BusType type,
    @Min(10) @Max(60) int totalSeats,
    @Size(max = 60) List<@NotBlank @Size(max = 8) String> ladiesOnlySeats
) { }
