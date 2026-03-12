package com.example.roko.controller;

import com.example.roko.dto.PaymentDTO;
import com.example.roko.enums.PaymentStatus;
import com.example.roko.security.UserPrincipal;
import com.example.roko.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-session")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> createPaymentSession(
            @RequestBody Map<String, Long> request,
            Authentication authentication) {

        log.info("Requête POST /api/paiements/create-session");

        Long reservationId = request.get("reservationId");
        Long userId = getUserIdFromAuthentication(authentication);

        Map<String, String> response = paymentService.createPaymentSession(reservationId, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentDTO> confirmPayment(@RequestBody Map<String, String> request) {
        log.info("Requête POST /api/paiements/confirm");

        String sessionId = request.get("sessionId");
        PaymentDTO payment = paymentService.confirmPayment(sessionId);

        return ResponseEntity.ok(payment);
    }


    @PostMapping("/failure")
    public ResponseEntity<PaymentDTO> handlePaymentFailure(@RequestBody Map<String, String> request) {
        log.info("Requête POST /api/paiements/failure");

        String sessionId = request.get("sessionId");
        String reason = request.get("reason");

        PaymentDTO payment = paymentService.handlePaymentFailure(sessionId, reason);
        return ResponseEntity.ok(payment);
    }


    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Webhook Stripe reçu");



        return ResponseEntity.ok("Webhook reçu");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        log.info("Requête GET /api/paiements");

        List<PaymentDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<PaymentDTO> getPaymentById(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Requête GET /api/paiements/{}", id);

        Long userId = getUserIdFromAuthentication(authentication);
        boolean isAdmin = isAdmin(authentication);

        PaymentDTO payment = paymentService.getPaymentById(id, userId, isAdmin);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<List<PaymentDTO>> getMyPayments(Authentication authentication) {
        log.info("Requête GET /api/paiements/me");

        Long userId = getUserIdFromAuthentication(authentication);
        List<PaymentDTO> payments = paymentService.getPaymentsByUser(userId);

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<PaymentDTO> getPaymentByReservation(
            @PathVariable Long reservationId,
            Authentication authentication) {

        log.info("Requête GET /api/paiements/reservation/{}", reservationId);

        Long userId = getUserIdFromAuthentication(authentication);
        boolean isAdmin = isAdmin(authentication);

        PaymentDTO payment = paymentService.getPaymentByReservation(reservationId, userId, isAdmin);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(@PathVariable PaymentStatus statut) {
        log.info("Requête GET /api/paiements/statut/{}", statut);

        List<PaymentDTO> payments = paymentService.getPaymentsByStatus(statut);
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<PaymentDTO> cancelPayment(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Requête PUT /api/paiements/{}/annuler", id);

        Long userId = getUserIdFromAuthentication(authentication);
        boolean isAdmin = isAdmin(authentication);

        PaymentDTO payment = paymentService.cancelPayment(id, userId, isAdmin);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/rembourser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createRefund(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        log.info("Requête POST /api/paiements/{}/rembourser", id);

        Double amount = request.get("amount") != null ?
                Double.valueOf(request.get("amount").toString()) : null;
        String reason = (String) request.get("reason");

        Map<String, Object> response = paymentService.createRefund(id, amount, reason);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/statistiques/chiffre-affaires")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Double>> getTotalRevenue() {
        log.info("Requête GET /api/paiements/statistiques/chiffre-affaires");

        Double total = paymentService.calculateTotalRevenue();

        Map<String, Double> response = new HashMap<>();
        response.put("total", total);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/statistiques/chiffre-affaires/periode")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRevenueBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        log.info("Requête GET /api/paiements/statistiques/chiffre-affaires/periode");

        Double total = paymentService.calculateRevenueBetween(debut, fin);

        Map<String, Object> response = new HashMap<>();
        response.put("debut", debut);
        response.put("fin", fin);
        response.put("total", total);

        return ResponseEntity.ok(response);
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