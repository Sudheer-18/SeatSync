package com.example.demo.repository;

import java.time.Instant;
import java.util.List;
import com.example.demo.model.SeatHoldEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatHoldRepository extends JpaRepository<SeatHoldEntity, Long> {
    @Query("select h from SeatHoldEntity h where h.trip.id = :tripId and h.expiresAt > :now")
    List<SeatHoldEntity> findActive(@Param("tripId") Long tripId, @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from SeatHoldEntity h where h.trip.id = :tripId and h.expiresAt > :now")
    List<SeatHoldEntity> lockActive(@Param("tripId") Long tripId, @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from SeatHoldEntity h where h.trip.id = :tripId and h.seat.id in :seatIds")
    List<SeatHoldEntity> lockByTripAndSeats(
        @Param("tripId") Long tripId, @Param("seatIds") List<Long> seatIds);

    long deleteByTrip_IdAndSeat_IdInAndUser_Id(Long tripId, List<Long> seatIds, Long userId);
    void deleteByTrip_Id(Long tripId);
}
