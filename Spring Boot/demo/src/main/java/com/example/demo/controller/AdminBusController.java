package com.example.demo.controller;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.admin.BusCreateRequest;
import com.example.demo.dto.admin.BusCreateResponse;
import com.example.demo.dto.admin.BusResponse;
import com.example.demo.dto.admin.BusUpdateRequest;
import com.example.demo.services.AdminBusService;
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
@RequestMapping("/admin/buses")
public class AdminBusController {
    private final AdminBusService buses;

    public AdminBusController(AdminBusService buses) {
        this.buses = buses;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusCreateResponse create(@Valid @RequestBody BusCreateRequest request) { return buses.create(request); }

    @GetMapping
    public PageResponse<BusResponse> list(@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) { return buses.list(page, size); }

    @PutMapping("/{id}")
    public BusResponse update(@PathVariable Long id, @Valid @RequestBody BusUpdateRequest request) {
        return buses.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { buses.delete(id); }
}
