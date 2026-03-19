package com.example.roko.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Value("${stripe.success.url:http://localhost:4200/payment/success}")
    private String successUrl;

    @Value("${stripe.cancel.url:http://localhost:4200/payment/cancel}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            log.warn("Clé Stripe absente: les endpoints de paiement Stripe seront indisponibles tant que stripe.api.key n'est pas configurée");
            return;
        }

        Stripe.apiKey = stripeApiKey;
        log.info("Stripe API initialisée avec succès");

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook secret Stripe non configuré: la validation des signatures webhook sera désactivée");
        }
    }

    /**
     * Valide la signature du webhook Stripe et retourne l'événement
     */
    public Event constructWebhookEvent(String payload, String sigHeader) throws SignatureVerificationException {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook secret non configuré, parsing de l'événement sans validation de signature");
            return Event.GSON.fromJson(payload, Event.class);
        }

        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }

    /**
     * Vérifie si le webhook secret est configuré
     */
    public boolean isWebhookSecretConfigured() {
        return webhookSecret != null && !webhookSecret.isBlank();
    }

    public Session createCheckoutSession(
            Long reservationId,
            Double amount,
            String currency,
            String customerEmail,
            String description) throws StripeException {

        log.info("Création d'une session Stripe pour la réservation {}", reservationId);

        long amountInCents = (long) (amount * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .setCustomerEmail(customerEmail)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency.toLowerCase())
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Réservation ROKO #" + reservationId)
                                                                .setDescription(description)
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .putMetadata("reservationId", reservationId.toString())
                .setExpiresAt(System.currentTimeMillis() / 1000 + 3600)
                .build();

        Session session = Session.create(params);
        log.info("Session Stripe créée avec succès. Session ID: {}", session.getId());

        return session;
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        log.info("Récupération de la session Stripe {}", sessionId);
        return Session.retrieve(sessionId);
    }

    public String getSessionStatus(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        return session.getPaymentStatus();
    }

    public Refund createRefund(String paymentIntentId, Double amount, String reason) throws StripeException {
        log.info("Création d'un remboursement pour le paiement {}", paymentIntentId);

        Long amountInCents = amount != null ? (long) (amount * 100) : null;

        RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);

        if (amountInCents != null) {
            paramsBuilder.setAmount(amountInCents);
        }

        Refund refund = Refund.create(paramsBuilder.build());
        log.info("Remboursement créé avec succès. Refund ID: {}", refund.getId());

        return refund;
    }

    public boolean isSessionExpired(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        Long expiresAt = session.getExpiresAt();

        if (expiresAt == null) {
            return false;
        }

        return System.currentTimeMillis() / 1000 > expiresAt;
    }

    public Map<String, String> getSessionMetadata(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        return session.getMetadata() != null ? session.getMetadata() : new HashMap<>();
    }

    public String getPaymentIntentId(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        return session.getPaymentIntent();
    }
}
