package com.example.roko.mapper;

import com.example.roko.dto.CreateUserRequest;
import com.example.roko.dto.UserDTO;
import com.example.roko.entity.Admin;
import com.example.roko.entity.User;
import com.example.roko.entity.Voyageurs;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setTelephone(user.getTelephone());
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);

        if (user instanceof Voyageurs) {
            dto.setRole("VOYAGEUR");
        } else if (user instanceof Admin) {
            dto.setRole("ADMIN");
        } else {
            dto.setRole("USER");
        }

        return dto;
    }

    public User toEntity(CreateUserRequest dto) {
        throw new UnsupportedOperationException("Utilisez un mapper spécifique (VoyageurMapper ou AdminMapper) pour créer une entité.");
    }
}