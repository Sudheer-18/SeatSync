package com.example.demo.dto.admin;

import com.example.demo.model.BusEntity;

public record BusResponse(Long id, String name, String busNumber, String type, int totalSeats) {
    public static BusResponse from(BusEntity bus) {
        return new BusResponse(bus.getId(), bus.getName(), bus.getBusNumber(), bus.getType().name(), bus.getTotalSeats());
    }
}
