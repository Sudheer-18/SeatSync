package com.example.demo.repository;

import java.util.Optional;
import com.example.demo.model.RouteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<RouteEntity, Long> {
    boolean existsBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);
    boolean existsBySourceIgnoreCaseAndDestinationIgnoreCaseAndIdNot(String source, String destination, Long id);
    Optional<RouteEntity> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);
    Page<RouteEntity> findAll(Pageable pageable);
}
