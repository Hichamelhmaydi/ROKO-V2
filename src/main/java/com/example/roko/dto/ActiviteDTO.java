package com.example.roko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteDTO {
    private Long id;
    private String nom;
    private String description;
    private Long voyageId;
}

