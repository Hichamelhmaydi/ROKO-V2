package com.example.roko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private String stripeSessionId;
    private Double amount;
    private String status;
    private Long userId;
    private Long reservationId;
    private LocalDateTime dateCreation;
    private LocalDateTime datePaiement;
}

