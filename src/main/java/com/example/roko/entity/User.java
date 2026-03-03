package com.example.roko.entity;

import com.example.roko.enums.CompteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String telephone;

    @Column(length = 500)
    private String adresse;

    @Column(name = "date_inscription")
    private LocalDateTime dateInscription;

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(nullable = false)
    private Boolean bloque = false;

    @Enumerated(EnumType.STRING)
    private CompteStatus status;

    @PrePersist
    public void prePersist() {
        if (dateInscription == null) {
            dateInscription = LocalDateTime.now();
        }
        if (status == null) {
            status = CompteStatus.ACTIVER;
        }
    }
}