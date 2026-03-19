package com.example.roko.service;

import com.example.roko.dto.response.ActiviteVoyageDTO;
import com.example.roko.entity.Activites;
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

import java.math.BigDecimal;
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
        activiteVoyage.setPrix(dto.getPrix());
        activiteVoyage.setObligatoire(dto.getObligatoire() != null ? dto.getObligatoire() : false);
        activiteVoyage.setOrdreAffichage(dto.getOrdreAffichage());
        activiteVoyage.setJourPrevu(dto.getJourPrevu());
        activiteVoyage.setDureeMinutes(dto.getDureeMinutes());
        activiteVoyage.setNotes(dto.getNotes());
        activiteVoyage.setDisponible(dto.getDisponible() != null ? dto.getDisponible() : true);

        Activites_Voyages saved = activiteVoyageRepository.save(activiteVoyage);
        log.info("Association créée avec succès. ID: {}", saved.getId());

        // Si l'activité est obligatoire, mettre à jour le prix de base du voyage
        if (Boolean.TRUE.equals(saved.getObligatoire())) {
            BigDecimal prixActivite = saved.getPrix() != null && saved.getPrix().compareTo(BigDecimal.ZERO) > 0
                    ? saved.getPrix()
                    : (activite.getPrix() != null ? activite.getPrix() : BigDecimal.ZERO);
            voyage.setPrixBase(voyage.getPrixBase().add(prixActivite));
            voyageRepository.save(voyage);
            log.info("Prix du voyage {} mis à jour: +{}", voyage.getId(), prixActivite);
        }

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

        List<Activites_Voyages> associations
                = activiteVoyageRepository.findObligatoiresByVoyageId(voyageId);
        return associations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

        List<Activites_Voyages> associations
                = activiteVoyageRepository.findByVoyageIdAndJour(voyageId, jour);
        return associations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ActiviteVoyageDTO updateAssociation(Long id, ActiviteVoyageDTO dto) {
        log.info("Mise à jour de l'association {}", id);

        Activites_Voyages association = activiteVoyageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Association non trouvée avec l'ID: " + id));

        // Capturer l'état avant modification pour le calcul du prix
        Boolean oldObligatoire = association.getObligatoire();
        BigDecimal oldPrix = resolveEffectivePrix(association);

        if (dto.getPrix() != null) {
            association.setPrix(dto.getPrix());
        }
        if (dto.getObligatoire() != null) {
            association.setObligatoire(dto.getObligatoire());
        }
        if (dto.getOrdreAffichage() != null) {
            association.setOrdreAffichage(dto.getOrdreAffichage());
        }
        if (dto.getJourPrevu() != null) {
            association.setJourPrevu(dto.getJourPrevu());
        }
        if (dto.getDureeMinutes() != null) {
            association.setDureeMinutes(dto.getDureeMinutes());
        }
        if (dto.getNotes() != null) {
            association.setNotes(dto.getNotes());
        }
        if (dto.getDisponible() != null) {
            association.setDisponible(dto.getDisponible());
        }

        Activites_Voyages updated = activiteVoyageRepository.save(association);

        // Ajuster le prix de base du voyage selon les changements
        boolean wasObligatoire = Boolean.TRUE.equals(oldObligatoire);
        boolean isObligatoire = Boolean.TRUE.equals(updated.getObligatoire());
        BigDecimal newPrix = resolveEffectivePrix(updated);
        Voyages voyage = updated.getVoyage();

        if (wasObligatoire && !isObligatoire) {
            voyage.setPrixBase(voyage.getPrixBase().subtract(oldPrix).max(BigDecimal.ZERO));
            voyageRepository.save(voyage);
            log.info("Prix du voyage {} mis à jour (retrait obligatoire): -{}", voyage.getId(), oldPrix);
        } else if (!wasObligatoire && isObligatoire) {
            voyage.setPrixBase(voyage.getPrixBase().add(newPrix));
            voyageRepository.save(voyage);
            log.info("Prix du voyage {} mis à jour (ajout obligatoire): +{}", voyage.getId(), newPrix);
        } else if (wasObligatoire && isObligatoire && oldPrix.compareTo(newPrix) != 0) {
            voyage.setPrixBase(voyage.getPrixBase().subtract(oldPrix).add(newPrix).max(BigDecimal.ZERO));
            voyageRepository.save(voyage);
            log.info("Prix du voyage {} mis à jour (changement prix obligatoire): {} -> {}", voyage.getId(), oldPrix, newPrix);
        }

        log.info("Association mise à jour avec succès");
        return toDTO(updated);
    }

    public void dissocierActiviteDeVoyage(Long activiteId, Long voyageId) {
        log.info("Dissociation de l'activité {} du voyage {}", activiteId, voyageId);

        Activites_Voyages association = activiteVoyageRepository
                .findByActiviteIdAndVoyageId(activiteId, voyageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Association non trouvée entre l'activité " + activiteId
                + " et le voyage " + voyageId));

        // Si l'activité était obligatoire, soustraire son prix du voyage
        if (Boolean.TRUE.equals(association.getObligatoire())) {
            BigDecimal prix = resolveEffectivePrix(association);
            Voyages voyage = association.getVoyage();
            voyage.setPrixBase(voyage.getPrixBase().subtract(prix).max(BigDecimal.ZERO));
            voyageRepository.save(voyage);
            log.info("Prix du voyage {} mis à jour (dissociation obligatoire): -{}", voyage.getId(), prix);
        }

        activiteVoyageRepository.delete(association);
        log.info("Dissociation effectuée avec succès");
    }

    public void deleteAssociation(Long id) {
        log.info("Suppression de l'association {}", id);

        Activites_Voyages association = activiteVoyageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Association non trouvée avec l'ID: " + id));

        // Si l'activité était obligatoire, soustraire son prix du voyage
        if (Boolean.TRUE.equals(association.getObligatoire())) {
            BigDecimal prix = resolveEffectivePrix(association);
            Voyages voyage = association.getVoyage();
            voyage.setPrixBase(voyage.getPrixBase().subtract(prix).max(BigDecimal.ZERO));
            voyageRepository.save(voyage);
            log.info("Prix du voyage {} mis à jour (suppression obligatoire): -{}", voyage.getId(), prix);
        }

        activiteVoyageRepository.delete(association);
        log.info("Association supprimée avec succès");
    }

    @Transactional(readOnly = true)
    public long countActivitesByVoyage(Long voyageId) {
        return activiteVoyageRepository.countByVoyageId(voyageId);
    }

    @Transactional(readOnly = true)
    public long countActivitesObligatoires(Long voyageId) {
        return activiteVoyageRepository.countObligatoiresByVoyageId(voyageId);
    }

    private BigDecimal resolveEffectivePrix(Activites_Voyages association) {
        if (association.getPrix() != null && association.getPrix().compareTo(BigDecimal.ZERO) > 0) {
            return association.getPrix();
        }
        Activites activite = association.getActivite();
        return activite.getPrix() != null ? activite.getPrix() : BigDecimal.ZERO;
    }

    private ActiviteVoyageDTO toDTO(Activites_Voyages entity) {
        ActiviteVoyageDTO dto = new ActiviteVoyageDTO();
        dto.setId(entity.getId());
        dto.setActiviteId(entity.getActivite().getId());
        dto.setVoyageId(entity.getVoyage().getId());
        dto.setActiviteNom(entity.getActivite().getNom());
        dto.setActiviteDescription(entity.getActivite().getDescription());
        dto.setVoyageNom(entity.getVoyage().getNom());
        dto.setVoyageDestination(entity.getVoyage().getDestination());
        dto.setPrix(entity.getPrix());
        dto.setObligatoire(entity.getObligatoire());
        dto.setOrdreAffichage(entity.getOrdreAffichage());
        dto.setJourPrevu(entity.getJourPrevu());
        dto.setDureeMinutes(entity.getDureeMinutes());
        dto.setNotes(entity.getNotes());
        dto.setDisponible(entity.getDisponible());
        return dto;
    }
}
