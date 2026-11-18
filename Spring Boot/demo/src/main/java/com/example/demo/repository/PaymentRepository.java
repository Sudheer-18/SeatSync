package com.example.demo.repository;

import java.util.Optional;
import java.time.Instant;
import java.util.List;
import com.example.demo.model.PaymentEntity;
import com.example.demo.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByBooking_Id(Long bookingId);
    boolean existsByPaymentReference(String paymentReference);
    @Query("select p.booking.id from PaymentEntity p where p.status = :status and p.expiresAt <= :now")
    List<Long> findExpiredBookingIds(@Param("status") PaymentStatus status, @Param("now") Instant now);
}
