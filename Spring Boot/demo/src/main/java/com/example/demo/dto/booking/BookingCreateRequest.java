package com.example.demo.dto.booking;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookingCreateRequest(
    @NotNull Long tripId,
    @NotBlank @Pattern(regexp = "\\+[1-9][0-9]{7,14}") String contactPhone,
    @NotEmpty @Size(max = 6) List<@Valid PassengerRequest> passengers
) { }
