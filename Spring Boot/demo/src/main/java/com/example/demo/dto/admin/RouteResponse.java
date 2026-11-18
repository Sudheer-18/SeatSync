package com.example.demo.dto.admin;

import com.example.demo.model.RouteEntity;

public record RouteResponse(Long id, String source, String destination) {
    public static RouteResponse from(RouteEntity route) {
        return new RouteResponse(route.getId(), route.getSource(), route.getDestination());
    }
}
