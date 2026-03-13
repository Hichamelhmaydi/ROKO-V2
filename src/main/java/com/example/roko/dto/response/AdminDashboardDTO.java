package com.example.roko.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {

    private long totalReservations;
    private long reservationsEnAttente;
    private long reservationsConfirmees;
    private long reservationsCompletees;
    private long reservationsAnnulees;

    private long totalVoyages;
    private long voyagesDisponibles;

    private long totalVoyageurs;
    private long voyageursActifs;
    private long voyageursBloques;
}
