package com.example.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.example.demo.services.DemoTripDataInitializer;
import com.example.demo.services.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "seatsync.demo-data.enabled=true")
class DemoTripDataInitializerTest {
    @Autowired private TripService trips;
    @Autowired private DemoTripDataInitializer initializer;

    @Test
    void seedsScreenshotRouteAndDoesNotDuplicateOnRestart() {
        LocalDate date = LocalDate.now().plusDays(1);
        var result = trips.search("Kakinada", "Visakhapatnam", date, List.of(),
            "departure", 0, 10);
        assertEquals(1, result.totalElements());
        assertEquals("Orange Travels", result.content().get(0).busName());
        assertEquals(40, result.content().get(0).availableSeats());
        assertEquals(new BigDecimal("650.00"), result.content().get(0).price());

        assertEquals(1, trips.search("Hyderabad", "Vijayawada", date, List.of(),
            "departure", 0, 10).totalElements());
        var kakinadaHyderabad = trips.search("Kakinada", "Hyderabad", date, List.of(),
            "departure", 0, 10);
        assertEquals(1, kakinadaHyderabad.totalElements());
        assertEquals("Aditya Travels", kakinadaHyderabad.content().get(0).busName());
        initializer.run();
        assertEquals(1, trips.search("Kakinada", "Visakhapatnam", date, List.of(),
            "departure", 0, 10).totalElements());
    }
}
