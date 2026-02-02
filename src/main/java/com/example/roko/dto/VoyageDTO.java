package com.example.roko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoyageDTO {
    private Long id;
    private String nom;
    private String description;
    private String cover;
    private String destination;
    private String dateDepart;
    private String dateRetour;
    private String statut;
    private String itineraire;
    private List<String> photos;
}
