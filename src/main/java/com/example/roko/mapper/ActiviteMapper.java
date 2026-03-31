package com.example.roko.mapper;

import com.example.roko.dto.response.ActiviteDTO;
import com.example.roko.entity.Activites;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActiviteMapper {

    @Mapping(target = "voyageId", source = "voyage.id")
    @Mapping(target = "voyageNom", source = "voyage.nom")
    @Mapping(target = "nombreReservations", expression = "java(activite.getReservations() != null ? activite.getReservations().size() : null)")
    ActiviteDTO toDTO(Activites activite);

    @Mapping(target = "voyage", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    Activites toEntity(ActiviteDTO dto);

    List<ActiviteDTO> toDTOList(List<Activites> activites);

    List<Activites> toEntityList(List<ActiviteDTO> dtos);

    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "prix", source = "prix")
    @Mapping(target = "obligatoire", source = "obligatoire")
    void updateEntityFromDTO(ActiviteDTO dto, @MappingTarget Activites activite);
}
