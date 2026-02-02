package com.example.roko.mapper;

import com.example.roko.dto.PaymentDTO;
import com.example.roko.entity.Payment;
import com.example.roko.entity.Reservations;
import com.example.roko.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    public PaymentDTO toDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setStripeSessionId(payment.getStripeSessionId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.setUserId(payment.getUserId());
        dto.setReservationId(payment.getReservation() != null ? payment.getReservation().getId() : null);
        dto.setDateCreation(payment.getDateCreation());
        dto.setDatePaiement(payment.getDatePaiement());

        return dto;
    }

    public Payment toEntity(PaymentDTO dto) {
        if (dto == null) {
            return null;
        }

        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setStripeSessionId(dto.getStripeSessionId());
        payment.setAmount(dto.getAmount());
        payment.setUserId(dto.getUserId());
        payment.setDateCreation(dto.getDateCreation());
        payment.setDatePaiement(dto.getDatePaiement());

        // Conversion du statut
        if (dto.getStatus() != null) {
            payment.setStatus(PaymentStatus.valueOf(dto.getStatus()));
        }

        // La réservation sera set séparément dans le service
        if (dto.getReservationId() != null) {
            Reservations reservation = new Reservations();
            reservation.setId(dto.getReservationId());
            payment.setReservation(reservation);
        }

        return payment;
    }

    public List<PaymentDTO> toDTOList(List<Payment> payments) {
        if (payments == null) {
            return new ArrayList<>();
        }
        return payments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<Payment> toEntityList(List<PaymentDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}