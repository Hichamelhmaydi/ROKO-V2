package com.example.roko.controller;

import com.example.roko.dto.response.ActiviteDTO;
import com.example.roko.service.ActiviteService;
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
@RequestMapping("/api/activites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class ActiviteController {

    private final ActiviteService activiteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActiviteDTO> createActivite(@Valid @RequestBody ActiviteDTO activiteDTO) {
        log.info("Requête POST /api/activites - Création d'une activité: {}", activiteDTO.getNom());
        ActiviteDTO createdActivite = activiteService.createActivite(activiteDTO);
        return new ResponseEntity<>(createdActivite, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ActiviteDTO>> getAllActivites() {
        log.info("Requête GET /api/activites - Récupération de toutes les activités");
        List<ActiviteDTO> activites = activiteService.getAllActivites();
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActiviteDTO> getActiviteById(@PathVariable Long id) {
        log.info("Requête GET /api/activites/{} - Récupération de l'activité", id);
        ActiviteDTO activite = activiteService.getActiviteById(id);
        return ResponseEntity.ok(activite);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActiviteDTO> getActiviteByIdWithReservations(@PathVariable Long id) {
        log.info("Requête GET /api/activites/{}/details - Récupération avec réservations", id);
        ActiviteDTO activite = activiteService.getActiviteByIdWithReservations(id);
        return ResponseEntity.ok(activite);
    }

    @GetMapping("/voyage/{voyageId}")
    public ResponseEntity<List<ActiviteDTO>> getActivitesByVoyageId(@PathVariable Long voyageId) {
        log.info("Requête GET /api/activites/voyage/{} - Récupération des activités du voyage", voyageId);
        List<ActiviteDTO> activites = activiteService.getActivitesByVoyageId(voyageId);
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/voyage/{voyageId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActiviteDTO>> getActivitesByVoyageIdWithReservations(@PathVariable Long voyageId) {
        log.info("Requête GET /api/activites/voyage/{}/details - Avec réservations", voyageId);
        List<ActiviteDTO> activites = activiteService.getActivitesByVoyageIdWithReservations(voyageId);
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ActiviteDTO>> searchActivitesByNom(@RequestParam String nom) {
        log.info("Requête GET /api/activites/search?nom={}", nom);
        List<ActiviteDTO> activites = activiteService.searchActivitesByNom(nom);
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/search/description")
    public ResponseEntity<List<ActiviteDTO>> searchActivitesByDescription(@RequestParam String keyword) {
        log.info("Requête GET /api/activites/search/description?keyword={}", keyword);
        List<ActiviteDTO> activites = activiteService.searchActivitesByDescription(keyword);
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/populaires")
    public ResponseEntity<List<ActiviteDTO>> getMostPopularActivites() {
        log.info("Requête GET /api/activites/populaires - Activités les plus populaires");
        List<ActiviteDTO> activites = activiteService.getMostPopularActivites();
        return ResponseEntity.ok(activites);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActiviteDTO> updateActivite(
            @PathVariable Long id,
            @Valid @RequestBody ActiviteDTO activiteDTO) {
        log.info("Requête PUT /api/activites/{} - Mise à jour de l'activité", id);
        ActiviteDTO updatedActivite = activiteService.updateActivite(id, activiteDTO);
        return ResponseEntity.ok(updatedActivite);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteActivite(@PathVariable Long id) {
        log.info("Requête DELETE /api/activites/{} - Suppression de l'activité", id);
        activiteService.deleteActivite(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Activité supprimée avec succès");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/force")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> forceDeleteActivite(@PathVariable Long id) {
        log.warn("Requête DELETE /api/activites/{}/force - Suppression forcée", id);
        activiteService.forceDeleteActivite(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Activité supprimée de force avec succès");
        response.put("id", id.toString());
        response.put("warning", "Les réservations associées peuvent être affectées");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/voyage/{voyageId}/count")
    public ResponseEntity<Map<String, Long>> countActivitesByVoyageId(@PathVariable Long voyageId) {
        log.info("Requête GET /api/activites/voyage/{}/count", voyageId);
        long count = activiteService.countActivitesByVoyageId(voyageId);

        Map<String, Long> response = new HashMap<>();
        response.put("voyageId", voyageId);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Map<String, Boolean>> activiteExists(@PathVariable Long id) {
        log.info("Requête GET /api/activites/{}/exists", id);
        boolean exists = activiteService.activiteExists(id);

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }
}
