package com.example.roko.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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


    @OneToMany(mappedBy = "activite", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Activites_Voyages> activitesVoyages = new HashSet<>();


    @ManyToMany(mappedBy = "activites")
    private Set<Reservations> reservations = new HashSet<>();


    public void addVoyage(Voyages voyage, Boolean obligatoire, Integer ordre) {
        Activites_Voyages activiteVoyage = new Activites_Voyages();
        activiteVoyage.setActivite(this);
        activiteVoyage.setVoyage(voyage);
        activiteVoyage.setObligatoire(obligatoire);
        activiteVoyage.setOrdreAffichage(ordre);
        activiteVoyage.setDisponible(true);

        activitesVoyages.add(activiteVoyage);
        voyage.getActivitesVoyages().add(activiteVoyage);
    }

    public void removeVoyage(Voyages voyage) {
        activitesVoyages.removeIf(av ->
                av.getActivite().equals(this) && av.getVoyage().equals(voyage));
        voyage.getActivitesVoyages().removeIf(av ->
                av.getActivite().equals(this) && av.getVoyage().equals(voyage));
    }
}