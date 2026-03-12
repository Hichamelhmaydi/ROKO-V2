package com.example.roko.entity;

import com.example.roko.enums.ReservationStatut;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id", nullable = false)
    private Voyages voyage;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatut statut = ReservationStatut.EN_ATTENTE;

    @Column(name = "date_reservation", nullable = false)
    private LocalDateTime dateReservation;


    @Column(name = "nombre_personnes", nullable = false)
    private Integer nombrePersonnes = 1;


    @Column(name = "prix_base", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixBase;


    @Column(name = "prix_activites", precision = 10, scale = 2)
    private BigDecimal prixActivites = BigDecimal.ZERO;

    @Column(name = "montant_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal montantTotal;

    @Column(length = 1000)
    private String commentaire;

    @ManyToMany
    @JoinTable(
            name = "reservations_activites",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "activite_id")
    )
    private Set<Activites> activites = new HashSet<>();


    @Column(name = "date_confirmation")
    private LocalDateTime dateConfirmation;

    @Column(name = "date_annulation")
    private LocalDateTime dateAnnulation;

    @Column(name = "motif_annulation", length = 500)
    private String motifAnnulation;

    @Column(name = "date_completion")
    private LocalDateTime dateCompletion;


    @Column(name = "paiement_effectue")
    private Boolean paiementEffectue = false;


    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;



    @PrePersist
    public void prePersist() {
        if (dateReservation == null) {
            dateReservation = LocalDateTime.now();
        }
        calculerMontantTotal();
    }

    @PreUpdate
    public void calculerMontantTotal() {
        if (prixBase != null && nombrePersonnes != null) {
            BigDecimal montantBase = prixBase.multiply(BigDecimal.valueOf(nombrePersonnes));
            BigDecimal montantActivites = prixActivites != null ? prixActivites : BigDecimal.ZERO;
            this.montantTotal = montantBase.add(montantActivites);
        }
    }
}