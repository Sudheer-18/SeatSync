package com.example.demo.dto.admin;

import com.example.demo.model.BusType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BusUpdateRequest(
    @NotBlank @Size(min = 2, max = 100) String name,
    @NotBlank @Size(max = 30) String busNumber,
    @NotNull BusType type
) { }
