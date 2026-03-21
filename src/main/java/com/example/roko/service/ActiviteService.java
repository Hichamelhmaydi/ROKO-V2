package com.example.roko.service;

import com.example.roko.dto.response.ActiviteDTO;
import com.example.roko.entity.Activites;
import com.example.roko.entity.Activites_Voyages;
import com.example.roko.entity.Voyages;
import com.example.roko.exception.ResourceNotFoundException;
import com.example.roko.exception.BusinessException;
import com.example.roko.mapper.ActiviteMapper;
import com.example.roko.repository.ActiviteRepository;
import com.example.roko.repository.ActiviteVoyageRepository;
import com.example.roko.repository.ReservationRepository;
import com.example.roko.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final VoyageRepository voyageRepository;
    private final ActiviteMapper activiteMapper;
    private final ActiviteVoyageRepository activiteVoyageRepository;
    private final ReservationRepository reservationRepository;

    public ActiviteDTO createActivite(ActiviteDTO activiteDTO) {
        log.info("Création d'une nouvelle activité: {}", activiteDTO.getNom());

        Voyages voyage = voyageRepository.findById(activiteDTO.getVoyageId())
                .orElseThrow(() -> new ResourceNotFoundException(
                "Voyage non trouvé avec l'ID: " + activiteDTO.getVoyageId()));

        if (activiteRepository.existsByNomAndVoyageId(activiteDTO.getNom(), activiteDTO.getVoyageId())) {
            throw new BusinessException(
                    "Une activité avec le nom '" + activiteDTO.getNom() + "' existe déjà pour ce voyage");
        }

        Activites activite = activiteMapper.toEntity(activiteDTO);
        activite.setVoyage(voyage);

        Activites savedActivite = activiteRepository.save(activite);
        log.info("Activité créée avec succès. ID: {}", savedActivite.getId());

        // Créer automatiquement l'entrée dans activites_voyages (optionnelle par défaut)
        if (!activiteVoyageRepository.existsByActiviteIdAndVoyageId(savedActivite.getId(), voyage.getId())) {
            Activites_Voyages activiteVoyage = new Activites_Voyages();
            activiteVoyage.setActivite(savedActivite);
            activiteVoyage.setVoyage(voyage);
            activiteVoyage.setObligatoire(false);
            activiteVoyage.setDisponible(true);
            activiteVoyage.setPrix(savedActivite.getPrix());
            activiteVoyageRepository.save(activiteVoyage);
            log.info("Association activite_voyage créée automatiquement pour l'activité {}", savedActivite.getId());
        }

        return activiteMapper.toDTO(savedActivite);
    }

    @Transactional(readOnly = true)
    public List<ActiviteDTO> getAllActivites() {
        log.info("Récupération de toutes les activités");
        List<Activites> activites = activiteRepository.findAll();
        return activiteMapper.toDTOList(activites);
    }

    @Transactional(readOnly = true)
    public ActiviteDTO getActiviteById(Long id) {
        log.info("Récupération de l'activité avec l'ID: {}", id);
        Activites activite = activiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Activité non trouvée avec l'ID: " + id));
        return activiteMapper.toDTO(activite);
    }

    @Transactional(readOnly = true)
    public ActiviteDTO getActiviteByIdWithReservations(Long id) {
        log.info("Récupération de l'activité avec réservations. ID: {}", id);
        Activites activite = activiteRepository.findByIdWithReservations(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Activité non trouvée avec l'ID: " + id));
        return activiteMapper.toDTO(activite);
    }

    @Transactional(readOnly = true)
    public List<ActiviteDTO> getActivitesByVoyageId(Long voyageId) {
        log.info("Récupération des activités pour le voyage ID: {}", voyageId);

        if (!voyageRepository.existsById(voyageId)) {
            throw new ResourceNotFoundException("Voyage non trouvé avec l'ID: " + voyageId);
        }

        List<Activites> activites = activiteRepository.findByVoyageId(voyageId);
        return activiteMapper.toDTOList(activites);
    }

    @Transactional(readOnly = true)
    public List<ActiviteDTO> getActivitesByVoyageIdWithReservations(Long voyageId) {
        log.info("Récupération des activités avec réservations pour le voyage ID: {}", voyageId);

        if (!voyageRepository.existsById(voyageId)) {
            throw new ResourceNotFoundException("Voyage non trouvé avec l'ID: " + voyageId);
        }

        List<Activites> activites = activiteRepository.findByVoyageIdWithReservations(voyageId);
        return activiteMapper.toDTOList(activites);
    }

    @Transactional(readOnly = true)
    public List<ActiviteDTO> searchActivitesByNom(String nom) {
        log.info("Recherche d'activités par nom: {}", nom);
        List<Activites> activites = activiteRepository.searchByNom(nom);
        return activiteMapper.toDTOList(activites);
    }

    @Transactional(readOnly = true)
    public List<ActiviteDTO> searchActivitesByDescription(String keyword) {
        log.info("Recherche d'activités par description: {}", keyword);
        List<Activites> activites = activiteRepository.searchByDescription(keyword);
        return activiteMapper.toDTOList(activites);
    }

    @Transactional(readOnly = true)
    public List<ActiviteDTO> getMostPopularActivites() {
        log.info("Récupération des activités les plus populaires");
        List<Activites> activites = activiteRepository.findMostPopularActivites();
        return activiteMapper.toDTOList(activites);
    }

    public ActiviteDTO updateActivite(Long id, ActiviteDTO activiteDTO) {
        log.info("Mise à jour de l'activité ID: {}", id);

        Activites existingActivite = activiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Activité non trouvée avec l'ID: " + id));

        if (activiteDTO.getVoyageId() != null
                && !existingActivite.getVoyage().getId().equals(activiteDTO.getVoyageId())) {

            Voyages newVoyage = voyageRepository.findById(activiteDTO.getVoyageId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                    "Voyage non trouvé avec l'ID: " + activiteDTO.getVoyageId()));

            if (activiteRepository.existsByNomAndVoyageId(activiteDTO.getNom(), activiteDTO.getVoyageId())) {
                throw new BusinessException(
                        "Une activité avec le nom '" + activiteDTO.getNom() + "' existe déjà pour ce voyage");
            }

            existingActivite.setVoyage(newVoyage);
        }

        activiteMapper.updateEntityFromDTO(activiteDTO, existingActivite);

        Activites updatedActivite = activiteRepository.save(existingActivite);
        log.info("Activité mise à jour avec succès. ID: {}", updatedActivite.getId());

        return activiteMapper.toDTO(updatedActivite);
    }

    public void deleteActivite(Long id) {
        log.info("Tentative de suppression de l'activité ID: {}", id);

        activiteRepository.findByIdWithReservations(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Activité non trouvée avec l'ID: " + id));

        reservationRepository.deleteReservationActivitiesByActiviteId(id);
        activiteVoyageRepository.deleteByActiviteId(id);

        activiteRepository.deleteById(id);
        log.info("Activité supprimée avec succès. ID: {}", id);
    }

    public void forceDeleteActivite(Long id) {
        log.warn("Suppression forcée de l'activité ID: {}", id);

        if (!activiteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Activité non trouvée avec l'ID: " + id);
        }

        reservationRepository.deleteReservationActivitiesByActiviteId(id);
        activiteVoyageRepository.deleteByActiviteId(id);

        activiteRepository.deleteById(id);
        log.info("Activité supprimée de force. ID: {}", id);
    }

    @Transactional(readOnly = true)
    public long countActivitesByVoyageId(Long voyageId) {
        log.info("Comptage des activités pour le voyage ID: {}", voyageId);
        return activiteRepository.countByVoyageId(voyageId);
    }

    @Transactional(readOnly = true)
    public boolean activiteExists(Long id) {
        return activiteRepository.existsById(id);
    }
}
