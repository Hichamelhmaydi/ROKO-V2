package com.example.roko.mapper;

import com.example.roko.dto.request.CreateUserRequest;
import com.example.roko.dto.response.UserDTO;
import com.example.roko.entity.Admin;
import com.example.roko.entity.User;
import com.example.roko.entity.Voyageurs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "status", source = "status")
    @Mapping(target = "role", expression = "java(resolveRole(user))")
    UserDTO toDTO(User user);

    default String resolveRole(User user) {
        if (user == null) {
            return null;
        }
        if (user instanceof Voyageurs) {
            return "VOYAGEUR";
        }
        if (user instanceof Admin) {
            return "ADMIN";
        }
        return "USER";
    }

    default User toEntity(CreateUserRequest dto) {
        throw new UnsupportedOperationException("Utilisez un mapper spécifique (VoyageurMapper ou AdminMapper) pour créer une entité.");
    }
}
