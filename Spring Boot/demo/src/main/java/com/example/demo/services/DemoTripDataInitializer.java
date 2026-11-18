package com.example.demo.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import com.example.demo.model.BusEntity;
import com.example.demo.model.BusType;
import com.example.demo.model.RouteEntity;
import com.example.demo.model.SeatEntity;
import com.example.demo.model.TripEntity;
import com.example.demo.repository.BusRepository;
import com.example.demo.repository.RouteRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.TripRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "seatsync.demo-data.enabled", havingValue = "true")
public class DemoTripDataInitializer implements CommandLineRunner {
    private static final String BUS_PREFIX = "SS-DEMO-";
    private static final int SEAT_COUNT = 40;
    private static final int SERVICE_DAYS = 30;
    private static final List<TripSeed> SEEDS = List.of(
        new TripSeed("Orange Travels", "Kakinada", "Visakhapatnam", "06:00", "10:30", "650"),
        new TripSeed("Sri Krishna Travels", "Hyderabad", "Vijayawada", "21:00", "04:00", "850"),
        new TripSeed("VRL Travels", "Bengaluru", "Hyderabad", "20:00", "06:00", "1200"),
        new TripSeed("Kaveri Travels", "Chennai", "Bengaluru", "22:00", "05:30", "950"),
        new TripSeed("APSRTC Garuda", "Vijayawada", "Tirupati", "07:00", "15:00", "750"),
        new TripSeed("SRS Travels", "Hyderabad", "Bengaluru", "21:30", "07:00", "1400"),
        new TripSeed("Morning Star", "Rajahmundry", "Vijayawada", "05:30", "09:00", "450"),
        new TripSeed("IntrCity SmartBus", "Pune", "Mumbai", "07:00", "11:00", "550"),
        new TripSeed("Jabbar Travels", "Visakhapatnam", "Hyderabad", "18:00", "08:00", "1100"),
        new TripSeed("Aditya Travels", "Kakinada", "Hyderabad", "20:00", "07:00", "1000")
    );

    private final BusRepository buses;
    private final RouteRepository routes;
    private final SeatRepository seats;
    private final TripRepository trips;

    public DemoTripDataInitializer(BusRepository buses, RouteRepository routes,
        SeatRepository seats, TripRepository trips) {
        this.buses = buses;
        this.routes = routes;
        this.seats = seats;
        this.trips = trips;
    }

    @Override
    @Transactional
    public void run(String... args) {
        LocalDate firstDate = LocalDate.now().plusDays(1);
        for (int index = 0; index < SEEDS.size(); index++) {
            TripSeed seed = SEEDS.get(index);
            BusEntity bus = createBus(index, seed.busName());
            RouteEntity route = routes.findBySourceIgnoreCaseAndDestinationIgnoreCase(
                seed.source(), seed.destination()).orElseGet(
                    () -> routes.save(new RouteEntity(seed.source(), seed.destination())));
            createTrips(seed, bus, route, firstDate);
        }
    }

    private BusEntity createBus(int index, String name) {
        String number = BUS_PREFIX + String.format("%03d", index + 1);
        BusEntity existing = buses.findByBusNumberIgnoreCase(number).orElse(null);
        if (existing != null) {
            if (!existing.getName().equals(name) || existing.getType() != BusType.AC_SEATER
                || existing.getTotalSeats() != SEAT_COUNT) {
                throw new IllegalStateException("Reserved demo bus number has unexpected data: " + number);
            }
            return existing;
        }
        BusEntity bus = buses.saveAndFlush(new BusEntity(name, number, BusType.AC_SEATER, SEAT_COUNT));
        List<SeatEntity> generated = new ArrayList<>();
        for (int seat = 0; seat < SEAT_COUNT; seat++) {
            String label = (char) ('A' + seat / 4) + Integer.toString(seat % 4 + 1);
            generated.add(new SeatEntity(bus, label, bus.getType().seatType(), false));
        }
        seats.saveAll(generated);
        return bus;
    }

    private void createTrips(TripSeed seed, BusEntity bus, RouteEntity route, LocalDate firstDate) {
        for (int day = 0; day < SERVICE_DAYS; day++) {
            LocalDate date = firstDate.plusDays(day);
            LocalDateTime departure = date.atTime(LocalTime.parse(seed.departure()));
            LocalDateTime arrival = date.atTime(LocalTime.parse(seed.arrival()));
            if (!arrival.isAfter(departure)) arrival = arrival.plusDays(1);
            if (!trips.existsByBus_IdAndRoute_IdAndDepartureTime(bus.getId(), route.getId(), departure)) {
                trips.save(new TripEntity(bus, route, departure, arrival, new BigDecimal(seed.price())));
            }
        }
    }

    private record TripSeed(String busName, String source, String destination,
        String departure, String arrival, String price) { }
}
