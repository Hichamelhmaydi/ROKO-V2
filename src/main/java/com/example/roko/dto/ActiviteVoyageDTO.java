package com.example.roko.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteVoyageDTO {

    private Long id;

    @NotNull(message = "L'ID de l'activité est obligatoire")
    private Long activiteId;

    @NotNull(message = "L'ID du voyage est obligatoire")
    private Long voyageId;

    private String activiteNom;
    private String activiteDescription;

    private String voyageNom;
    private String voyageDestination;

    private BigDecimal prix;
    private Boolean obligatoire = false;
    private Integer ordreAffichage;
    private String jourPrevu;
    private Integer dureeMinutes;
    private String notes;
    private Boolean disponible = true;
}