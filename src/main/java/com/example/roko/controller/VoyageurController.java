package com.example.roko.controller;

import com.example.roko.dto.response.VoyageurDTO;
import com.example.roko.enums.CompteStatus;
import com.example.roko.service.VoyageurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voyageurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VoyageurController {

    private final VoyageurService voyageurService;

    @PostMapping
    public ResponseEntity<VoyageurDTO> createVoyageur(@RequestBody VoyageurDTO voyageurDTO) {
        try {
            VoyageurDTO createdVoyageur = voyageurService.createVoyageur(voyageurDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVoyageur);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<VoyageurDTO>> getAllVoyageurs() {
        List<VoyageurDTO> voyageurs = voyageurService.getAllVoyageurs();
        return ResponseEntity.ok(voyageurs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoyageurDTO> getVoyageurById(@PathVariable Long id) {
        try {
            VoyageurDTO voyageur = voyageurService.getVoyageurById(id);
            return ResponseEntity.ok(voyageur);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<VoyageurDTO> getVoyageurByEmail(@PathVariable String email) {
        try {
            VoyageurDTO voyageur = voyageurService.getVoyageurByEmail(email);
            return ResponseEntity.ok(voyageur);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<VoyageurDTO>> getVoyageursByStatus(@PathVariable String status) {
        try {
            CompteStatus compteStatus = CompteStatus.valueOf(status.toUpperCase());
            List<VoyageurDTO> voyageurs = voyageurService.getVoyageursByStatus(compteStatus);
            return ResponseEntity.ok(voyageurs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<VoyageurDTO>> searchVoyageurs(@RequestParam String query) {
        List<VoyageurDTO> voyageurs = voyageurService.searchVoyageurs(query);
        return ResponseEntity.ok(voyageurs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoyageurDTO> updateVoyageur(
            @PathVariable Long id,
            @RequestBody VoyageurDTO voyageurDTO) {
        try {
            VoyageurDTO updatedVoyageur = voyageurService.updateVoyageur(id, voyageurDTO);
            return ResponseEntity.ok(updatedVoyageur);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<VoyageurDTO> toggleVoyageurStatus(@PathVariable Long id) {
        try {
            VoyageurDTO updatedVoyageur = voyageurService.toggleVoyageurStatus(id);
            return ResponseEntity.ok(updatedVoyageur);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoyageur(@PathVariable Long id) {
        try {
            voyageurService.deleteVoyageur(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoyageurDTO> blockVoyageur(@PathVariable Long id) {
        return ResponseEntity.ok(voyageurService.setBlocked(id, true));
    }

    @PatchMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoyageurDTO> unblockVoyageur(@PathVariable Long id) {
        return ResponseEntity.ok(voyageurService.setBlocked(id, false));
    }

    @GetMapping("/stats/active")
    public ResponseEntity<Long> countActiveVoyageurs() {
        long count = voyageurService.countActiveVoyageurs();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/total")
    public ResponseEntity<Long> countAllVoyageurs() {
        long count = voyageurService.countAllVoyageurs();
        return ResponseEntity.ok(count);
    }
}
