package com.example.roko.mapper;

import com.example.roko.dto.response.VoyageDTO;
import com.example.roko.entity.Voyages;
import com.example.roko.enums.VoyageStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VoyageMapper {

    public VoyageDTO toDTO(Voyages voyage) {
        if (voyage == null) {
            return null;
        }

        VoyageDTO dto = new VoyageDTO();
        dto.setId(voyage.getId());
        dto.setNom(voyage.getNom());
        dto.setDescription(voyage.getDescription());
        dto.setCover(voyage.getCover());
        dto.setDestination(voyage.getDestination());
        dto.setDateDepart(voyage.getDateDepart());
        dto.setDateRetour(voyage.getDateRetour());
        dto.setStatut(voyage.getStatut() != null ? voyage.getStatut().name() : null);
        dto.setItineraire(voyage.getItineraire());
        dto.setPhotos(voyage.getPhotos() != null ? new ArrayList<>(voyage.getPhotos()) : new ArrayList<>());

        return dto;
    }

    public Voyages toEntity(VoyageDTO dto) {
        if (dto == null) {
            return null;
        }

        Voyages voyage = new Voyages();
        voyage.setId(dto.getId());
        voyage.setNom(dto.getNom());
        voyage.setDescription(dto.getDescription());
        voyage.setCover(dto.getCover());
        voyage.setDestination(dto.getDestination());
        voyage.setDateDepart(dto.getDateDepart());
        voyage.setDateRetour(dto.getDateRetour());
        voyage.setItineraire(dto.getItineraire());
        voyage.setPhotos(dto.getPhotos() != null ? new ArrayList<>(dto.getPhotos()) : new ArrayList<>());

        if (dto.getStatut() != null) {
            voyage.setStatut(VoyageStatus.valueOf(dto.getStatut()));
        }

        return voyage;
    }

    public List<VoyageDTO> toDTOList(List<Voyages> voyages) {
        if (voyages == null) {
            return new ArrayList<>();
        }
        return voyages.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<Voyages> toEntityList(List<VoyageDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}