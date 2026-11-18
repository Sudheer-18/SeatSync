package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.demo.model.BusType;
import com.example.demo.model.TripEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<TripEntity, Long> {
    @EntityGraph(attributePaths = {"bus", "route"})
    Page<TripEntity> findAll(Pageable pageable);
    @EntityGraph(attributePaths = {"bus", "route"})
    @Query(value = """
        select t from TripEntity t where (:start is null or t.departureTime >= :start)
          and (:end is null or t.departureTime < :end)
        """,
        countQuery = """
        select count(t) from TripEntity t where (:start is null or t.departureTime >= :start)
          and (:end is null or t.departureTime < :end)
        """)
    Page<TripEntity> findAdmin(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
    @Override
    @EntityGraph(attributePaths = {"bus", "route"})
    Optional<TripEntity> findById(Long id);
    boolean existsByBus_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
        Long busId, LocalDateTime arrival, LocalDateTime departure);
    boolean existsByBus_IdAndRoute_IdAndDepartureTime(Long busId, Long routeId, LocalDateTime departureTime);
    boolean existsByBus_IdAndIdNotAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
        Long busId, Long id, LocalDateTime arrival, LocalDateTime departure);
    boolean existsByBus_Id(Long busId);
    boolean existsByRoute_Id(Long routeId);
    boolean existsByRoute_IdAndDepartureTimeBetween(Long routeId, LocalDateTime from, LocalDateTime to);

    @Query(value = """
        select t from TripEntity t join fetch t.bus b join fetch t.route r
        where lower(r.source) = lower(:source) and lower(r.destination) = lower(:destination)
          and t.departureTime >= :start and t.departureTime < :end
          and (:hasTypes = false or b.type in :types)
        """,
        countQuery = """
        select count(t) from TripEntity t join t.bus b join t.route r
        where lower(r.source) = lower(:source) and lower(r.destination) = lower(:destination)
          and t.departureTime >= :start and t.departureTime < :end
          and (:hasTypes = false or b.type in :types)
        """)
    Page<TripEntity> search(
        @Param("source") String source,
        @Param("destination") String destination,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("hasTypes") boolean hasTypes,
        @Param("types") List<BusType> types,
        Pageable pageable);
}
