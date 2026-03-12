package com.example.roko.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("ADMIN")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Admin extends User {
}
