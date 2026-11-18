package com.example.demo.repository;

import java.util.Optional;
import java.time.Instant;
import java.util.List;
import com.example.demo.model.BookingEntity;
import com.example.demo.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    Optional<BookingEntity> findByIdempotencyKey(String key);
    @EntityGraph(attributePaths = {"trip", "trip.bus", "trip.route"})
    Optional<BookingEntity> findByIdAndUser_Id(Long id, Long userId);
    @EntityGraph(attributePaths = {"trip", "trip.bus", "trip.route"})
    Page<BookingEntity> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    @EntityGraph(attributePaths = {"trip", "trip.bus", "trip.route"})
    @Query(value = "select b from BookingEntity b where b.user.id = :userId and (:status is null or b.status = :status)",
        countQuery = "select count(b) from BookingEntity b where b.user.id = :userId and (:status is null or b.status = :status)")
    Page<BookingEntity> findMine(@Param("userId") Long userId, @Param("status") BookingStatus status, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BookingEntity b where b.id = :id and b.user.id = :userId")
    Optional<BookingEntity> lockOwned(@Param("id") Long id, @Param("userId") Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BookingEntity b where b.id = :id")
    Optional<BookingEntity> lockById(@Param("id") Long id);
    @EntityGraph(attributePaths = {"user", "trip", "trip.bus", "trip.route"})
    @Query(value = """
        select b from BookingEntity b where (:tripId is null or b.trip.id = :tripId)
          and (:status is null or b.status = :status)
          and (:fromTime is null or b.createdAt >= :fromTime)
          and (:toTime is null or b.createdAt < :toTime)
        """,
        countQuery = """
        select count(b) from BookingEntity b where (:tripId is null or b.trip.id = :tripId)
          and (:status is null or b.status = :status)
          and (:fromTime is null or b.createdAt >= :fromTime)
          and (:toTime is null or b.createdAt < :toTime)
        """)
    Page<BookingEntity> searchAdmin(@Param("tripId") Long tripId, @Param("status") BookingStatus status,
        @Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime, Pageable pageable);
    boolean existsByTrip_Id(Long tripId);
    long countByTrip_Id(Long tripId);
    long countByStatus(BookingStatus status);
    @Query("select coalesce(sum(b.totalAmount), 0) from BookingEntity b where b.status = :status")
    java.math.BigDecimal revenueByStatus(BookingStatus status);
    @Query("select count(b) from BookingEntity b where (:fromTime is null or b.createdAt >= :fromTime) and (:toTime is null or b.createdAt < :toTime)")
    long countCreatedBetween(@Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime);
    @Query("select count(b) from BookingEntity b where b.status = :status and (:fromTime is null or b.createdAt >= :fromTime) and (:toTime is null or b.createdAt < :toTime)")
    long countByStatusAndCreatedBetween(@Param("status") BookingStatus status, @Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime);
    @Query("select coalesce(sum(b.totalAmount), 0) from BookingEntity b where b.status = :status and (:fromTime is null or b.createdAt >= :fromTime) and (:toTime is null or b.createdAt < :toTime)")
    java.math.BigDecimal revenueByStatusAndCreatedBetween(@Param("status") BookingStatus status, @Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime);
    @Query("select r.source, r.destination, count(b) from BookingEntity b join b.trip t join t.route r where b.status = :status and (:fromTime is null or b.createdAt >= :fromTime) and (:toTime is null or b.createdAt < :toTime) group by r.id, r.source, r.destination order by count(b) desc")
    List<Object[]> topRoutes(@Param("status") BookingStatus status, @Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime, Pageable pageable);
}
