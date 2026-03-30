package com.example.roko.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoyageDTO {

    private Long id;

    @NotBlank(message = "Le nom du voyage est obligatoire")
    private String nom;

    @NotBlank(message = "La description du voyage est obligatoire")
    private String description;

    private String cover;

    @NotBlank(message = "La destination est obligatoire")
    private String destination;

    @NotBlank(message = "La date de départ est obligatoire")
    private String dateDepart;

    @NotBlank(message = "La date de retour est obligatoire")
    private String dateRetour;

    private String statut;
    private String itineraire;

    @DecimalMin(value = "0.01", message = "Le prix initial doit être supérieur à 0")
    private BigDecimal prixInitial;

    @NotNull(message = "Le prix de base est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix de base doit être supérieur à 0")
    private BigDecimal prixBase;

    private List<String> photos;

    @JsonIgnore
    @AssertTrue(message = "La date de départ doit être aujourd'hui ou dans le futur")
    public boolean isDateDepartValid() {
        if (dateDepart == null || dateDepart.trim().isEmpty()) {
            return true;
        }
        try {
            return !LocalDate.parse(dateDepart).isBefore(LocalDate.now());
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    @JsonIgnore
    @AssertTrue(message = "La date de retour doit être après la date de départ")
    public boolean isDateRangeValid() {
        if (dateDepart == null || dateDepart.trim().isEmpty() || dateRetour == null || dateRetour.trim().isEmpty()) {
            return true;
        }
        try {
            LocalDate depart = LocalDate.parse(dateDepart);
            LocalDate retour = LocalDate.parse(dateRetour);
            return retour.isAfter(depart);
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
