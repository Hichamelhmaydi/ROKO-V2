package com.example.roko.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteVoyageId implements Serializable {

    @Column(name = "activite_id")
    private Long activiteId;

    @Column(name = "voyage_id")
    private Long voyageId;
}
