package com.example.roko.controller;

import com.example.roko.dto.response.ActiviteVoyageDTO;
import com.example.roko.service.ActiviteVoyageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activites-voyages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class ActiviteVoyageController {

    private final ActiviteVoyageService activiteVoyageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActiviteVoyageDTO> associerActiviteAVoyage(
            @Valid @RequestBody ActiviteVoyageDTO dto) {
        log.info("Requête POST /api/activites-voyages - Association activité-voyage");
        ActiviteVoyageDTO created = activiteVoyageService.associerActiviteAVoyage(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/voyage/{voyageId}")
    public ResponseEntity<List<ActiviteVoyageDTO>> getActivitesByVoyage(
            @PathVariable Long voyageId) {
        log.info("Requête GET /api/activites-voyages/voyage/{}", voyageId);
        List<ActiviteVoyageDTO> activites = activiteVoyageService.getActivitesByVoyage(voyageId);
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/activite/{activiteId}")
    public ResponseEntity<List<ActiviteVoyageDTO>> getVoyagesByActivite(
            @PathVariable Long activiteId) {
        log.info("Requête GET /api/activites-voyages/activite/{}", activiteId);
        List<ActiviteVoyageDTO> voyages = activiteVoyageService.getVoyagesByActivite(activiteId);
        return ResponseEntity.ok(voyages);
    }

    @GetMapping("/voyage/{voyageId}/obligatoires")
    public ResponseEntity<List<ActiviteVoyageDTO>> getActivitesObligatoires(
            @PathVariable Long voyageId) {
        log.info("Requête GET /api/activites-voyages/voyage/{}/obligatoires", voyageId);
        List<ActiviteVoyageDTO> activites = activiteVoyageService.getActivitesObligatoires(voyageId);
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/voyage/{voyageId}/optionnelles")
    public ResponseEntity<List<ActiviteVoyageDTO>> getActivitesOptionnelles(
            @PathVariable Long voyageId) {
        log.info("Requête GET /api/activites-voyages/voyage/{}/optionnelles", voyageId);
        List<ActiviteVoyageDTO> activites = activiteVoyageService.getActivitesOptionnelles(voyageId);
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/voyage/{voyageId}/jour/{jour}")
    public ResponseEntity<List<ActiviteVoyageDTO>> getActivitesByJour(
            @PathVariable Long voyageId,
            @PathVariable String jour) {
        log.info("Requête GET /api/activites-voyages/voyage/{}/jour/{}", voyageId, jour);
        List<ActiviteVoyageDTO> activites = activiteVoyageService.getActivitesByJour(voyageId, jour);
        return ResponseEntity.ok(activites);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActiviteVoyageDTO> updateAssociation(
            @PathVariable Long id,
            @Valid @RequestBody ActiviteVoyageDTO dto) {
        log.info("Requête PUT /api/activites-voyages/{}", id);
        ActiviteVoyageDTO updated = activiteVoyageService.updateAssociation(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/activite/{activiteId}/voyage/{voyageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> dissocierActiviteDeVoyage(
            @PathVariable Long activiteId,
            @PathVariable Long voyageId) {
        log.info("Requête DELETE /api/activites-voyages/activite/{}/voyage/{}",
                activiteId, voyageId);
        activiteVoyageService.dissocierActiviteDeVoyage(activiteId, voyageId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Activité dissociée du voyage avec succès");
        response.put("activiteId", activiteId.toString());
        response.put("voyageId", voyageId.toString());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAssociation(@PathVariable Long id) {
        log.info("Requête DELETE /api/activites-voyages/{}", id);
        activiteVoyageService.deleteAssociation(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Association supprimée avec succès");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/voyage/{voyageId}/count")
    public ResponseEntity<Map<String, Object>> countActivites(@PathVariable Long voyageId) {
        log.info("Requête GET /api/activites-voyages/voyage/{}/count", voyageId);

        long total = activiteVoyageService.countActivitesByVoyage(voyageId);
        long obligatoires = activiteVoyageService.countActivitesObligatoires(voyageId);
        long optionnelles = total - obligatoires;

        Map<String, Object> response = new HashMap<>();
        response.put("voyageId", voyageId);
        response.put("total", total);
        response.put("obligatoires", obligatoires);
        response.put("optionnelles", optionnelles);

        return ResponseEntity.ok(response);
    }
}
