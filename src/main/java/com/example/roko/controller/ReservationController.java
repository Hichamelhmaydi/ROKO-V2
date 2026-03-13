package com.example.roko.controller;

import com.example.roko.dto.response.ReservationDTO;
import com.example.roko.enums.ReservationStatut;
import com.example.roko.security.UserPrincipal;
import com.example.roko.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<ReservationDTO> createReservation(
            @Valid @RequestBody ReservationDTO reservationDTO,
            Authentication authentication) {

        log.info("Requête POST /api/reservations - Création d'une réservation");

        Long userId = getUserIdFromAuthentication(authentication);

        ReservationDTO createdReservation = reservationService.createReservation(reservationDTO, userId);
        return new ResponseEntity<>(createdReservation, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReservationDTO>> getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateReservation") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Requête GET /api/reservations - Récupération de toutes les réservations");

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ReservationDTO> reservations = reservationService.getAllReservations(pageable);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<ReservationDTO> getReservationById(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Requête GET /api/reservations/{}", id);

        Long userId = getUserIdFromAuthentication(authentication);
        boolean isAdmin = isAdmin(authentication);

        ReservationDTO reservation = reservationService.getReservationById(id, userId, isAdmin);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getMyReservations(Authentication authentication) {
        log.info("Requête GET /api/reservations/me");

        Long userId = getUserIdFromAuthentication(authentication);
        List<ReservationDTO> reservations = reservationService.getReservationsByUser(userId);

        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getReservationsByUserId(@PathVariable Long userId) {
        log.info("Requête GET /api/reservations/user/{}", userId);
        return ResponseEntity.ok(reservationService.getReservationsByUser(userId));
    }

    @GetMapping("/voyage/{voyageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getReservationsByVoyage(@PathVariable Long voyageId) {
        log.info("Requête GET /api/reservations/voyage/{}", voyageId);

        List<ReservationDTO> reservations = reservationService.getReservationsByVoyage(voyageId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getReservationsByStatut(
            @PathVariable ReservationStatut statut) {

        log.info("Requête GET /api/reservations/statut/{}", statut);

        List<ReservationDTO> reservations = reservationService.getReservationsByStatut(statut);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getReservationsEnAttente() {
        log.info("Requête GET /api/reservations/en-attente");

        List<ReservationDTO> reservations = reservationService.getReservationsByStatut(
                ReservationStatut.EN_ATTENTE);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/recentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getRecentReservations() {
        log.info("Requête GET /api/reservations/recentes");

        List<ReservationDTO> reservations = reservationService.getRecentReservations();
        return ResponseEntity.ok(reservations);
    }

    @PutMapping("/{id}/confirmer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationDTO> confirmerReservation(@PathVariable Long id) {
        log.info("Requête PUT /api/reservations/{}/confirmer", id);

        ReservationDTO reservation = reservationService.confirmerReservation(id);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<ReservationDTO> annulerReservation(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        log.info("Requête PUT /api/reservations/{}/annuler", id);

        String motif = body.get("motif");
        Long userId = getUserIdFromAuthentication(authentication);
        boolean isAdmin = isAdmin(authentication);

        ReservationDTO reservation = reservationService.annulerReservation(id, motif, userId, isAdmin);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{id}/completer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationDTO> completerReservation(@PathVariable Long id) {
        log.info("Requête PUT /api/reservations/{}/completer", id);

        ReservationDTO reservation = reservationService.completerReservation(id);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<ReservationDTO> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationDTO reservationDTO,
            Authentication authentication) {

        log.info("Requête PUT /api/reservations/{}", id);

        Long userId = getUserIdFromAuthentication(authentication);
        boolean isAdmin = isAdmin(authentication);

        ReservationDTO updatedReservation = reservationService.updateReservation(
                id, reservationDTO, userId, isAdmin);
        return ResponseEntity.ok(updatedReservation);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteReservation(@PathVariable Long id) {
        log.info("Requête DELETE /api/reservations/{}", id);

        reservationService.deleteReservation(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Réservation supprimée avec succès");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countByStatut(@PathVariable ReservationStatut statut) {
        log.info("Requête GET /api/reservations/count/statut/{}", statut);

        long count = reservationService.countReservationsByStatut(statut);

        Map<String, Long> response = new HashMap<>();
        response.put("statut", (long) statut.ordinal());
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/count")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> countMyReservations(Authentication authentication) {
        log.info("Requête GET /api/reservations/me/count");

        Long userId = getUserIdFromAuthentication(authentication);
        long count = reservationService.countReservationsByUser(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/payer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationDTO> marquerCommePaye(@PathVariable Long id) {
        log.info("Requête PUT /api/reservations/{}/payer", id);

        ReservationDTO reservation = reservationService.marquerCommePaye(id);
        return ResponseEntity.ok(reservation);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Impossible de récupérer l'ID de l'utilisateur");
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
