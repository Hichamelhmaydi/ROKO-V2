package com.example.roko.service;

import com.example.roko.dto.VoyageDTO;
import com.example.roko.mapper.VoyageMapper;
import com.example.roko.entity.Voyages;
import com.example.roko.enums.VoyageStatus;
import com.example.roko.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VoyageService {

    private final VoyageRepository voyageRepository;
    private final VoyageMapper voyageMapper;


    public VoyageDTO createVoyage(VoyageDTO voyageDTO) {
        Voyages voyage = voyageMapper.toEntity(voyageDTO);
        voyage.setStatut(VoyageStatus.DISPONIBLE); // Par défaut disponible

        Voyages savedVoyage = voyageRepository.save(voyage);
        return voyageMapper.toDTO(savedVoyage);
    }


    @Transactional(readOnly = true)
    public VoyageDTO getVoyageById(Long id) {
        Voyages voyage = voyageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyage non trouvé avec l'ID: " + id));
        return voyageMapper.toDTO(voyage);
    }


    @Transactional(readOnly = true)
    public List<VoyageDTO> getAllVoyages() {
        List<Voyages> voyages = voyageRepository.findAll();
        return voyageMapper.toDTOList(voyages);
    }


    @Transactional(readOnly = true)
    public List<VoyageDTO> getVoyagesByDestination(String destination) {
        List<Voyages> voyages = voyageRepository.findByDestinationContainingIgnoreCase(destination);
        return voyageMapper.toDTOList(voyages);
    }


    @Transactional(readOnly = true)
    public List<VoyageDTO> getVoyagesByStatut(VoyageStatus statut) {
        List<Voyages> voyages = voyageRepository.findByStatutOrderByDateDepartAsc(statut);
        return voyageMapper.toDTOList(voyages);
    }


    @Transactional(readOnly = true)
    public List<VoyageDTO> getVoyagesDisponibles() {
        List<Voyages> voyages = voyageRepository.findAllByStatutOrderByDateDepartAsc(VoyageStatus.DISPONIBLE);
        return voyageMapper.toDTOList(voyages);
    }


    @Transactional(readOnly = true)
    public List<VoyageDTO> searchVoyages(String search) {
        List<Voyages> voyages = voyageRepository.searchVoyages(search);
        return voyageMapper.toDTOList(voyages);
    }


    @Transactional(readOnly = true)
    public List<VoyageDTO> getVoyagesByNom(String nom) {
        List<Voyages> voyages = voyageRepository.findByNomContainingIgnoreCase(nom);
        return voyageMapper.toDTOList(voyages);
    }


    @Transactional(readOnly = true)
    public List<VoyageDTO> getVoyagesByDestinationAndStatut(String destination, VoyageStatus statut) {
        List<Voyages> voyages = voyageRepository.findByDestinationAndStatut(destination, statut);
        return voyageMapper.toDTOList(voyages);
    }


    @Transactional(readOnly = true)
    public List<VoyageDTO> getVoyagesByDateDepart(String dateDepart) {
        List<Voyages> voyages = voyageRepository.findByDateDepartAfter(dateDepart);
        return voyageMapper.toDTOList(voyages);
    }


    public VoyageDTO updateVoyage(Long id, VoyageDTO voyageDTO) {
        Voyages existingVoyage = voyageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyage non trouvé avec l'ID: " + id));


        existingVoyage.setNom(voyageDTO.getNom());
        existingVoyage.setDescription(voyageDTO.getDescription());
        existingVoyage.setCover(voyageDTO.getCover());
        existingVoyage.setDestination(voyageDTO.getDestination());
        existingVoyage.setDateDepart(voyageDTO.getDateDepart());
        existingVoyage.setDateRetour(voyageDTO.getDateRetour());
        existingVoyage.setItineraire(voyageDTO.getItineraire());

        if (voyageDTO.getPhotos() != null) {
            existingVoyage.setPhotos(voyageDTO.getPhotos());
        }

        if (voyageDTO.getStatut() != null) {
            existingVoyage.setStatut(VoyageStatus.valueOf(voyageDTO.getStatut()));
        }

        Voyages updatedVoyage = voyageRepository.save(existingVoyage);
        return voyageMapper.toDTO(updatedVoyage);
    }


    public VoyageDTO updateVoyageStatut(Long id, VoyageStatus statut) {
        Voyages voyage = voyageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyage non trouvé avec l'ID: " + id));

        voyage.setStatut(statut);
        Voyages updatedVoyage = voyageRepository.save(voyage);
        return voyageMapper.toDTO(updatedVoyage);
    }


    public VoyageDTO addPhotoToVoyage(Long id, String photoUrl) {
        Voyages voyage = voyageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyage non trouvé avec l'ID: " + id));

        voyage.getPhotos().add(photoUrl);
        Voyages updatedVoyage = voyageRepository.save(voyage);
        return voyageMapper.toDTO(updatedVoyage);
    }


    public VoyageDTO removePhotoFromVoyage(Long id, String photoUrl) {
        Voyages voyage = voyageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyage non trouvé avec l'ID: " + id));

        voyage.getPhotos().remove(photoUrl);
        Voyages updatedVoyage = voyageRepository.save(voyage);
        return voyageMapper.toDTO(updatedVoyage);
    }


    public void deleteVoyage(Long id) {
        if (!voyageRepository.existsById(id)) {
            throw new RuntimeException("Voyage non trouvé avec l'ID: " + id);
        }
        voyageRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public long countVoyagesDisponibles() {
        return voyageRepository.countByStatut(VoyageStatus.DISPONIBLE);
    }


    @Transactional(readOnly = true)
    public long countAllVoyages() {
        return voyageRepository.count();
    }


    @Transactional(readOnly = true)
    public long countVoyagesByStatut(VoyageStatus statut) {
        return voyageRepository.countByStatut(statut);
    }
}