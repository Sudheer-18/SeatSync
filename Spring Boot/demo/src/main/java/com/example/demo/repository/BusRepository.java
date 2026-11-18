package com.example.demo.repository;

import java.util.Optional;
import com.example.demo.model.BusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface BusRepository extends JpaRepository<BusEntity, Long> {
    boolean existsByBusNumberIgnoreCase(String busNumber);
    boolean existsByBusNumberIgnoreCaseAndIdNot(String busNumber, Long id);
    Optional<BusEntity> findByBusNumberIgnoreCase(String busNumber);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BusEntity b where b.id = :id")
    Optional<BusEntity> lockById(@Param("id") Long id);
    Page<BusEntity> findAll(Pageable pageable);
}
