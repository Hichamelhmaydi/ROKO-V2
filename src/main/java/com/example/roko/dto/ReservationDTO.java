package com.example.roko.dto;

import com.example.roko.enums.ReservationStatut;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    private Long id;

    @NotNull(message = "L'ID du voyage est obligatoire")
    private Long voyageId;

    private Long userId;

    private String voyageNom;
    private String voyageDestination;
    private String voyageDateDepart;
    private String voyageDateRetour;

    private String userNom;
    private String userPrenom;
    private String userEmail;

    @NotNull(message = "Le nombre de personnes est obligatoire")
    @Min(value = 1, message = "Le nombre de personnes doit être au moins 1")
    private Integer nombrePersonnes;

    private ReservationStatut statut;

    private LocalDateTime dateReservation;
    private LocalDateTime dateConfirmation;
    private LocalDateTime dateAnnulation;
    private LocalDateTime dateCompletion;

    private BigDecimal prixBase;
    private BigDecimal prixActivites;
    private BigDecimal montantTotal;

    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String commentaire;

    private List<Long> activitesOptionnellesIds;

    private List<ActiviteDTO> activites;

    private String motifAnnulation;

    private Boolean paiementEffectue;
    private LocalDateTime datePaiement;
}