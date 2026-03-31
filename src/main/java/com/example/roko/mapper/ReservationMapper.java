package com.example.roko.mapper;

import com.example.roko.dto.response.ActiviteDTO;
import com.example.roko.dto.response.ReservationDTO;
import com.example.roko.entity.Activites;
import com.example.roko.entity.Reservations;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = ActiviteMapper.class)
public interface ReservationMapper {

    @Mapping(target = "userId", source = "voyageur.id")
    @Mapping(target = "voyageId", source = "voyage.id")
    @Mapping(target = "voyageNom", source = "voyage.nom")
    @Mapping(target = "voyageDestination", source = "voyage.destination")
    @Mapping(target = "voyageDateDepart", source = "voyage.dateDepart")
    @Mapping(target = "voyageDateRetour", source = "voyage.dateRetour")
    @Mapping(target = "userNom", source = "voyageur.nom")
    @Mapping(target = "userPrenom", source = "voyageur.prenom")
    @Mapping(target = "userEmail", source = "voyageur.email")
    @Mapping(target = "activites", source = "activites")
    @Mapping(target = "activitesOptionnellesIds", source = "activites", qualifiedByName = "activitesToIds")
    ReservationDTO toDTO(Reservations reservation);

    @Mapping(target = "voyageur", ignore = true)
    @Mapping(target = "voyage", ignore = true)
    @Mapping(target = "activites", ignore = true)
    Reservations toEntity(ReservationDTO dto);

    List<ReservationDTO> toDTOList(List<Reservations> reservations);

    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "nombrePersonnes", source = "nombrePersonnes")
    @Mapping(target = "commentaire", source = "commentaire")
    @Mapping(target = "statut", source = "statut")
    void updateEntityFromDTO(ReservationDTO dto, @MappingTarget Reservations reservation);

    @Named("activitesToIds")
    default List<Long> activitesToIds(java.util.Set<Activites> activites) {
        if (activites == null || activites.isEmpty()) {
            return java.util.List.of();
        }
        return activites.stream().map(Activites::getId).toList();
    }
}
