package com.example.roko.service;

import com.example.roko.dto.response.ActiviteVoyageDTO;
import com.example.roko.entity.Activites;
import com.example.roko.entity.ActiviteVoyageId;
import com.example.roko.entity.Activites_Voyages;
import com.example.roko.entity.Voyages;
import com.example.roko.exception.BusinessException;
import com.example.roko.exception.ResourceNotFoundException;
import com.example.roko.repository.ActiviteRepository;
import com.example.roko.repository.ActiviteVoyageRepository;
import com.example.roko.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ActiviteVoyageService {

    private final ActiviteVoyageRepository activiteVoyageRepository;
    private final ActiviteRepository activiteRepository;
    private final VoyageRepository voyageRepository;

    public ActiviteVoyageDTO associerActiviteAVoyage(ActiviteVoyageDTO dto) {
        log.info("Association activité {} au voyage {}", dto.getActiviteId(), dto.getVoyageId());

        Activites activite = activiteRepository.findById(dto.getActiviteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                "Activité non trouvée avec l'ID: " + dto.getActiviteId()));

        Voyages voyage = voyageRepository.findById(dto.getVoyageId())
                .orElseThrow(() -> new ResourceNotFoundException(
                "Voyage non trouvé avec l'ID: " + dto.getVoyageId()));

        if (activiteVoyageRepository.existsByActiviteIdAndVoyageId(
                dto.getActiviteId(), dto.getVoyageId())) {
            throw new BusinessException(
                    "Cette activité est déjà associée à ce voyage");
        }

        Activites_Voyages activiteVoyage = new Activites_Voyages();
        activiteVoyage.setActivite(activite);
        activiteVoyage.setVoyage(voyage);
        activiteVoyage.setId(new ActiviteVoyageId(dto.getActiviteId(), dto.getVoyageId()));

        Activites_Voyages saved = activiteVoyageRepository.save(activiteVoyage);
        log.info("Association créée avec succès. activiteId={}, voyageId={}", dto.getActiviteId(), dto.getVoyageId());

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ActiviteVoyageDTO> getActivitesByVoyage(Long voyageId) {
        log.info("Récupération des activités du voyage {}", voyageId);

        if (!voyageRepository.existsById(voyageId)) {
            throw new ResourceNotFoundException("Voyage non trouvé avec l'ID: " + voyageId);
        }

        List<Activites_Voyages> associations = activiteVoyageRepository.findByVoyageId(voyageId);
        return associations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActiviteVoyageDTO> getVoyagesByActivite(Long activiteId) {
        log.info("Récupération des voyages contenant l'activité {}", activiteId);

        if (!activiteRepository.existsById(activiteId)) {
            throw new ResourceNotFoundException("Activité non trouvée avec l'ID: " + activiteId);
        }

        List<Activites_Voyages> associations = activiteVoyageRepository.findByActiviteId(activiteId);
        return associations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActiviteVoyageDTO> getActivitesObligatoires(Long voyageId) {
        log.info("Récupération des activités obligatoires du voyage {}", voyageId);
        return new ArrayList<ActiviteVoyageDTO>();
    }

    @Transactional(readOnly = true)
    public List<ActiviteVoyageDTO> getActivitesOptionnelles(Long voyageId) {
        log.info("Récupération des activités optionnelles du voyage {}", voyageId);

        List<Activites_Voyages> associations
                = activiteVoyageRepository.findOptionellesByVoyageId(voyageId);
        return associations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActiviteVoyageDTO> getActivitesByJour(Long voyageId, String jour) {
        log.info("Récupération des activités du jour {} pour le voyage {}", jour, voyageId);
        return new ArrayList<ActiviteVoyageDTO>();
    }

    public ActiviteVoyageDTO updateAssociation(Long id, ActiviteVoyageDTO dto) {
        throw new BusinessException("La mise à jour par ID n'est plus supportée: utilisez les IDs activite/voyage");
    }

    public void dissocierActiviteDeVoyage(Long activiteId, Long voyageId) {
        log.info("Dissociation de l'activité {} du voyage {}", activiteId, voyageId);

        Activites_Voyages association = activiteVoyageRepository
                .findByActiviteIdAndVoyageId(activiteId, voyageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Association non trouvée entre l'activité " + activiteId
                + " et le voyage " + voyageId));

        activiteVoyageRepository.delete(association);
        log.info("Dissociation effectuée avec succès");
    }

    public void deleteAssociation(Long id) {
        throw new BusinessException("La suppression par ID n'est plus supportée: utilisez les IDs activite/voyage");
    }

    @Transactional(readOnly = true)
    public long countActivitesByVoyage(Long voyageId) {
        return activiteVoyageRepository.countByVoyageId(voyageId);
    }

    @Transactional(readOnly = true)
    public long countActivitesObligatoires(Long voyageId) {
        return 0;
    }

    private ActiviteVoyageDTO toDTO(Activites_Voyages entity) {
        ActiviteVoyageDTO dto = new ActiviteVoyageDTO();
        dto.setActiviteId(entity.getActivite().getId());
        dto.setVoyageId(entity.getVoyage().getId());
        dto.setActiviteNom(entity.getActivite().getNom());
        dto.setActiviteDescription(entity.getActivite().getDescription());
        dto.setVoyageNom(entity.getVoyage().getNom());
        dto.setVoyageDestination(entity.getVoyage().getDestination());
        dto.setPrix(entity.getActivite().getPrix());
        dto.setObligatoire(false);
        dto.setDisponible(true);
        return dto;
    }
}
