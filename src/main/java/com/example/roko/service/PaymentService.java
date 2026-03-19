package com.example.roko.service;

import com.example.roko.dto.response.PaymentDTO;
import com.example.roko.entity.Payment;
import com.example.roko.entity.Reservations;
import com.example.roko.enums.PaymentStatus;
import com.example.roko.enums.ReservationStatut;
import com.example.roko.exception.BusinessException;
import com.example.roko.exception.ResourceNotFoundException;
import com.example.roko.mapper.PaymentMapper;
import com.example.roko.repository.PaymentRepository;
import com.example.roko.repository.ReservationRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final StripeService stripeService;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;


    public Map<String, String> createPaymentSession(Long reservationId, Long userId) {
        log.info("Création d'une session de paiement pour la réservation {} par l'utilisateur {}",
                reservationId, userId);

        // Vérifier que la réservation existe
        Reservations reservation = reservationRepository.findByIdWithDetails(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation non trouvée avec l'ID: " + reservationId));

        // Vérifier que l'utilisateur est propriétaire de la réservation
        if (!reservation.getVoyageur().getId().equals(userId)) {
            throw new BusinessException("Vous n'avez pas accès à cette réservation");
        }

        // Vérifier que la réservation n'est pas déjà payée
        if (reservation.getPaiementEffectue()) {
            throw new BusinessException("Cette réservation est déjà payée");
        }

        // Vérifier qu'il n'y a pas déjà un paiement en attente
        if (paymentRepository.hasUserPendingPayment(userId)) {
            throw new BusinessException("Vous avez déjà un paiement en attente");
        }

        try {
            // Créer la session Stripe
            Session session = stripeService.createCheckoutSession(
                    reservationId,
                    reservation.getMontantTotal().doubleValue(),
                    "EUR",
                    reservation.getVoyageur().getEmail(),
                    "Voyage " + reservation.getVoyage().getNom() + " pour " +
                            reservation.getNombrePersonnes() + " personne(s)"
            );

            // Créer l'entité Payment
            Payment payment = new Payment();
            payment.setStripeSessionId(session.getId());
            payment.setAmount(reservation.getMontantTotal().doubleValue());
            payment.setStatus(PaymentStatus.EN_ATTENTE);
            payment.setUserId(userId);
            payment.setReservation(reservation);
            payment.setDateCreation(LocalDateTime.now());

            paymentRepository.save(payment);
            log.info("Session de paiement créée avec succès. Session ID: {}", session.getId());

            // Envoyer une notification
            notificationService.envoyerNotificationPaiementCree(userId, reservationId);

            // Retourner l'URL de la session
            Map<String, String> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("sessionUrl", session.getUrl());
            response.put("paymentId", payment.getId().toString());

            return response;

        } catch (StripeException e) {
            log.error("Erreur lors de la création de la session Stripe", e);
            throw new BusinessException("Erreur lors de la création du paiement: " + e.getMessage());
        }
    }


    public PaymentDTO confirmPayment(String sessionId) {
        log.info("Confirmation du paiement pour la session {}", sessionId);

        // Récupérer le paiement
        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paiement non trouvé pour la session: " + sessionId));

        try {
            // Vérifier le statut de la session Stripe
            String stripeStatus = stripeService.getSessionStatus(sessionId);

            if ("paid".equals(stripeStatus)) {
                // Mettre à jour le paiement
                payment.setStatus(PaymentStatus.REUSSI);
                payment.setDatePaiement(LocalDateTime.now());

                // Mettre à jour la réservation
                Reservations reservation = payment.getReservation();
                reservation.setPaiementEffectue(true);
                reservation.setDatePaiement(LocalDateTime.now());
                reservation.setStatut(ReservationStatut.PAYEE);

                paymentRepository.save(payment);
                reservationRepository.save(reservation);

                log.info("Paiement confirmé avec succès pour la session {}", sessionId);

                // Envoyer une notification
                notificationService.envoyerNotificationPaiementReussi(
                        payment.getUserId(),
                        reservation.getId());

                return paymentMapper.toDTO(payment);

            } else {
                throw new BusinessException("Le paiement n'est pas encore complété");
            }

        } catch (StripeException e) {
            log.error("Erreur lors de la vérification du paiement Stripe", e);
            throw new BusinessException("Erreur lors de la confirmation du paiement: " + e.getMessage());
        }
    }


    public PaymentDTO handlePaymentFailure(String sessionId, String reason) {
        log.info("Traitement de l'échec du paiement pour la session {}", sessionId);

        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paiement non trouvé pour la session: " + sessionId));

        payment.setStatus(PaymentStatus.ECHOUE);
        Payment updatedPayment = paymentRepository.save(payment);

        // Envoyer une notification
        notificationService.envoyerNotificationPaiementEchoue(
                payment.getUserId(),
                payment.getReservation().getId(),
                reason);

        log.info("Échec du paiement enregistré pour la session {}", sessionId);

        return paymentMapper.toDTO(updatedPayment);
    }

    public PaymentDTO cancelPayment(Long paymentId, Long userId, boolean isAdmin) {
        log.info("Annulation du paiement {} par l'utilisateur {}", paymentId, userId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paiement non trouvé avec l'ID: " + paymentId));

        // Vérifier les permissions
        if (!isAdmin && !payment.getUserId().equals(userId)) {
            throw new BusinessException("Vous n'avez pas le droit d'annuler ce paiement");
        }

        if (payment.getStatus() == PaymentStatus.REUSSI) {
            throw new BusinessException("Un paiement réussi ne peut pas être annulé. Utilisez le remboursement.");
        }

        payment.setStatus(PaymentStatus.ANNULE);
        Payment updatedPayment = paymentRepository.save(payment);

        log.info("Paiement annulé avec succès. ID: {}", paymentId);

        return paymentMapper.toDTO(updatedPayment);
    }

    public Map<String, Object> createRefund(Long paymentId, Double amount, String reason) {
        log.info("Création d'un remboursement pour le paiement {}", paymentId);

        Payment payment = paymentRepository.findByIdWithReservation(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paiement non trouvé avec l'ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.REUSSI) {
            throw new BusinessException("Seuls les paiements réussis peuvent être remboursés");
        }

        try {
            // Récupérer le Payment Intent ID
            String paymentIntentId = stripeService.getPaymentIntentId(payment.getStripeSessionId());

            // Créer le remboursement sur Stripe
            Refund refund = stripeService.createRefund(paymentIntentId, amount, reason);

            // Mettre à jour le paiement
            payment.setStatus(PaymentStatus.REMBOURSE);
            paymentRepository.save(payment);

            // Mettre à jour la réservation
            Reservations reservation = payment.getReservation();
            reservation.setStatut(ReservationStatut.ANNULEE);
            reservationRepository.save(reservation);

            log.info("Remboursement créé avec succès. Refund ID: {}", refund.getId());

            notificationService.envoyerNotificationRemboursement(
                    payment.getUserId(),
                    reservation.getId(),
                    amount != null ? amount : payment.getAmount());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Remboursement effectué avec succès");
            response.put("refundId", refund.getId());
            response.put("amount", refund.getAmount() / 100.0);
            response.put("status", refund.getStatus());

            return response;

        } catch (StripeException e) {
            log.error("Erreur lors de la création du remboursement Stripe", e);
            throw new BusinessException("Erreur lors du remboursement: " + e.getMessage());
        }
    }


    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPayments() {
        log.info("Récupération de tous les paiements");
        List<Payment> payments = paymentRepository.findAll();
        return paymentMapper.toDTOList(payments);
    }


    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id, Long userId, boolean isAdmin) {
        log.info("Récupération du paiement {}", id);

        Payment payment = paymentRepository.findByIdWithReservation(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paiement non trouvé avec l'ID: " + id));

        if (!isAdmin && !payment.getUserId().equals(userId)) {
            throw new BusinessException("Vous n'avez pas accès à ce paiement");
        }

        return paymentMapper.toDTO(payment);
    }


    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByUser(Long userId) {
        log.info("Récupération des paiements de l'utilisateur {}", userId);
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return paymentMapper.toDTOList(payments);
    }


    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByReservation(Long reservationId, Long userId, boolean isAdmin) {
        log.info("Récupération du paiement de la réservation {}", reservationId);

        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun paiement trouvé pour la réservation: " + reservationId));

        if (!isAdmin && !payment.getUserId().equals(userId)) {
            throw new BusinessException("Vous n'avez pas accès à ce paiement");
        }

        return paymentMapper.toDTO(payment);
    }


    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        log.info("Récupération des paiements avec le statut {}", status);
        List<Payment> payments = paymentRepository.findByStatus(status);
        return paymentMapper.toDTOList(payments);
    }


    @Transactional(readOnly = true)
    public Double calculateTotalRevenue() {
        log.info("Calcul du chiffre d'affaires total");
        Double total = paymentRepository.calculateTotalSuccessfulPayments();
        return total != null ? total : 0.0;
    }


    @Transactional(readOnly = true)
    public Double calculateRevenueBetween(LocalDateTime debut, LocalDateTime fin) {
        log.info("Calcul du chiffre d'affaires entre {} et {}", debut, fin);
        Double total = paymentRepository.calculateTotalRevenueBetween(debut, fin);
        return total != null ? total : 0.0;
    }

    public void cleanExpiredPayments() {
        log.info("Nettoyage des paiements expirés");

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Payment> expiredPayments = paymentRepository.findExpiredPendingPayments(oneHourAgo);

        for (Payment payment : expiredPayments) {
            try {
                if (stripeService.isSessionExpired(payment.getStripeSessionId())) {
                    payment.setStatus(PaymentStatus.ANNULE);
                    paymentRepository.save(payment);
                    log.info("Paiement expiré marqué comme annulé. ID: {}", payment.getId());
                }
            } catch (StripeException e) {
                log.error("Erreur lors de la vérification de l'expiration du paiement {}",
                        payment.getId(), e);
            }
        }
    }

    /**
     * Traite evenement checkout.session.completed de Stripe
     * Appele automatiquement par le webhook quand le paiement est complete
     */
    public void handleCheckoutSessionCompleted(String sessionId) {
        log.info("Webhook: Traitement du checkout.session.completed pour la session {}", sessionId);

        Payment payment = paymentRepository.findByStripeSessionId(sessionId).orElse(null);

        if (payment == null) {
            log.warn("Webhook: Aucun paiement trouve pour la session {}", sessionId);
            return;
        }

        if (payment.getStatus() == PaymentStatus.REUSSI) {
            log.info("Webhook: Paiement deja marque comme reussi pour la session {}", sessionId);
            return;
        }

        payment.setStatus(PaymentStatus.REUSSI);
        payment.setDatePaiement(LocalDateTime.now());

        Reservations reservation = payment.getReservation();
        if (reservation != null) {
            reservation.setPaiementEffectue(true);
            reservation.setDatePaiement(LocalDateTime.now());
            reservation.setStatut(ReservationStatut.PAYEE);
            reservationRepository.save(reservation);

            notificationService.envoyerNotificationPaiementReussi(
                    payment.getUserId(),
                    reservation.getId());
        }

        paymentRepository.save(payment);
        log.info("Webhook: Paiement confirme avec succes pour la session {}", sessionId);
    }

    /**
     * Traite evenement checkout.session.expired de Stripe
     */
    public void handleCheckoutSessionExpired(String sessionId) {
        log.info("Webhook: Traitement du checkout.session.expired pour la session {}", sessionId);

        Payment payment = paymentRepository.findByStripeSessionId(sessionId).orElse(null);

        if (payment == null) {
            log.warn("Webhook: Aucun paiement trouve pour la session {}", sessionId);
            return;
        }

        if (payment.getStatus() == PaymentStatus.EN_ATTENTE) {
            payment.setStatus(PaymentStatus.ANNULE);
            paymentRepository.save(payment);

            Reservations reservation = payment.getReservation();
            if (reservation != null) {
                reservation.setStatut(ReservationStatut.EN_ATTENTE);
                reservationRepository.save(reservation);
            }

            log.info("Webhook: Session expiree, paiement annule pour la session {}", sessionId);
        }
    }

    /**
     * Traite evenement payment_intent.payment_failed de Stripe
     */
    public void handlePaymentIntentFailed(String sessionId, String failureMessage) {
        log.info("Webhook: Traitement du payment_intent.payment_failed pour la session {}", sessionId);

        Payment payment = paymentRepository.findByStripeSessionId(sessionId).orElse(null);

        if (payment == null) {
            log.warn("Webhook: Aucun paiement trouve pour la session {}", sessionId);
            return;
        }

        payment.setStatus(PaymentStatus.ECHOUE);
        paymentRepository.save(payment);

        Reservations reservation = payment.getReservation();
        if (reservation != null) {
            notificationService.envoyerNotificationPaiementEchoue(
                    payment.getUserId(),
                    reservation.getId(),
                    failureMessage != null ? failureMessage : "Echec du paiement");
        }

        log.info("Webhook: Paiement marque comme echoue pour la session {}", sessionId);
    }

    /**
     * Traite evenement charge.refunded de Stripe
     */
    public void handleChargeRefunded(String paymentIntentId) {
        log.info("Webhook: Traitement du charge.refunded pour le payment_intent {}", paymentIntentId);

        List<Payment> allPayments = paymentRepository.findAll();
        Payment payment = null;

        for (Payment p : allPayments) {
            try {
                String intentId = stripeService.getPaymentIntentId(p.getStripeSessionId());
                if (paymentIntentId.equals(intentId)) {
                    payment = p;
                    break;
                }
            } catch (StripeException e) {
                log.debug("Impossible de recuperer le payment_intent pour la session {}", p.getStripeSessionId());
            }
        }

        if (payment == null) {
            log.warn("Webhook: Aucun paiement trouve pour le payment_intent {}", paymentIntentId);
            return;
        }

        if (payment.getStatus() == PaymentStatus.REMBOURSE) {
            log.info("Webhook: Paiement deja marque comme rembourse");
            return;
        }

        payment.setStatus(PaymentStatus.REMBOURSE);
        paymentRepository.save(payment);

        Reservations reservation = payment.getReservation();
        if (reservation != null) {
            reservation.setStatut(ReservationStatut.ANNULEE);
            reservationRepository.save(reservation);

            notificationService.envoyerNotificationRemboursement(
                    payment.getUserId(),
                    reservation.getId(),
                    payment.getAmount());
        }

        log.info("Webhook: Paiement marque comme rembourse pour le payment_intent {}", paymentIntentId);
    }
}
