package com.example.demo.services;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.admin.RouteRequest;
import com.example.demo.dto.admin.RouteResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.model.RouteEntity;
import com.example.demo.repository.RouteRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.util.PageRequests;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminRouteService {
    private final RouteRepository routes;
    private final TripRepository trips;

    public AdminRouteService(RouteRepository routes, TripRepository trips) {
        this.routes = routes;
        this.trips = trips;
    }

    @Transactional
    public RouteResponse create(RouteRequest request) {
        validateDifferentCities(request);
        if (routes.existsBySourceIgnoreCaseAndDestinationIgnoreCase(request.source().trim(), request.destination().trim())) {
            throw ApiException.conflict("Route already exists.");
        }
        return RouteResponse.from(routes.save(new RouteEntity(request.source().trim(), request.destination().trim())));
    }

    @Transactional(readOnly = true)
    public PageResponse<RouteResponse> list(int page, int size) {
        return PageResponse.from(routes.findAll(PageRequests.of(page, size, Sort.by("id"))).map(RouteResponse::from));
    }

    @Transactional
    public RouteResponse update(Long id, RouteRequest request) {
        validateDifferentCities(request);
        RouteEntity route = routes.findById(id).orElseThrow(() -> ApiException.notFound("Route not found."));
        String source = request.source().trim();
        String destination = request.destination().trim();
        if ((!route.getSource().equalsIgnoreCase(source) || !route.getDestination().equalsIgnoreCase(destination))
            && routes.existsBySourceIgnoreCaseAndDestinationIgnoreCaseAndIdNot(source, destination, id)) {
            throw ApiException.conflict("Route already exists.");
        }
        route.setSource(source);
        route.setDestination(destination);
        return RouteResponse.from(route);
    }

    @Transactional
    public void delete(Long id) {
        RouteEntity route = routes.findById(id).orElseThrow(() -> ApiException.notFound("Route not found."));
        if (trips.existsByRoute_Id(id)) throw ApiException.conflict("Route has trips and cannot be deleted.");
        routes.delete(route);
    }

    private void validateDifferentCities(RouteRequest request) {
        if (request.source().trim().equalsIgnoreCase(request.destination().trim())) {
            throw ApiException.badRequest("Source and destination must be different.");
        }
    }
}
