package com.example.roko.service;

import com.example.roko.dto.response.NotificationDTO;
import com.example.roko.entity.Notifications;
import com.example.roko.entity.User;
import com.example.roko.enums.NotificationType;
import com.example.roko.exception.ResourceNotFoundException;
import com.example.roko.repository.NotificationRepository;
import com.example.roko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private void createNotification(Long userId, String titre, String message, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable pour la notification"));

        Notifications notification = new Notifications();
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setType(type);
        notification.setLu(false);
        notification.setUser(user);

        notificationRepository.save(notification);
    }

    public void envoyerNotificationPaiementCree(Long userId, Long reservationId) {
        log.info("Notification: Paiement créé pour l'utilisateur {} - Réservation {}",
                userId, reservationId);
        createNotification(
                userId,
                "Paiement en attente",
                "Votre paiement pour la réservation " + reservationId + " a été initié.",
                NotificationType.GENERAL
        );
    }

    public void envoyerNotificationPaiementReussi(Long userId, Long reservationId) {
        log.info("Notification: Paiement réussi pour l'utilisateur {} - Réservation {}",
                userId, reservationId);
        createNotification(
                userId,
                "Paiement confirmé",
                "Le paiement de votre réservation " + reservationId + " a été confirmé.",
                NotificationType.PAIEMENT_REUSSI
        );
    }

    public void envoyerNotificationPaiementEchoue(Long userId, Long reservationId, String reason) {
        log.info("Notification: Paiement échoué pour l'utilisateur {} - Réservation {} - Raison: {}",
                userId, reservationId, reason);
        createNotification(
                userId,
                "Paiement échoué",
                "Le paiement de votre réservation " + reservationId + " a échoué. Motif: " + reason,
                NotificationType.PAIEMENT_ECHEC
        );
    }

    public void envoyerNotificationRemboursement(Long userId, Long reservationId, Double amount) {
        log.info("Notification: Remboursement de {} EUR pour l'utilisateur {} - Réservation {}",
                amount, userId, reservationId);
        createNotification(
                userId,
                "Remboursement effectué",
                "Un remboursement de " + amount + " EUR a été effectué pour la réservation " + reservationId + ".",
                NotificationType.GENERAL
        );
    }

    public void envoyerNotificationReservationCreee(Long userId, Long reservationId) {
        createNotification(
                userId,
                "Réservation créée",
                "Votre réservation " + reservationId + " a bien été créée.",
                NotificationType.RESERVATION_CREEE
        );
    }

    public void envoyerNotificationReservationAnnulee(Long userId, Long reservationId) {
        createNotification(
                userId,
                "Réservation annulée",
                "Votre réservation " + reservationId + " a été annulée.",
                NotificationType.RESERVATION_ANNULEE
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyNotifications(Long userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(n -> new NotificationDTO(
                n.getId(),
                n.getTitre(),
                n.getMessage(),
                n.getLu(),
                n.getDateCreation(),
                n.getType()
        ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void markAsRead(Long userId, Long notificationId) {
        Notifications notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Accès non autorisé à cette notification");
        }

        notification.setLu(true);
        notificationRepository.save(notification);
    }
}
