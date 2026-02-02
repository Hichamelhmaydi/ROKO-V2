package com.example.roko.mapper;

import com.example.roko.dto.ReservationDTO;
import com.example.roko.entity.Reservations;
import com.example.roko.entity.Voyages;
import com.example.roko.entity.Voyageurs;
import com.example.roko.enums.ReservationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationMapper {

    public ReservationDTO toDTO(Reservations reservation) {
        if (reservation == null) {
            return null;
        }

        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setVoyageurId(reservation.getVoyageur() != null ? reservation.getVoyageur().getId() : null);
        dto.setVoyageId(reservation.getVoyage() != null ? reservation.getVoyage().getId() : null);
        dto.setStatut(reservation.getStatut() != null ? reservation.getStatut().name() : null);
        dto.setDateReservation(reservation.getDateReservation());
        dto.setPrixTotal(reservation.getPrixTotal());
        dto.setNotes(reservation.getNotes());

        return dto;
    }

    public Reservations toEntity(ReservationDTO dto) {
        if (dto == null) {
            return null;
        }

        Reservations reservation = new Reservations();
        reservation.setId(dto.getId());
        reservation.setDateReservation(dto.getDateReservation());
        reservation.setPrixTotal(dto.getPrixTotal());
        reservation.setNotes(dto.getNotes());

        // Conversion du statut
        if (dto.getStatut() != null) {
            reservation.setStatut(ReservationStatus.valueOf(dto.getStatut()));
        }

        // Le voyageur et le voyage seront set séparément dans le service
        if (dto.getVoyageurId() != null) {
            Voyageurs voyageur = new Voyageurs();
            voyageur.setId(dto.getVoyageurId());
            reservation.setVoyageur(voyageur);
        }

        if (dto.getVoyageId() != null) {
            Voyages voyage = new Voyages();
            voyage.setId(dto.getVoyageId());
            reservation.setVoyage(voyage);
        }

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

    public List<Reservations> toEntityList(List<ReservationDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}