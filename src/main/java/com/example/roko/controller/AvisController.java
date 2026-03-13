package com.example.roko.controller;

import com.example.roko.dto.AvisDTO;
import com.example.roko.enums.AvisStatus;
import com.example.roko.security.UserPrincipal;
import com.example.roko.service.AvisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AvisController {

    private final AvisService avisService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<AvisDTO> createAvis(@Valid @RequestBody AvisDTO avisDTO,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        AvisDTO created = avisService.createAvis(avisDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/voyage/{voyageId}")
    public ResponseEntity<List<AvisDTO>> getValidatedByVoyage(@PathVariable Long voyageId) {
        return ResponseEntity.ok(avisService.getValidatedAvisByVoyage(voyageId));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<List<AvisDTO>> getMyAvis(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(avisService.getMyAvis(userId));
    }

    @GetMapping("/moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AvisDTO>> getByStatut(@RequestParam AvisStatus statut) {
        return ResponseEntity.ok(avisService.getAvisByStatut(statut));
    }

    @PutMapping("/{id}/moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AvisDTO> moderateAvis(@PathVariable Long id,
            @RequestParam AvisStatus statut) {
        return ResponseEntity.ok(avisService.moderateAvis(id, statut));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAvis(@PathVariable Long id) {
        avisService.deleteAvis(id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Impossible de récupérer l'ID de l'utilisateur");
    }
}
