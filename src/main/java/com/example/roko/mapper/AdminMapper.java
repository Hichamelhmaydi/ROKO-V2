package com.example.roko.mapper;

import com.example.roko.dto.response.AdminDTO;
import com.example.roko.entity.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    AdminDTO toDTO(Admin admin);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "adresse", ignore = true)
    @Mapping(target = "dateInscription", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "bloque", ignore = true)
    Admin toEntity(AdminDTO dto);

    List<AdminDTO> toDTOList(List<Admin> admins);

    List<Admin> toEntityList(List<AdminDTO> dtos);
}
