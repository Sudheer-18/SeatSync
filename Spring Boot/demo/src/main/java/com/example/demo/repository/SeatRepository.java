package com.example.demo.repository;

import java.util.List;
import com.example.demo.model.SeatEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<SeatEntity, Long> {
    List<SeatEntity> findByBus_IdOrderBySeatNumber(Long busId);
    List<SeatEntity> findByBus_IdAndIdIn(Long busId, List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SeatEntity s where s.bus.id = :busId and s.id in :ids order by s.id")
    List<SeatEntity> lockByBusAndIds(@Param("busId") Long busId, @Param("ids") List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SeatEntity s where s.bus.id = :busId order by s.id")
    List<SeatEntity> lockByBus(@Param("busId") Long busId);
}
