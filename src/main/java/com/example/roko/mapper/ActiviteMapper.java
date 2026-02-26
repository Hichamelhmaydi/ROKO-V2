package com.example.roko.mapper;

import com.example.roko.dto.ActiviteDTO;
import com.example.roko.entity.Activites;
import com.example.roko.entity.Voyages;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActiviteMapper {

    public ActiviteDTO toDTO(Activites activite) {
        if (activite == null) {
            return null;
        }

        ActiviteDTO dto = new ActiviteDTO();
        dto.setId(activite.getId());
        dto.setNom(activite.getNom());
        dto.setDescription(activite.getDescription());
        dto.setVoyageId(activite.getVoyage() != null ? activite.getVoyage().getId() : null);

        if (activite.getVoyage() != null) {
            dto.setVoyageNom(activite.getVoyage().getDestination());
        }

        if (activite.getReservations() != null) {
            dto.setNombreReservations(activite.getReservations().size());
        }

        return dto;
    }


    public Activites toEntity(ActiviteDTO dto) {
        if (dto == null) {
            return null;
        }

        Activites activite = new Activites();
        activite.setId(dto.getId());
        activite.setNom(dto.getNom());
        activite.setDescription(dto.getDescription());

        if (dto.getVoyageId() != null) {
            Voyages voyage = new Voyages();
            voyage.setId(dto.getVoyageId());
            activite.setVoyage(voyage);
        }

        return activite;
    }

    public List<ActiviteDTO> toDTOList(List<Activites> activites) {
        if (activites == null) {
            return new ArrayList<>();
        }
        return activites.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public List<Activites> toEntityList(List<ActiviteDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(ActiviteDTO dto, Activites activite) {
        if (dto == null || activite == null) {
            return;
        }

        if (dto.getNom() != null) {
            activite.setNom(dto.getNom());
        }
        if (dto.getDescription() != null) {
            activite.setDescription(dto.getDescription());
        }
    }
}