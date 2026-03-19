package com.example.roko.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("VOYAGEUR")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Voyageurs extends User {

    @Column(name = "id_national")
    private String idNational;

    @Column(name = "date_expiration")
    private String dateExpiration;

    @OneToMany(mappedBy = "voyageur", cascade = CascadeType.ALL)
    private Set<Reservations> reservations = new HashSet<>();
}
