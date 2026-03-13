package com.example.roko.repository;

import com.example.roko.entity.Reservations;
import com.example.roko.enums.ReservationStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservations, Long> {

    @Query("SELECT r FROM Reservations r WHERE r.voyageur.id = :userId ORDER BY r.dateReservation DESC")
    List<Reservations> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Reservations r WHERE r.voyageur.id = :userId ORDER BY r.dateReservation DESC")
    Page<Reservations> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM Reservations r WHERE r.voyage.id = :voyageId ORDER BY r.dateReservation DESC")
    List<Reservations> findByVoyageId(@Param("voyageId") Long voyageId);

    @Query("SELECT r FROM Reservations r WHERE r.statut = :statut ORDER BY r.dateReservation DESC")
    List<Reservations> findByStatut(@Param("statut") ReservationStatut statut);

    @Query("SELECT r FROM Reservations r WHERE r.statut = :statut ORDER BY r.dateReservation DESC")
    Page<Reservations> findByStatut(@Param("statut") ReservationStatut statut, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Reservations r "
            + "LEFT JOIN FETCH r.voyageur "
            + "LEFT JOIN FETCH r.voyage "
            + "LEFT JOIN FETCH r.activites "
            + "WHERE r.id = :id")
    Optional<Reservations> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Reservations r WHERE r.statut = 'EN_ATTENTE' ORDER BY r.dateReservation ASC")
    List<Reservations> findReservationsEnAttente();

    @Query("SELECT r FROM Reservations r WHERE r.statut = 'CONFIRMEE' ORDER BY r.dateReservation DESC")
    List<Reservations> findReservationsConfirmees();

    @Query("SELECT r FROM Reservations r WHERE r.statut = 'ANNULEE' ORDER BY r.dateAnnulation DESC")
    List<Reservations> findReservationsAnnulees();

    @Query("SELECT r FROM Reservations r WHERE r.statut = 'COMPLETEE' ORDER BY r.dateCompletion DESC")
    List<Reservations> findReservationsCompletees();

    @Query("SELECT r FROM Reservations r WHERE r.dateReservation BETWEEN :debut AND :fin ORDER BY r.dateReservation DESC")
    List<Reservations> findByDateReservationBetween(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(r) FROM Reservations r WHERE r.statut = :statut")
    long countByStatut(@Param("statut") ReservationStatut statut);

    @Query("SELECT COUNT(r) FROM Reservations r WHERE r.voyageur.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Reservations r WHERE r.voyage.id = :voyageId")
    long countByVoyageId(@Param("voyageId") Long voyageId);

    @Query("SELECT r FROM Reservations r WHERE r.paiementEffectue = false AND r.statut = 'EN_ATTENTE' ORDER BY r.dateReservation DESC")
    List<Reservations> findReservationsNonPayees();

    @Query("SELECT r FROM Reservations r WHERE r.voyageur.id = :userId AND r.statut = :statut ORDER BY r.dateReservation DESC")
    List<Reservations> findByUserIdAndStatut(
            @Param("userId") Long userId,
            @Param("statut") ReservationStatut statut);

    @Query("SELECT SUM(r.montantTotal) FROM Reservations r WHERE r.voyage.id = :voyageId AND r.statut = 'CONFIRMEE'")
    Double calculateTotalRevenueByVoyage(@Param("voyageId") Long voyageId);

    @Query("SELECT r FROM Reservations r WHERE r.dateReservation >= :dateDebut ORDER BY r.dateReservation DESC")
    List<Reservations> findRecentReservations(@Param("dateDebut") LocalDateTime dateDebut);

    @Query("SELECT COUNT(r) > 0 FROM Reservations r WHERE r.voyageur.id = :userId AND r.voyage.id = :voyageId")
    boolean existsByUserIdAndVoyageId(@Param("userId") Long userId, @Param("voyageId") Long voyageId);

    @Query("SELECT COUNT(r) > 0 FROM Reservations r WHERE r.voyageur.id = :userId AND r.voyage.id = :voyageId AND r.statut = :statut")
    boolean existsByUserIdAndVoyageIdAndStatut(
            @Param("userId") Long userId,
            @Param("voyageId") Long voyageId,
            @Param("statut") ReservationStatut statut);
}
