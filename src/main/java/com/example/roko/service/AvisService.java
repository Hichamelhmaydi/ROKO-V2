package com.example.roko.service;

import com.example.roko.dto.AvisDTO;
import com.example.roko.entity.Avis;
import com.example.roko.entity.Voyageurs;
import com.example.roko.entity.Voyages;
import com.example.roko.enums.AvisStatus;
import com.example.roko.enums.ReservationStatut;
import com.example.roko.exception.BusinessException;
import com.example.roko.exception.ResourceNotFoundException;
import com.example.roko.mapper.AvisMapper;
import com.example.roko.repository.AvisRepository;
import com.example.roko.repository.ReservationRepository;
import com.example.roko.repository.UserRepository;
import com.example.roko.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AvisService {

    private final AvisRepository avisRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final VoyageRepository voyageRepository;
    private final AvisMapper avisMapper;

    public AvisDTO createAvis(AvisDTO dto, Long voyageurId) {
        Voyageurs voyageur = (Voyageurs) userRepository.findById(voyageurId)
                .orElseThrow(() -> new ResourceNotFoundException("Voyageur introuvable"));

        if (Boolean.TRUE.equals(voyageur.getBloque())) {
            throw new BusinessException("Votre compte est bloqué. Publication d'avis impossible.");
        }

        Voyages voyage = voyageRepository.findById(dto.getVoyageId())
                .orElseThrow(() -> new ResourceNotFoundException("Voyage introuvable"));

        boolean hasCompletedReservation = reservationRepository.existsByUserIdAndVoyageIdAndStatut(
                voyageurId, dto.getVoyageId(), ReservationStatut.COMPLETEE);

        if (!hasCompletedReservation) {
            throw new BusinessException("Vous devez terminer ce voyage avant de publier un avis.");
        }

        Avis avis = new Avis();
        avis.setVoyageur(voyageur);
        avis.setVoyage(voyage);
        avis.setNote(dto.getNote());
        avis.setCommentaire(dto.getCommentaire());
        avis.setStatut(AvisStatus.EN_ATTENTE);

        return avisMapper.toDTO(avisRepository.save(avis));
    }

    @Transactional(readOnly = true)
    public List<AvisDTO> getValidatedAvisByVoyage(Long voyageId) {
        return avisMapper.toDTOList(avisRepository.findValidatedByVoyageId(voyageId));
    }

    @Transactional(readOnly = true)
    public List<AvisDTO> getMyAvis(Long voyageurId) {
        return avisMapper.toDTOList(avisRepository.findByVoyageurId(voyageurId));
    }

    @Transactional(readOnly = true)
    public List<AvisDTO> getAvisByStatut(AvisStatus statut) {
        return avisMapper.toDTOList(avisRepository.findByStatut(statut));
    }

    public AvisDTO moderateAvis(Long avisId, AvisStatus statut) {
        if (statut == AvisStatus.EN_ATTENTE) {
            throw new BusinessException("Le statut de modération doit être VALIDE ou REFUSE.");
        }

        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));

        avis.setStatut(statut);
        avis.setDateModeration(LocalDateTime.now());

        return avisMapper.toDTO(avisRepository.save(avis));
    }

    public void deleteAvis(Long avisId) {
        if (!avisRepository.existsById(avisId)) {
            throw new ResourceNotFoundException("Avis introuvable");
        }
        avisRepository.deleteById(avisId);
    }

    @Transactional(readOnly = true)
    public long countByStatut(AvisStatus statut) {
        return avisRepository.countByStatut(statut);
    }
}
