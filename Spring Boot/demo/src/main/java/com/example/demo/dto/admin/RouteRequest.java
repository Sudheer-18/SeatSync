package com.example.demo.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RouteRequest(
    @NotBlank @Size(max = 100) String source,
    @NotBlank @Size(max = 100) String destination
) { }
