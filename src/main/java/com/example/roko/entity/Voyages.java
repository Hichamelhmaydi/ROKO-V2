package com.example.roko.entity;

import com.example.roko.enums.VoyageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "voyages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voyages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(length = 2000)
    private String description;

    @Column(name = "cover")
    private String cover;

    @Column(nullable = false)
    private String destination;

    @Column(name = "date_depart", nullable = false)
    private String dateDepart;

    @Column(name = "date_retour", nullable = false)
    private String dateRetour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoyageStatus statut = VoyageStatus.DISPONIBLE;

    @Column(length = 1000)
    private String itineraire;

    @ElementCollection
    @CollectionTable(name = "voyage_photos", joinColumns = @JoinColumn(name = "voyage_id"))
    @Column(name = "photo_url")
    private List<String> photos = new ArrayList<>();


    @OneToMany(mappedBy = "voyage", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Activites_Voyages> activitesVoyages = new HashSet<>();

    @OneToMany(mappedBy = "voyage", cascade = CascadeType.ALL)
    private Set<Reservations> reservations = new HashSet<>();


    public void addActivite(Activites activite, Boolean obligatoire, Integer ordre) {
        Activites_Voyages activiteVoyage = new Activites_Voyages();
        activiteVoyage.setActivite(activite);
        activiteVoyage.setVoyage(this);
        activiteVoyage.setObligatoire(obligatoire);
        activiteVoyage.setOrdreAffichage(ordre);
        activiteVoyage.setDisponible(true);

        activitesVoyages.add(activiteVoyage);
        activite.getActivitesVoyages().add(activiteVoyage);
    }


    public void removeActivite(Activites activite) {
        activitesVoyages.removeIf(av ->
                av.getVoyage().equals(this) && av.getActivite().equals(activite));
        activite.getActivitesVoyages().removeIf(av ->
                av.getVoyage().equals(this) && av.getActivite().equals(activite));
    }
}