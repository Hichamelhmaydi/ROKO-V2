package com.example.roko.mapper;

import com.example.roko.dto.response.VoyageurDTO;
import com.example.roko.entity.Voyageurs;
import com.example.roko.enums.CompteStatus;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VoyageurMapper {

    public VoyageurDTO toDTO(Voyageurs voyageur) {
        if (voyageur == null) return null;
        VoyageurDTO dto = new VoyageurDTO();
        dto.setId(voyageur.getId());
        dto.setNom(voyageur.getNom());
        dto.setPrenom(voyageur.getPrenom());
        dto.setEmail(voyageur.getEmail());
        dto.setTelephone(voyageur.getTelephone());
        dto.setStatus(voyageur.getStatus() != null ? voyageur.getStatus().name() : null);
        dto.setIdNational(voyageur.getIdNational());
        dto.setDateExpiration(voyageur.getDateExpiration());
        return dto;
    }

    public Voyageurs toEntity(VoyageurDTO dto) {
        if (dto == null) return null;
        Voyageurs voyageur = new Voyageurs();
        voyageur.setId(dto.getId());
        voyageur.setNom(dto.getNom());
        voyageur.setPrenom(dto.getPrenom());
        voyageur.setEmail(dto.getEmail());
        voyageur.setTelephone(dto.getTelephone());
        voyageur.setIdNational(dto.getIdNational());
        voyageur.setDateExpiration(dto.getDateExpiration());
        if (dto.getStatus() != null) {
            voyageur.setStatus(CompteStatus.valueOf(dto.getStatus()));
        }
        return voyageur;
    }

    public List<VoyageurDTO> toDTOList(List<Voyageurs> voyageurs) {
        if (voyageurs == null) return new ArrayList<>();
        return voyageurs.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<Voyageurs> toEntityList(List<VoyageurDTO> dtos) {
        if (dtos == null) return new ArrayList<>();
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}