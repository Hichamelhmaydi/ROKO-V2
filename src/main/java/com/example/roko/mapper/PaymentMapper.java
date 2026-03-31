package com.example.roko.mapper;

import com.example.roko.dto.response.PaymentDTO;
import com.example.roko.entity.Payment;
import com.example.roko.entity.Reservations;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "status", source = "status")
    @Mapping(target = "reservationId", source = "reservation.id")
    PaymentDTO toDTO(Payment payment);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "voyageurId", ignore = true)
    @Mapping(target = "reservation", source = "reservationId", qualifiedByName = "reservationFromId")
    Payment toEntity(PaymentDTO dto);

    List<PaymentDTO> toDTOList(List<Payment> payments);

    List<Payment> toEntityList(List<PaymentDTO> dtos);

    @Named("reservationFromId")
    default Reservations reservationFromId(Long reservationId) {
        if (reservationId == null) {
            return null;
        }
        Reservations reservation = new Reservations();
        reservation.setId(reservationId);
        return reservation;
    }
}
