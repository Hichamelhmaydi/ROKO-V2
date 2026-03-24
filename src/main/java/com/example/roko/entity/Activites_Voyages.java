package com.example.roko.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "activites_voyages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activites_Voyages {

    @EmbeddedId
    private ActiviteVoyageId id;

    @MapsId("activiteId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activites activite;

    @MapsId("voyageId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id", nullable = false)
    private Voyages voyage;

    public Activites_Voyages(Activites activite, Voyages voyage) {
        this.activite = activite;
        this.voyage = voyage;
        this.id = new ActiviteVoyageId(activite.getId(), voyage.getId());
    }
}
