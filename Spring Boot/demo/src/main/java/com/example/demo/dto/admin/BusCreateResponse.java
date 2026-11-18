package com.example.demo.dto.admin;

import com.example.demo.model.BusEntity;

public record BusCreateResponse(Long id, String name, String busNumber, String type, int totalSeats, int seatsCreated) {
    public static BusCreateResponse from(BusEntity bus) {
        return new BusCreateResponse(bus.getId(), bus.getName(), bus.getBusNumber(), bus.getType().name(),
            bus.getTotalSeats(), bus.getTotalSeats());
    }
}
