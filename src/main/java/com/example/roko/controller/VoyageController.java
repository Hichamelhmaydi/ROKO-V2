package com.example.roko.controller;

import com.example.roko.dto.response.VoyageDTO;
import com.example.roko.enums.VoyageStatus;
import com.example.roko.service.VoyageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voyages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VoyageController {

    private final VoyageService voyageService;


    @PostMapping
    public ResponseEntity<VoyageDTO> createVoyage(@RequestBody VoyageDTO voyageDTO) {
        try {
            VoyageDTO createdVoyage = voyageService.createVoyage(voyageDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVoyage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping
    public ResponseEntity<List<VoyageDTO>> getAllVoyages() {
        List<VoyageDTO> voyages = voyageService.getAllVoyages();
        return ResponseEntity.ok(voyages);
    }


    @GetMapping("/{id}")
    public ResponseEntity<VoyageDTO> getVoyageById(@PathVariable Long id) {
        try {
            VoyageDTO voyage = voyageService.getVoyageById(id);
            return ResponseEntity.ok(voyage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<VoyageDTO>> getVoyagesDisponibles() {
        List<VoyageDTO> voyages = voyageService.getVoyagesDisponibles();
        return ResponseEntity.ok(voyages);
    }


    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<VoyageDTO>> getVoyagesByDestination(@PathVariable String destination) {
        List<VoyageDTO> voyages = voyageService.getVoyagesByDestination(destination);
        return ResponseEntity.ok(voyages);
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<VoyageDTO>> getVoyagesByStatut(@PathVariable String statut) {
        try {
            VoyageStatus voyageStatus = VoyageStatus.valueOf(statut.toUpperCase());
            List<VoyageDTO> voyages = voyageService.getVoyagesByStatut(voyageStatus);
            return ResponseEntity.ok(voyages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<VoyageDTO>> searchVoyages(@RequestParam String query) {
        List<VoyageDTO> voyages = voyageService.searchVoyages(query);
        return ResponseEntity.ok(voyages);
    }


    @GetMapping("/nom/{nom}")
    public ResponseEntity<List<VoyageDTO>> getVoyagesByNom(@PathVariable String nom) {
        List<VoyageDTO> voyages = voyageService.getVoyagesByNom(nom);
        return ResponseEntity.ok(voyages);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<VoyageDTO>> getVoyagesByDestinationAndStatut(
            @RequestParam String destination,
            @RequestParam String statut) {
        try {
            VoyageStatus voyageStatus = VoyageStatus.valueOf(statut.toUpperCase());
            List<VoyageDTO> voyages = voyageService.getVoyagesByDestinationAndStatut(destination, voyageStatus);
            return ResponseEntity.ok(voyages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/date-depart/{dateDepart}")
    public ResponseEntity<List<VoyageDTO>> getVoyagesByDateDepart(@PathVariable String dateDepart) {
        List<VoyageDTO> voyages = voyageService.getVoyagesByDateDepart(dateDepart);
        return ResponseEntity.ok(voyages);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoyageDTO> updateVoyage(
            @PathVariable Long id,
            @RequestBody VoyageDTO voyageDTO) {
        try {
            VoyageDTO updatedVoyage = voyageService.updateVoyage(id, voyageDTO);
            return ResponseEntity.ok(updatedVoyage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<VoyageDTO> updateVoyageStatut(
            @PathVariable Long id,
            @RequestParam String statut) {
        try {
            VoyageStatus voyageStatus = VoyageStatus.valueOf(statut.toUpperCase());
            VoyageDTO updatedVoyage = voyageService.updateVoyageStatut(id, voyageStatus);
            return ResponseEntity.ok(updatedVoyage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<VoyageDTO> addPhotoToVoyage(
            @PathVariable Long id,
            @RequestBody String photoUrl) {
        try {
            VoyageDTO updatedVoyage = voyageService.addPhotoToVoyage(id, photoUrl);
            return ResponseEntity.ok(updatedVoyage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/photos")
    public ResponseEntity<VoyageDTO> removePhotoFromVoyage(
            @PathVariable Long id,
            @RequestBody String photoUrl) {
        try {
            VoyageDTO updatedVoyage = voyageService.removePhotoFromVoyage(id, photoUrl);
            return ResponseEntity.ok(updatedVoyage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoyage(@PathVariable Long id) {
        try {
            voyageService.deleteVoyage(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/stats/disponibles")
    public ResponseEntity<Long> countVoyagesDisponibles() {
        long count = voyageService.countVoyagesDisponibles();
        return ResponseEntity.ok(count);
    }


    @GetMapping("/stats/total")
    public ResponseEntity<Long> countAllVoyages() {
        long count = voyageService.countAllVoyages();
        return ResponseEntity.ok(count);
    }


    @GetMapping("/stats/statut/{statut}")
    public ResponseEntity<Long> countVoyagesByStatut(@PathVariable String statut) {
        try {
            VoyageStatus voyageStatus = VoyageStatus.valueOf(statut.toUpperCase());
            long count = voyageService.countVoyagesByStatut(voyageStatus);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}