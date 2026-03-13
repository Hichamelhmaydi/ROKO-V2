package com.example.roko.entity;

import com.example.roko.enums.AvisStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "avis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyageur_id", nullable = false)
    private Voyageurs voyageur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id", nullable = false)
    private Voyages voyage;

    @Column(nullable = false)
    private Integer note;

    @Column(length = 2000)
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvisStatus statut = AvisStatus.EN_ATTENTE;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_moderation")
    private LocalDateTime dateModeration;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
