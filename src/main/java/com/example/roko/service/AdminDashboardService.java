package com.example.roko.service;

import com.example.roko.dto.response.AdminDashboardDTO;
import com.example.roko.enums.CompteStatus;
import com.example.roko.enums.ReservationStatut;
import com.example.roko.enums.VoyageStatus;
import com.example.roko.repository.ReservationRepository;
import com.example.roko.repository.VoyageurRepository;
import com.example.roko.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final ReservationRepository reservationRepository;
    private final VoyageRepository voyageRepository;
    private final VoyageurRepository voyageurRepository;

    public AdminDashboardDTO getDashboardStats() {
        return new AdminDashboardDTO(
                reservationRepository.count(),
                reservationRepository.countByStatut(ReservationStatut.EN_ATTENTE),
                reservationRepository.countByStatut(ReservationStatut.EN_ATTENTE_PAIEMENT),
                reservationRepository.countByStatut(ReservationStatut.PAYEE),
                reservationRepository.countByStatut(ReservationStatut.ANNULEE),
                voyageRepository.count(),
                voyageRepository.countByStatut(VoyageStatus.DISPONIBLE),
                voyageurRepository.count(),
                voyageurRepository.countByStatusAndBloqueFalse(CompteStatus.ACTIVER),
                voyageurRepository.countByBloqueTrue()
        );
    }
}
