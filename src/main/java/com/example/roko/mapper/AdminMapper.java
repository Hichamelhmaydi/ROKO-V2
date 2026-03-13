package com.example.roko.mapper;

import com.example.roko.dto.response.AdminDTO;
import com.example.roko.entity.Admin;
import com.example.roko.enums.CompteStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdminMapper {

    public AdminDTO toDTO(Admin admin) {
        if (admin == null) {
            return null;
        }

        AdminDTO dto = new AdminDTO();
        dto.setId(admin.getId());
        dto.setNom(admin.getNom());
        dto.setPrenom(admin.getPrenom());
        dto.setEmail(admin.getEmail());
        dto.setTelephone(admin.getTelephone());
        dto.setStatus(admin.getStatus() != null ? admin.getStatus().name() : null);

        return dto;
    }

    public Admin toEntity(AdminDTO dto) {
        if (dto == null) {
            return null;
        }

        Admin admin = new Admin();
        admin.setId(dto.getId());
        admin.setNom(dto.getNom());
        admin.setPrenom(dto.getPrenom());
        admin.setEmail(dto.getEmail());
        admin.setTelephone(dto.getTelephone());

        // Conversion du statut
        if (dto.getStatus() != null) {
            admin.setStatus(CompteStatus.valueOf(dto.getStatus()));
        }

        return admin;
    }

    public List<AdminDTO> toDTOList(List<Admin> admins) {
        if (admins == null) {
            return new ArrayList<>();
        }
        return admins.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<Admin> toEntityList(List<AdminDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}