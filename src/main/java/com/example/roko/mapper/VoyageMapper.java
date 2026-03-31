package com.example.roko.mapper;

import com.example.roko.dto.response.VoyageDTO;
import com.example.roko.entity.Voyages;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VoyageMapper {

    @Mapping(target = "statut", source = "statut")
    @Mapping(target = "prixInitial", expression = "java(resolvePrixInitial(voyage))")
    VoyageDTO toDTO(Voyages voyage);

    @Mapping(target = "statut", source = "statut")
    @Mapping(target = "activites", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    Voyages toEntity(VoyageDTO dto);

    List<VoyageDTO> toDTOList(List<Voyages> voyages);

    List<Voyages> toEntityList(List<VoyageDTO> dtos);

    default java.math.BigDecimal resolvePrixInitial(Voyages voyage) {
        if (voyage == null) {
            return null;
        }
        return voyage.getPrixInitial() != null ? voyage.getPrixInitial() : voyage.getPrixBase();
    }
}
