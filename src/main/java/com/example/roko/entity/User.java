package com.example.roko.entity;

import com.example.roko.enums.CompteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

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


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Reservations> reservations = new HashSet<>();
    private CompteStatus status;

    @PrePersist
    public void prePersist() {
        if (dateInscription == null) {
            dateInscription = LocalDateTime.now();
        }
    }

    public void setStatus(CompteStatus status) {

        this.status = status;
    }

    public CompteStatus getStatus() {
        return status;
    }
}