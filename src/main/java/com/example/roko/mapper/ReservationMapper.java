package com.example.roko.mapper;

import com.example.roko.dto.ActiviteDTO;
import com.example.roko.dto.ReservationDTO;
import com.example.roko.entity.Activites;
import com.example.roko.entity.Reservations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class ReservationMapper {

    private final ActiviteMapper activiteMapper;

    public ReservationMapper(ActiviteMapper activiteMapper) {
        this.activiteMapper = activiteMapper;
    }


    public ReservationDTO toDTO(Reservations reservation) {
        if (reservation == null) {
            return null;
        }

        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setUserId(reservation.getVoyageur() != null ? reservation.getVoyageur().getId() : null);
        dto.setVoyageId(reservation.getVoyage() != null ? reservation.getVoyage().getId() : null);
        dto.setNombrePersonnes(reservation.getNombrePersonnes());
        dto.setStatut(reservation.getStatut());
        dto.setDateReservation(reservation.getDateReservation());
        dto.setDateConfirmation(reservation.getDateConfirmation());
        dto.setDateAnnulation(reservation.getDateAnnulation());
        dto.setDateCompletion(reservation.getDateCompletion());
        dto.setPrixBase(reservation.getPrixBase());
        dto.setPrixActivites(reservation.getPrixActivites());
        dto.setMontantTotal(reservation.getMontantTotal());
        dto.setCommentaire(reservation.getCommentaire());
        dto.setMotifAnnulation(reservation.getMotifAnnulation());
        dto.setPaiementEffectue(reservation.getPaiementEffectue());
        dto.setDatePaiement(reservation.getDatePaiement());

        if (reservation.getVoyage() != null) {
            dto.setVoyageNom(reservation.getVoyage().getNom());
            dto.setVoyageDestination(reservation.getVoyage().getDestination());
            dto.setVoyageDateDepart(reservation.getVoyage().getDateDepart());
            dto.setVoyageDateRetour(reservation.getVoyage().getDateRetour());
        }

        if (reservation.getVoyageur() != null) {
            dto.setUserNom(reservation.getVoyageur().getNom());
            dto.setUserPrenom(reservation.getVoyageur().getPrenom());
            dto.setUserEmail(reservation.getVoyageur().getEmail());
        }

        if (reservation.getActivites() != null && !reservation.getActivites().isEmpty()) {
            List<ActiviteDTO> activitesDTO = reservation.getActivites().stream()
                    .map(activiteMapper::toDTO)
                    .collect(Collectors.toList());
            dto.setActivites(activitesDTO);

            List<Long> activitesIds = reservation.getActivites().stream()
                    .map(Activites::getId)
                    .collect(Collectors.toList());
            dto.setActivitesOptionnellesIds(activitesIds);
        }

        return dto;
    }


    public Reservations toEntity(ReservationDTO dto) {
        if (dto == null) {
            return null;
        }

        Reservations reservation = new Reservations();
        reservation.setId(dto.getId());
        reservation.setNombrePersonnes(dto.getNombrePersonnes());
        reservation.setStatut(dto.getStatut());
        reservation.setDateReservation(dto.getDateReservation());
        reservation.setDateConfirmation(dto.getDateConfirmation());
        reservation.setDateAnnulation(dto.getDateAnnulation());
        reservation.setDateCompletion(dto.getDateCompletion());
        reservation.setPrixBase(dto.getPrixBase());
        reservation.setPrixActivites(dto.getPrixActivites());
        reservation.setMontantTotal(dto.getMontantTotal());
        reservation.setCommentaire(dto.getCommentaire());
        reservation.setMotifAnnulation(dto.getMotifAnnulation());
        reservation.setPaiementEffectue(dto.getPaiementEffectue());
        reservation.setDatePaiement(dto.getDatePaiement());

        return reservation;
    }


    public List<ReservationDTO> toDTOList(List<Reservations> reservations) {
        if (reservations == null) {
            return new ArrayList<>();
        }
        return reservations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public void updateEntityFromDTO(ReservationDTO dto, Reservations reservation) {
        if (dto == null || reservation == null) {
            return;
        }

        if (dto.getNombrePersonnes() != null) {
            reservation.setNombrePersonnes(dto.getNombrePersonnes());
        }
        if (dto.getCommentaire() != null) {
            reservation.setCommentaire(dto.getCommentaire());
        }
        if (dto.getStatut() != null) {
            reservation.setStatut(dto.getStatut());
        }
    }
}