package com.example.roko.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("ADMINISTRATEUR")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Admin extends User {
}
