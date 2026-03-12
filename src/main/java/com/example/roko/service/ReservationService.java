package com.example.roko.service;

import com.example.roko.dto.ReservationDTO;
import com.example.roko.entity.*;
import com.example.roko.enums.ReservationStatut;
import com.example.roko.exception.BusinessException;
import com.example.roko.exception.ResourceNotFoundException;
import com.example.roko.mapper.ReservationMapper;
import com.example.roko.repository.ActiviteRepository;
import com.example.roko.repository.ReservationRepository;
import com.example.roko.repository.UserRepository;
import com.example.roko.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VoyageRepository voyageRepository;
    private final UserRepository userRepository;
    private final ActiviteRepository activiteRepository;
    private final ReservationMapper reservationMapper;

    public ReservationDTO createReservation(ReservationDTO reservationDTO, Long userId) {
        log.info("Création d'une réservation pour l'utilisateur {} et le voyage {}",
                userId, reservationDTO.getVoyageId());

        Voyageurs voyageur = (Voyageurs) userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouvé avec l'ID: " + userId));


        Voyages voyage = voyageRepository.findById(reservationDTO.getVoyageId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Voyage non trouvé avec l'ID: " + reservationDTO.getVoyageId()));

        if (!"DISPONIBLE".equals(voyage.getStatut().toString())) {
            throw new BusinessException("Ce voyage n'est plus disponible pour la réservation");
        }

        Reservations reservation = new Reservations();
        reservation.setVoyageur(voyageur);
        reservation.setVoyage(voyage);
        reservation.setNombrePersonnes(reservationDTO.getNombrePersonnes());
        reservation.setCommentaire(reservationDTO.getCommentaire());
        reservation.setStatut(ReservationStatut.EN_ATTENTE);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setPaiementEffectue(false);


        BigDecimal prixBase = BigDecimal.valueOf(2000.00);
        reservation.setPrixBase(prixBase);

        BigDecimal prixActivites = BigDecimal.ZERO;
        if (reservationDTO.getActivitesOptionnellesIds() != null &&
                !reservationDTO.getActivitesOptionnellesIds().isEmpty()) {

            Set<Activites> activitesSelectionnees = new HashSet<>();
            for (Long activiteId : reservationDTO.getActivitesOptionnellesIds()) {
                Activites activite = activiteRepository.findById(activiteId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Activité non trouvée avec l'ID: " + activiteId));

                if (!activite.getVoyage().getId().equals(voyage.getId())) {
                    throw new BusinessException(
                            "L'activité " + activite.getNom() + " n'appartient pas à ce voyage");
                }

                activitesSelectionnees.add(activite);
                prixActivites = prixActivites.add(BigDecimal.valueOf(100.00)); // Prix fictif
            }
            reservation.setActivites(activitesSelectionnees);
        }

        reservation.setPrixActivites(prixActivites);


        Reservations savedReservation = reservationRepository.save(reservation);
        log.info("Réservation créée avec succès. ID: {}", savedReservation.getId());

        return reservationMapper.toDTO(savedReservation);
    }


    @Transactional(readOnly = true)
    public Page<ReservationDTO> getAllReservations(Pageable pageable) {
        log.info("Récupération de toutes les réservations (page {})", pageable.getPageNumber());
        Page<Reservations> reservations = reservationRepository.findAll(pageable);
        return reservations.map(reservationMapper::toDTO);
    }


    @Transactional(readOnly = true)
    public ReservationDTO getReservationById(Long id, Long userId, boolean isAdmin) {
        log.info("Récupération de la réservation ID: {}", id);

        Reservations reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation non trouvée avec l'ID: " + id));

        if (!isAdmin && !reservation.getVoyageur().getId().equals(userId)) {
            throw new BusinessException("Vous n'avez pas accès à cette réservation");
        }

        return reservationMapper.toDTO(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByUser(Long userId) {
        log.info("Récupération des réservations de l'utilisateur {}", userId);
        List<Reservations> reservations = reservationRepository.findByUserId(userId);
        return reservationMapper.toDTOList(reservations);
    }


    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByVoyage(Long voyageId) {
        log.info("Récupération des réservations du voyage {}", voyageId);

        if (!voyageRepository.existsById(voyageId)) {
            throw new ResourceNotFoundException("Voyage non trouvé avec l'ID: " + voyageId);
        }

        List<Reservations> reservations = reservationRepository.findByVoyageId(voyageId);
        return reservationMapper.toDTOList(reservations);
    }


    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByStatut(ReservationStatut statut) {
        log.info("Récupération des réservations avec le statut {}", statut);
        List<Reservations> reservations = reservationRepository.findByStatut(statut);
        return reservationMapper.toDTOList(reservations);
    }


    public ReservationDTO confirmerReservation(Long id) {
        log.info("Confirmation de la réservation ID: {}", id);

        Reservations reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation non trouvée avec l'ID: " + id));

        if (reservation.getStatut() != ReservationStatut.EN_ATTENTE &&
                reservation.getStatut() != ReservationStatut.EN_ATTENTE_PAIEMENT) {
            throw new BusinessException(
                    "Seules les réservations en attente peuvent être confirmées");
        }

        reservation.setStatut(ReservationStatut.CONFIRMEE);
        reservation.setDateConfirmation(LocalDateTime.now());

        Reservations updatedReservation = reservationRepository.save(reservation);
        log.info("Réservation confirmée avec succès. ID: {}", id);

        return reservationMapper.toDTO(updatedReservation);
    }

    public ReservationDTO annulerReservation(Long id, String motif, Long userId, boolean isAdmin) {
        log.info("Annulation de la réservation ID: {} par l'utilisateur {}", id, userId);

        Reservations reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation non trouvée avec l'ID: " + id));

        if (!isAdmin && !reservation.getVoyageur().getId().equals(userId)) {
            throw new BusinessException("Vous n'avez pas le droit d'annuler cette réservation");
        }

        if (reservation.getStatut() == ReservationStatut.ANNULEE) {
            throw new BusinessException("Cette réservation est déjà annulée");
        }

        if (reservation.getStatut() == ReservationStatut.COMPLETEE) {
            throw new BusinessException("Impossible d'annuler une réservation complétée");
        }

        reservation.setStatut(ReservationStatut.ANNULEE);
        reservation.setDateAnnulation(LocalDateTime.now());
        reservation.setMotifAnnulation(motif);

        Reservations updatedReservation = reservationRepository.save(reservation);
        log.info("Réservation annulée avec succès. ID: {}", id);

        return reservationMapper.toDTO(updatedReservation);
    }


    public ReservationDTO completerReservation(Long id) {
        log.info("Complétion de la réservation ID: {}", id);

        Reservations reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation non trouvée avec l'ID: " + id));

        if (reservation.getStatut() != ReservationStatut.CONFIRMEE) {
            throw new BusinessException(
                    "Seules les réservations confirmées peuvent être complétées");
        }

        reservation.setStatut(ReservationStatut.COMPLETEE);
        reservation.setDateCompletion(LocalDateTime.now());

        Reservations updatedReservation = reservationRepository.save(reservation);
        log.info("Réservation complétée avec succès. ID: {}", id);

        return reservationMapper.toDTO(updatedReservation);
    }

    public ReservationDTO updateReservation(Long id, ReservationDTO reservationDTO, Long userId, boolean isAdmin) {
        log.info("Mise à jour de la réservation ID: {}", id);

        Reservations reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation non trouvée avec l'ID: " + id));

        if (!isAdmin && !reservation.getVoyageur().getId().equals(userId)) {
            throw new BusinessException("Vous n'avez pas le droit de modifier cette réservation");
        }

        if (reservation.getStatut() != ReservationStatut.EN_ATTENTE) {
            throw new BusinessException(
                    "Seules les réservations en attente peuvent être modifiées");
        }

        reservationMapper.updateEntityFromDTO(reservationDTO, reservation);

        Reservations updatedReservation = reservationRepository.save(reservation);
        log.info("Réservation mise à jour avec succès. ID: {}", id);

        return reservationMapper.toDTO(updatedReservation);
    }


    public void deleteReservation(Long id) {
        log.info("Suppression de la réservation ID: {}", id);

        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Réservation non trouvée avec l'ID: " + id);
        }

        reservationRepository.deleteById(id);
        log.info("Réservation supprimée avec succès. ID: {}", id);
    }


    @Transactional(readOnly = true)
    public long countReservationsByStatut(ReservationStatut statut) {
        return reservationRepository.countByStatut(statut);
    }


    @Transactional(readOnly = true)
    public long countReservationsByUser(Long userId) {
        return reservationRepository.countByUserId(userId);
    }


    @Transactional(readOnly = true)
    public List<ReservationDTO> getRecentReservations() {
        LocalDateTime dateDebut = LocalDateTime.now().minusHours(24);
        List<Reservations> reservations = reservationRepository.findRecentReservations(dateDebut);
        return reservationMapper.toDTOList(reservations);
    }


    public ReservationDTO marquerCommePaye(Long id) {
        log.info("Marquage de la réservation ID: {} comme payée", id);

        Reservations reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation non trouvée avec l'ID: " + id));

        reservation.setPaiementEffectue(true);
        reservation.setDatePaiement(LocalDateTime.now());
        reservation.setStatut(ReservationStatut.PAYEE);

        Reservations updatedReservation = reservationRepository.save(reservation);
        log.info("Réservation marquée comme payée. ID: {}", id);

        return reservationMapper.toDTO(updatedReservation);
    }
}