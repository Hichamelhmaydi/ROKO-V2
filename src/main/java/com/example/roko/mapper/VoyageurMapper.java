package com.example.roko.mapper;

import com.example.roko.dto.response.VoyageurDTO;
import com.example.roko.entity.Voyageurs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VoyageurMapper {

    VoyageurDTO toDTO(Voyageurs voyageur);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "adresse", ignore = true)
    @Mapping(target = "dateInscription", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "bloque", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    Voyageurs toEntity(VoyageurDTO dto);

    List<VoyageurDTO> toDTOList(List<Voyageurs> voyageurs);

    List<Voyageurs> toEntityList(List<VoyageurDTO> dtos);
}
