package com.example.demo;

import java.time.LocalDate;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.example.demo.model.BusEntity;
import com.example.demo.model.BusType;
import com.example.demo.model.RouteEntity;
import com.example.demo.model.Role;
import com.example.demo.model.SeatEntity;
import com.example.demo.model.SeatHoldEntity;
import com.example.demo.model.TripEntity;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.BusRepository;
import com.example.demo.repository.RouteRepository;
import com.example.demo.repository.SeatHoldRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
class TripSearchIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired private BusRepository buses;
    @Autowired private RouteRepository routes;
    @Autowired private TripRepository trips;
    @Autowired private UserRepository users;
    @Autowired private SeatRepository seats;
    @Autowired private SeatHoldRepository seatHolds;
    @Autowired private JwtService jwt;
    @Autowired private ObjectMapper mapper;
    private MockMvc http;

    @BeforeEach
    void setUp() {
        http = MockMvcBuilders.webAppContextSetup(context)
            .addFilters(context.getBean(FilterChainProxy.class)).build();
    }

    @Test
    void searchReturnsPaginatedTripDetails() throws Exception {
        String suffix = Long.toString(System.nanoTime());
        BusEntity bus = buses.save(new BusEntity("Test Bus", "TEST" + suffix, BusType.AC_SLEEPER, 40));
        RouteEntity route = routes.save(new RouteEntity("Origin" + suffix, "Destination" + suffix));
        LocalDate date = LocalDate.now().plusDays(1);
        trips.save(new TripEntity(bus, route, date.atTime(20, 0), date.atTime(23, 0), new java.math.BigDecimal("125.50")));

        var result = http.perform(get("/trips/search")
                .param("from", route.getSource().toUpperCase())
                .param("to", route.getDestination().toUpperCase())
                .param("date", date.toString())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();
        var body = mapper.readTree(result.getResponse().getContentAsString());
        assertEquals(1, body.path("totalElements").asInt());
        assertEquals(40, body.path("content").get(0).path("availableSeats").asInt());
        assertEquals(route.getSource(), body.path("content").get(0).path("source").asText());
    }

    @Test
    void adminReportsRequireAdminJwtAndSupportUnfilteredQueries() throws Exception {
        UserEntity admin = new UserEntity("Test Admin", "admin-" + System.nanoTime() + "@example.com", "hashed");
        admin.setRole(Role.ADMIN);
        admin = users.save(admin);
        String token = jwt.issue(admin).token();
        http.perform(get("/admin/stats").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
        http.perform(get("/admin/bookings").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
        http.perform(get("/admin/stats")).andExpect(status().isUnauthorized());
    }

    @Test
    void seatMapAndHoldsUseDatabaseAndExpiredHoldsCanBeTakenOver() throws Exception {
        String suffix = Long.toString(System.nanoTime());
        BusEntity bus = buses.save(new BusEntity("Hold Test Bus", "HOLD" + suffix, BusType.AC_SEATER, 1));
        RouteEntity route = routes.save(new RouteEntity("HoldOrigin" + suffix, "HoldDestination" + suffix));
        LocalDate date = LocalDate.now().plusDays(1);
        TripEntity trip = trips.save(new TripEntity(bus, route, date.atTime(20, 0), date.atTime(23, 0),
            new BigDecimal("125.00")));
        SeatEntity seat = seats.save(new SeatEntity(bus, "1A", "STANDARD", false));
        UserEntity first = users.save(new UserEntity("Hold Owner", "hold-" + suffix + "@example.com", "hashed"));
        UserEntity second = users.save(new UserEntity("Second Owner", "second-" + suffix + "@example.com", "hashed"));
        String firstToken = jwt.issue(first).token();
        String secondToken = jwt.issue(second).token();
        String request = mapper.writeValueAsString(Map.of("seatIds", List.of(seat.getId())));

        var emptyMap = http.perform(get("/trips/" + trip.getId() + "/seats"))
            .andExpect(status().isOk()).andReturn();
        assertEquals("AVAILABLE", mapper.readTree(emptyMap.getResponse().getContentAsString())
            .path("seats").get(0).path("status").asText());
        var createdHold = http.perform(post("/trips/" + trip.getId() + "/hold").header("Authorization", "Bearer " + firstToken)
                .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk()).andReturn();
        assertEquals(300, mapper.readTree(createdHold.getResponse().getContentAsString()).path("ttlSeconds").asInt());
        var heldMap = http.perform(get("/trips/" + trip.getId() + "/seats")
                .header("Authorization", "Bearer " + firstToken))
            .andExpect(status().isOk()).andReturn();
        var heldSeat = mapper.readTree(heldMap.getResponse().getContentAsString()).path("seats").get(0);
        assertEquals("HELD", heldSeat.path("status").asText());
        assertTrue(heldSeat.path("heldByMe").asBoolean());
        http.perform(post("/trips/" + trip.getId() + "/hold").header("Authorization", "Bearer " + secondToken)
                .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isConflict());

        SeatHoldEntity hold = seatHolds.findAll().stream()
            .filter(item -> item.getTrip().getId().equals(trip.getId())).findFirst().orElseThrow();
        hold.renew(first, Instant.now().minusSeconds(1));
        seatHolds.saveAndFlush(hold);
        http.perform(post("/trips/" + trip.getId() + "/hold").header("Authorization", "Bearer " + secondToken)
                .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk());
        var replacementMap = http.perform(get("/trips/" + trip.getId() + "/seats")
                .header("Authorization", "Bearer " + secondToken))
            .andExpect(status().isOk()).andReturn();
        assertTrue(mapper.readTree(replacementMap.getResponse().getContentAsString())
            .path("seats").get(0).path("heldByMe").asBoolean());
    }
}
