package com.example.roko.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {


    public void envoyerNotificationPaiementCree(Long userId, Long reservationId) {
        log.info("Notification: Paiement créé pour l'utilisateur {} - Réservation {}",
                userId, reservationId);

    }


    public void envoyerNotificationPaiementReussi(Long userId, Long reservationId) {
        log.info("Notification: Paiement réussi pour l'utilisateur {} - Réservation {}",
                userId, reservationId);

    }

    public void envoyerNotificationPaiementEchoue(Long userId, Long reservationId, String reason) {
        log.info("Notification: Paiement échoué pour l'utilisateur {} - Réservation {} - Raison: {}",
                userId, reservationId, reason);

    }

    public void envoyerNotificationRemboursement(Long userId, Long reservationId, Double amount) {
        log.info("Notification: Remboursement de {} EUR pour l'utilisateur {} - Réservation {}",
                amount, userId, reservationId);

    }
}