package com.example.demo.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.admin.BusCreateRequest;
import com.example.demo.dto.admin.BusCreateResponse;
import com.example.demo.dto.admin.BusResponse;
import com.example.demo.dto.admin.BusUpdateRequest;
import com.example.demo.exception.ApiException;
import com.example.demo.model.BusEntity;
import com.example.demo.model.SeatEntity;
import com.example.demo.repository.BusRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.util.PageRequests;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBusService {
    private final BusRepository buses;
    private final SeatRepository seats;
    private final TripRepository trips;

    public AdminBusService(BusRepository buses, SeatRepository seats, TripRepository trips) {
        this.buses = buses;
        this.seats = seats;
        this.trips = trips;
    }

    @Transactional
    public BusCreateResponse create(BusCreateRequest request) {
        if (request.name().trim().length() < 2) throw ApiException.badRequest("Bus name must be at least 2 characters.");
        String number = request.busNumber().trim().toUpperCase();
        if (buses.existsByBusNumberIgnoreCase(number)) throw ApiException.conflict("Bus number already exists.");
        Set<String> ladies = validateLadiesSeats(request.ladiesOnlySeats(), request.totalSeats());
        BusEntity bus = buses.saveAndFlush(new BusEntity(request.name().trim(), number, request.type(), request.totalSeats()));
        List<SeatEntity> generated = new ArrayList<>();
        for (int i = 0; i < bus.getTotalSeats(); i++) {
            String seatNumber = seatNumber(i);
            generated.add(new SeatEntity(bus, seatNumber, bus.getType().seatType(), ladies.contains(seatNumber)));
        }
        seats.saveAll(generated);
        return BusCreateResponse.from(bus);
    }

    @Transactional(readOnly = true)
    public PageResponse<BusResponse> list(int page, int size) {
        return PageResponse.from(buses.findAll(PageRequests.of(page, size, Sort.by("id"))).map(BusResponse::from));
    }

    @Transactional
    public BusResponse update(Long id, BusUpdateRequest request) {
        if (request.name().trim().length() < 2) throw ApiException.badRequest("Bus name must be at least 2 characters.");
        BusEntity bus = buses.findById(id).orElseThrow(() -> ApiException.notFound("Bus not found."));
        String number = request.busNumber().trim().toUpperCase();
        if (!bus.getBusNumber().equalsIgnoreCase(number) && buses.existsByBusNumberIgnoreCaseAndIdNot(number, id)) {
            throw ApiException.conflict("Bus number already exists.");
        }
        bus.setName(request.name().trim());
        bus.setBusNumber(number);
        bus.setType(request.type());
        seats.findByBus_IdOrderBySeatNumber(id).forEach(seat -> seat.setSeatType(request.type().seatType()));
        return BusResponse.from(bus);
    }

    @Transactional
    public void delete(Long id) {
        BusEntity bus = buses.findById(id).orElseThrow(() -> ApiException.notFound("Bus not found."));
        if (trips.existsByBus_Id(id)) throw ApiException.conflict("Bus has trips and cannot be deleted.");
        seats.deleteAll(seats.findByBus_IdOrderBySeatNumber(bus.getId()));
        buses.delete(bus);
    }

    private Set<String> validateLadiesSeats(List<String> labels, int total) {
        Set<String> result = labels == null ? Set.of() : new HashSet<>(labels);
        if (labels != null && result.size() != labels.size()) throw ApiException.badRequest("Ladies-only seat numbers must be unique.");
        for (String label : result) {
            boolean valid = false;
            for (int i = 0; i < total; i++) valid |= seatNumber(i).equals(label);
            if (!valid) throw ApiException.badRequest("Ladies-only seats must be generated seat numbers.");
        }
        return result;
    }

    private String seatNumber(int index) {
        return String.valueOf((char) ('A' + index / 4)) + (index % 4 + 1);
    }
}
