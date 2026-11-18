package com.example.demo.controller;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.admin.RouteRequest;
import com.example.demo.dto.admin.RouteResponse;
import com.example.demo.services.AdminRouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/routes")
public class AdminRouteController {
    private final AdminRouteService routes;

    public AdminRouteController(AdminRouteService routes) {
        this.routes = routes;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RouteResponse create(@Valid @RequestBody RouteRequest request) { return routes.create(request); }

    @GetMapping
    public PageResponse<RouteResponse> list(@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) { return routes.list(page, size); }

    @PutMapping("/{id}")
    public RouteResponse update(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        return routes.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { routes.delete(id); }
}
