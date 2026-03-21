package com.example.roko.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
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
    private BigDecimal prixInitial;
    private BigDecimal prixBase;
    private List<String> photos;
}
