package com.example.roko.dto;

import com.example.roko.enums.AvisStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvisDTO {

    private Long id;

    @NotNull(message = "L'ID du voyage est obligatoire")
    private Long voyageId;

    private Long voyageurId;

    private String voyageNom;
    private String voyageurNom;
    private String voyageurPrenom;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimum est 1")
    @Max(value = 5, message = "La note maximum est 5")
    private Integer note;

    @Size(max = 2000, message = "Le commentaire ne peut pas dépasser 2000 caractères")
    private String commentaire;

    private AvisStatus statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModeration;
}
