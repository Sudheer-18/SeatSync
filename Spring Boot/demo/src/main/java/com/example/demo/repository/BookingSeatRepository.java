package com.example.demo.repository;

import java.util.List;
import com.example.demo.model.BookingSeatEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingSeatRepository extends JpaRepository<BookingSeatEntity, Long> {
    List<BookingSeatEntity> findByTrip_Id(Long tripId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select bs from BookingSeatEntity bs where bs.trip.id = :tripId and bs.seat.id in :seatIds")
    List<BookingSeatEntity> lockByTripAndSeats(
        @Param("tripId") Long tripId, @Param("seatIds") List<Long> seatIds);
    List<BookingSeatEntity> findByBooking_IdOrderById(Long bookingId);
    boolean existsByTrip_IdAndSeat_Id(Long tripId, Long seatId);
    long countByTrip_Id(Long tripId);
    void deleteByBooking_Id(Long bookingId);
}
