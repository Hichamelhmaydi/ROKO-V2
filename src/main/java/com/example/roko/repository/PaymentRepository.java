package com.example.roko.repository;

import com.example.roko.entity.Payment;
import com.example.roko.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByStripeSessionId(String stripeSessionId);

    boolean existsByStripeSessionId(String stripeSessionId);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId ORDER BY p.dateCreation DESC")
    List<Payment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Payment p WHERE p.reservation.id = :reservationId")
    Optional<Payment> findByReservationId(@Param("reservationId") Long reservationId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.dateCreation DESC")
    List<Payment> findByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.status = 'EN_ATTENTE' AND p.dateCreation < :dateLimit")
    List<Payment> findExpiredPendingPayments(@Param("dateLimit") LocalDateTime dateLimit);

    @Query("SELECT p FROM Payment p WHERE p.status = 'REUSSI' AND p.datePaiement BETWEEN :debut AND :fin")
    List<Payment> findSuccessfulPaymentsBetween(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'REUSSI'")
    Double calculateTotalSuccessfulPayments();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'REUSSI' AND p.datePaiement BETWEEN :debut AND :fin")
    Double calculateTotalRevenueBetween(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.dateCreation >= :dateDebut ORDER BY p.dateCreation DESC")
    List<Payment> findRecentPayments(@Param("dateDebut") LocalDateTime dateDebut);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status = :status ORDER BY p.dateCreation DESC")
    List<Payment> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status);

    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.userId = :userId AND p.status = 'EN_ATTENTE'")
    boolean hasUserPendingPayment(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p FROM Payment p LEFT JOIN FETCH p.reservation WHERE p.id = :id")
    Optional<Payment> findByIdWithReservation(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.reservation.id = :reservationId")
    void deleteByReservationId(@Param("reservationId") Long reservationId);

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.reservation.id IN (:reservationIds)")
    void deleteByReservationIds(@Param("reservationIds") List<Long> reservationIds);
}
