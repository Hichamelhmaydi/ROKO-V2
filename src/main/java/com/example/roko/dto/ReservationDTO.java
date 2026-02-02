package com.example.roko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private Long id;
    private Long voyageurId;
    private Long voyageId;
    private String statut;
    private LocalDateTime dateReservation;
    private Double prixTotal;
    private String notes;
}

