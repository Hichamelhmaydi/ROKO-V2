package com.example.roko.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "activites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activites {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal prix = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id")
    private Voyages voyage;

    @ManyToMany(mappedBy = "activites")
    private Set<Reservations> reservations = new HashSet<>();
}
