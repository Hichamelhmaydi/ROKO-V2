package com.example.roko.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "activites_voyages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activites_Voyages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activites activite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id", nullable = false)
    private Voyages voyage;


    @Column(precision = 10, scale = 2)
    private BigDecimal prix;


    @Column(nullable = false)
    private Boolean obligatoire = false;


    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;


    @Column(name = "jour_prevu")
    private String jourPrevu;

    @Column(name = "duree_minutes")
    private Integer dureeMinutes;


    @Column(length = 500)
    private String notes;

    
    @Column(nullable = false)
    private Boolean disponible = true;
}