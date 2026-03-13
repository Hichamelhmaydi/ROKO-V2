package com.example.roko.mapper;

import com.example.roko.dto.AvisDTO;
import com.example.roko.entity.Avis;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AvisMapper {

    public AvisDTO toDTO(Avis avis) {
        AvisDTO dto = new AvisDTO();
        dto.setId(avis.getId());
        dto.setVoyageId(avis.getVoyage().getId());
        dto.setVoyageurId(avis.getVoyageur().getId());
        dto.setVoyageNom(avis.getVoyage().getNom());
        dto.setVoyageurNom(avis.getVoyageur().getNom());
        dto.setVoyageurPrenom(avis.getVoyageur().getPrenom());
        dto.setNote(avis.getNote());
        dto.setCommentaire(avis.getCommentaire());
        dto.setStatut(avis.getStatut());
        dto.setDateCreation(avis.getDateCreation());
        dto.setDateModeration(avis.getDateModeration());
        return dto;
    }

    public List<AvisDTO> toDTOList(List<Avis> avisList) {
        return avisList.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
