package com.example.roko.service;

import com.example.roko.dto.VoyageurDTO;
import com.example.roko.entity.Voyageurs;
import com.example.roko.enums.CompteStatus;
import com.example.roko.mapper.VoyageurMapper;
import com.example.roko.repository.VoyageurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VoyageurService {

    private final VoyageurRepository voyageurRepository;
    private final VoyageurMapper voyageurMapper;

    public VoyageurDTO createVoyageur(VoyageurDTO voyageurDTO) {
        if (voyageurRepository.existsByEmail(voyageurDTO.getEmail())) {
            throw new RuntimeException("Un voyageur avec cet email existe déjà");
        }

        Voyageurs voyageur = voyageurMapper.toEntity(voyageurDTO);
        voyageur.setStatus(CompteStatus.ACTIVER);
        Voyageurs savedVoyageur = voyageurRepository.save(voyageur);
        return voyageurMapper.toDTO(savedVoyageur);
    }

    @Transactional(readOnly = true)
    public VoyageurDTO getVoyageurById(Long id) {
        Voyageurs voyageur = voyageurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyageur non trouvé avec l'ID: " + id));
        return voyageurMapper.toDTO(voyageur);
    }

    @Transactional(readOnly = true)
    public List<VoyageurDTO> getAllVoyageurs() {
        List<Voyageurs> voyageurs = voyageurRepository.findAll();
        return voyageurMapper.toDTOList(voyageurs);
    }

    @Transactional(readOnly = true)
    public VoyageurDTO getVoyageurByEmail(String email) {
        Voyageurs voyageur = voyageurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Voyageur non trouvé avec l'email: " + email));
        return voyageurMapper.toDTO(voyageur);
    }

    @Transactional(readOnly = true)
    public List<VoyageurDTO> getVoyageursByStatus(CompteStatus status) {
        List<Voyageurs> voyageurs = voyageurRepository.findByStatus(status);
        return voyageurMapper.toDTOList(voyageurs);
    }

    @Transactional(readOnly = true)
    public List<VoyageurDTO> searchVoyageurs(String search) {
        List<Voyageurs> voyageurs = voyageurRepository.searchByNomOrPrenom(search);
        return voyageurMapper.toDTOList(voyageurs);
    }

    public VoyageurDTO updateVoyageur(Long id, VoyageurDTO voyageurDTO) {
        Voyageurs existingVoyageur = voyageurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyageur non trouvé avec l'ID: " + id));

        if (!existingVoyageur.getEmail().equals(voyageurDTO.getEmail())
                && voyageurRepository.existsByEmail(voyageurDTO.getEmail())) {
            throw new RuntimeException("Un voyageur avec cet email existe déjà");
        }

        existingVoyageur.setNom(voyageurDTO.getNom());
        existingVoyageur.setPrenom(voyageurDTO.getPrenom());
        existingVoyageur.setEmail(voyageurDTO.getEmail());
        existingVoyageur.setTelephone(voyageurDTO.getTelephone());
        existingVoyageur.setIdNational(voyageurDTO.getIdNational());
        existingVoyageur.setDateExpiration(voyageurDTO.getDateExpiration());

        if (voyageurDTO.getStatus() != null) {
            existingVoyageur.setStatus(CompteStatus.valueOf(voyageurDTO.getStatus()));
        }

        Voyageurs updatedVoyageur = voyageurRepository.save(existingVoyageur);
        return voyageurMapper.toDTO(updatedVoyageur);
    }

    public VoyageurDTO toggleVoyageurStatus(Long id) {
        Voyageurs voyageur = voyageurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyageur non trouvé avec l'ID: " + id));

        if (voyageur.getStatus() == CompteStatus.ACTIVER) {
            voyageur.setStatus(CompteStatus.DESACTIVER);
        } else {
            voyageur.setStatus(CompteStatus.ACTIVER);
        }

        Voyageurs updatedVoyageur = voyageurRepository.save(voyageur);
        return voyageurMapper.toDTO(updatedVoyageur);
    }

    public VoyageurDTO setBlocked(Long id, boolean blocked) {
        Voyageurs voyageur = voyageurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyageur non trouvé avec l'ID: " + id));

        voyageur.setBloque(blocked);
        if (blocked) {
            voyageur.setActif(false);
            voyageur.setStatus(CompteStatus.DESACTIVER);
        }

        Voyageurs updatedVoyageur = voyageurRepository.save(voyageur);
        return voyageurMapper.toDTO(updatedVoyageur);
    }

    public void deleteVoyageur(Long id) {
        if (!voyageurRepository.existsById(id)) {
            throw new RuntimeException("Voyageur non trouvé avec l'ID: " + id);
        }
        voyageurRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countActiveVoyageurs() {
        return voyageurRepository.countByStatus(CompteStatus.ACTIVER);
    }

    @Transactional(readOnly = true)
    public long countAllVoyageurs() {
        return voyageurRepository.count();
    }
}
