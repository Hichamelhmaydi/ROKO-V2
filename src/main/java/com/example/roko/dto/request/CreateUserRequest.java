package com.example.roko.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String password;
}