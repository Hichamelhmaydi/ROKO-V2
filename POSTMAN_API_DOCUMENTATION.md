# Documentation API ROKO pour Postman

## Base URL

L'application démarre actuellement en local sur:

```text
http://localhost:8080
```

Le projet expose des endpoints `HTTP`, pas `HTTPS`.

## Variables Postman conseillées

Créer un environnement Postman avec:

```text
BASE_URL = http://localhost:8080
TOKEN_VOYAGEUR =
TOKEN_ADMIN =
VOYAGE_ID =
ACTIVITE_ID =
ACTIVITE_VOYAGE_ID =
RESERVATION_ID =
PAYMENT_ID =
USER_ID =
VOYAGEUR_ID =
AVIS_ID =
NOTIFICATION_ID =
```

## Headers à utiliser

Pour les routes protégées:

```http
Authorization: Bearer {{TOKEN_VOYAGEUR}}
```

ou:

```http
Authorization: Bearer {{TOKEN_ADMIN}}
```

Pour les requêtes JSON:

```http
Content-Type: application/json
Accept: application/json
```

## Règles d'accès actuelles

### Public

- `/api/auth/login`
- `/api/auth/register`
- `/api/voyages/**`
- `/api/activites/**`
- `/api/avis/voyage/**` (avis validés d'un voyage)

### Admin seulement

- `/api/users/**`
- `/api/admin/**`
- `/api/admin/dashboard` (statistiques tableau de bord)
- plusieurs endpoints réservations et paiements avec `@PreAuthorize("hasRole('ADMIN')")`
- `/api/avis/moderation` et `/api/avis/{id}/moderation`
- `/api/voyageurs/{id}/block` et `/api/voyageurs/{id}/unblock`
- `/api/reservations/user/{userId}`

### Authentifié

- `/api/voyageurs/**`
- `/api/reservations/**`
- `/api/activites-voyages/**`
- `/api/paiements/**`
- `/api/notifications/**`
- `/api/avis/**` (sauf les routes publiques ci-dessus)

### Important

- `/api/auth/me` et `/api/auth/logout` sont sous `/api/auth/**`, mais `me` a quand même besoin d'un JWT valide pour renvoyer l'utilisateur courant.
- `/api/paiements/webhook` est aujourd'hui protégé par la sécurité globale du projet, donc il demandera aussi une authentification tant que la config n'est pas ajustée.
- `/api/auth/logout` ne détruit pas le token côté serveur. Le backend est stateless JWT.

## Enums acceptées

### `ReservationStatut`

```text
CREE
PAYEE
ANNULEE
ECHEC
EN_ATTENTE
EN_ATTENTE_PAIEMENT
CONFIRMEE
COMPLETEE
```

### `PaymentStatus`

```text
EN_ATTENTE
EN_COURS
REUSSI
ECHOUE
ANNULE
REMBOURSE
REMBOURSEMENT_EN_COURS
```

### `VoyageStatus`

```text
DISPONIBLE
COMPLET
ANNULE
```

### `CompteStatus`

```text
ACTIVER
DESACTIVER
```

### `AvisStatus`

```text
EN_ATTENTE
VALIDE
REFUSE
```

### `NotificationType`

```text
RESERVATION_CREEE
PAIEMENT_REUSSI
PAIEMENT_ECHEC
RESERVATION_ANNULEE
RAPPEL_DE_VOYAGE
GENERAL
```

## Ordre recommandé pour tester tout le projet

1. Créer un voyageur avec `/api/auth/register`.
2. Se connecter avec `/api/auth/login`.
3. Récupérer le profil avec `/api/auth/me`.
4. Créer un voyage avec `/api/voyages`.
5. Créer une activité avec `/api/activites`.
6. Associer activité et voyage avec `/api/activites-voyages`.
7. Créer une réservation avec `/api/reservations`.
8. Créer une session Stripe avec `/api/paiements/create-session`.
9. Confirmer ou simuler l'échec de paiement.
10. Consulter les notifications reçues avec `/api/notifications/me`.
11. Après que la réservation est complétée, publier un avis avec `/api/avis`.
12. Modérer l'avis (admin) avec `PUT /api/avis/{id}/moderation?statut=VALIDE`.
13. Consulter le tableau de bord admin avec `/api/admin/dashboard`.
14. Tester le blocage d'un voyageur avec `PATCH /api/voyageurs/{id}/block`.

## 1. Authentification

### `POST {{BASE_URL}}/api/auth/register`

Crée un compte voyageur.

Auth: non

Body:

```json
{
  "nom": "Elhmaydi",
  "prenom": "Hicham",
  "email": "hicham@example.com",
  "password": "secret123",
  "telephone": "+212600000001",
  "idNational": "AB123456",
  "dateExpiration": "2030-12-31"
}
```

Réponse attendue:

```json
{
  "token": "...jwt...",
  "type": "Bearer",
  "id": 1,
  "nom": "Elhmaydi",
  "prenom": "Hicham",
  "email": "hicham@example.com",
  "role": "VOYAGEUR",
  "message": "..."
}
```

### `POST {{BASE_URL}}/api/auth/login`

Connexion.

Auth: non

Body:

```json
{
  "email": "hicham@example.com",
  "password": "secret123"
}
```

Réponse attendue:

```json
{
  "token": "...jwt...",
  "type": "Bearer",
  "id": 1,
  "nom": "Elhmaydi",
  "prenom": "Hicham",
  "email": "hicham@example.com",
  "role": "ROLE_VOYAGEUR",
  "message": "..."
}
```

### `POST {{BASE_URL}}/api/auth/register-admin`

Crée un administrateur.

Auth: `Bearer {{TOKEN_ADMIN}}`

Body:

```json
{
  "nom": "Admin",
  "prenom": "Principal",
  "email": "admin2@example.com",
  "password": "secret123",
  "telephone": "+212600000002",
  "idNational": "ADMIN002",
  "dateExpiration": "2035-12-31"
}
```

### `GET {{BASE_URL}}/api/auth/me`

Récupère l'utilisateur courant.

Auth: `Bearer {{TOKEN_VOYAGEUR}}` ou `Bearer {{TOKEN_ADMIN}}`

Pas de body.

### `POST {{BASE_URL}}/api/auth/logout`

Déconnexion logique seulement.

Auth: optionnel dans le code, mais recommandé si tu simules un vrai flux utilisateur.

Pas de body.

Réponse:

```json
{
  "success": true,
  "message": "Déconnexion réussie"
}
```

## 2. Voyages

### DTO `VoyageDTO`

```json
{
  "id": 1,
  "nom": "Circuit Désert",
  "description": "Voyage de 5 jours dans le désert",
  "cover": "https://example.com/covers/desert.jpg",
  "destination": "Maroc",
  "dateDepart": "2026-06-15",
  "dateRetour": "2026-06-20",
  "statut": "DISPONIBLE",
  "itineraire": "Marrakech -> Ouarzazate -> Merzouga",
  "photos": ["https://example.com/photo1.jpg", "https://example.com/photo2.jpg"]
}
```

### `POST {{BASE_URL}}/api/voyages`

Crée un voyage.

Auth: public dans la config actuelle.

Body exemple:

```json
{
  "nom": "Circuit Désert",
  "description": "Voyage de 5 jours dans le désert",
  "cover": "https://example.com/covers/desert.jpg",
  "destination": "Maroc",
  "dateDepart": "2026-06-15",
  "dateRetour": "2026-06-20",
  "statut": "DISPONIBLE",
  "itineraire": "Marrakech -> Ouarzazate -> Merzouga",
  "photos": []
}
```

### `GET {{BASE_URL}}/api/voyages`

Liste tous les voyages.

### `GET {{BASE_URL}}/api/voyages/{{VOYAGE_ID}}`

Détail d'un voyage.

### `GET {{BASE_URL}}/api/voyages/disponibles`

Liste des voyages disponibles.

### `GET {{BASE_URL}}/api/voyages/destination/Maroc`

Recherche par destination.

### `GET {{BASE_URL}}/api/voyages/statut/DISPONIBLE`

Recherche par statut.

### `GET {{BASE_URL}}/api/voyages/search?query=desert`

Recherche texte libre.

### `GET {{BASE_URL}}/api/voyages/nom/Circuit Désert`

Recherche par nom exact ou partiel selon le service.

### `GET {{BASE_URL}}/api/voyages/filter?destination=Maroc&statut=DISPONIBLE`

Filtre destination + statut.

### `GET {{BASE_URL}}/api/voyages/date-depart/2026-06-15`

Recherche par date de départ.

### `PUT {{BASE_URL}}/api/voyages/{{VOYAGE_ID}}`

Met à jour un voyage.

Body: même structure que `VoyageDTO`.

### `PATCH {{BASE_URL}}/api/voyages/{{VOYAGE_ID}}/statut?statut=COMPLET`

Met à jour uniquement le statut.

### `POST {{BASE_URL}}/api/voyages/{{VOYAGE_ID}}/photos`

Ajoute une photo.

Auth: public dans la config actuelle.

Body brut attendu: une chaîne JSON.

Exemple body raw JSON:

```json
"https://example.com/new-photo.jpg"
```

### `DELETE {{BASE_URL}}/api/voyages/{{VOYAGE_ID}}/photos`

Supprime une photo.

Body brut:

```json
"https://example.com/new-photo.jpg"
```

### `DELETE {{BASE_URL}}/api/voyages/{{VOYAGE_ID}}`

Supprime un voyage.

### `GET {{BASE_URL}}/api/voyages/stats/disponibles`

Compte des voyages disponibles.

### `GET {{BASE_URL}}/api/voyages/stats/total`

Compte total des voyages.

### `GET {{BASE_URL}}/api/voyages/stats/statut/ANNULE`

Compte par statut.

## 3. Activités

### DTO `ActiviteDTO`

```json
{
  "id": 1,
  "nom": "Balade en dromadaire",
  "description": "Balade guidée de deux heures dans les dunes",
  "voyageId": 1,
  "voyageNom": "Circuit Désert",
  "nombreReservations": 0
}
```

### `POST {{BASE_URL}}/api/activites`

Auth: public dans la config actuelle.

Body:

```json
{
  "nom": "Balade en dromadaire",
  "description": "Balade guidée de deux heures dans les dunes",
  "voyageId": {{VOYAGE_ID}}
}
```

### `GET {{BASE_URL}}/api/activites`

Liste toutes les activités.

### `GET {{BASE_URL}}/api/activites/{{ACTIVITE_ID}}`

Détail activité.

### `GET {{BASE_URL}}/api/activites/{{ACTIVITE_ID}}/details`

Détail avec réservations liées.

### `GET {{BASE_URL}}/api/activites/voyage/{{VOYAGE_ID}}`

Activités d'un voyage.

### `GET {{BASE_URL}}/api/activites/voyage/{{VOYAGE_ID}}/details`

Activités d'un voyage avec détails.

### `GET {{BASE_URL}}/api/activites/search?nom=dromadaire`

Recherche par nom.

### `GET {{BASE_URL}}/api/activites/search/description?keyword=dunes`

Recherche par description.

### `GET {{BASE_URL}}/api/activites/populaires`

Liste activités populaires.

### `PUT {{BASE_URL}}/api/activites/{{ACTIVITE_ID}}`

Body: même structure que `ActiviteDTO`.

### `DELETE {{BASE_URL}}/api/activites/{{ACTIVITE_ID}}`

Suppression normale.

### `DELETE {{BASE_URL}}/api/activites/{{ACTIVITE_ID}}/force`

Suppression forcée.

### `GET {{BASE_URL}}/api/activites/voyage/{{VOYAGE_ID}}/count`

Compte des activités par voyage.

### `GET {{BASE_URL}}/api/activites/{{ACTIVITE_ID}}/exists`

Vérifie l'existence.

## 4. Associations Activité-Voyage

### Important

Ces routes sont sous `/api/activites-voyages` et demandent actuellement un JWT valide.

### DTO `ActiviteVoyageDTO`

```json
{
  "id": 1,
  "activiteId": 1,
  "voyageId": 1,
  "activiteNom": "Balade en dromadaire",
  "activiteDescription": "Balade guidée",
  "voyageNom": "Circuit Désert",
  "voyageDestination": "Maroc",
  "prix": 250.0,
  "obligatoire": false,
  "ordreAffichage": 1,
  "jourPrevu": "Jour 2",
  "dureeMinutes": 120,
  "notes": "Prévoir lunettes de soleil",
  "disponible": true
}
```

### `POST {{BASE_URL}}/api/activites-voyages`

Auth: `Bearer {{TOKEN_VOYAGEUR}}` ou `Bearer {{TOKEN_ADMIN}}`

Body:

```json
{
  "activiteId": {{ACTIVITE_ID}},
  "voyageId": {{VOYAGE_ID}},
  "prix": 250.00,
  "obligatoire": false,
  "ordreAffichage": 1,
  "jourPrevu": "Jour 2",
  "dureeMinutes": 120,
  "notes": "Prévoir lunettes de soleil",
  "disponible": true
}
```

### `GET {{BASE_URL}}/api/activites-voyages/voyage/{{VOYAGE_ID}}`

Liste des associations pour un voyage.

### `GET {{BASE_URL}}/api/activites-voyages/activite/{{ACTIVITE_ID}}`

Liste des voyages pour une activité.

### `GET {{BASE_URL}}/api/activites-voyages/voyage/{{VOYAGE_ID}}/obligatoires`

Liste des activités obligatoires.

### `GET {{BASE_URL}}/api/activites-voyages/voyage/{{VOYAGE_ID}}/optionnelles`

Liste des activités optionnelles.

### `GET {{BASE_URL}}/api/activites-voyages/voyage/{{VOYAGE_ID}}/jour/Jour 2`

Liste par jour prévu.

### `PUT {{BASE_URL}}/api/activites-voyages/{{ACTIVITE_VOYAGE_ID}}`

Body: même structure que `ActiviteVoyageDTO`.

### `DELETE {{BASE_URL}}/api/activites-voyages/activite/{{ACTIVITE_ID}}/voyage/{{VOYAGE_ID}}`

Dissocie activité et voyage.

### `DELETE {{BASE_URL}}/api/activites-voyages/{{ACTIVITE_VOYAGE_ID}}`

Supprime l'association par id.

### `GET {{BASE_URL}}/api/activites-voyages/voyage/{{VOYAGE_ID}}/count`

Compte total, obligatoires, optionnelles.

## 5. Réservations

### DTO `ReservationDTO`

Pour créer, les champs minimums sont surtout:

```json
{
  "voyageId": 1,
  "nombrePersonnes": 2,
  "commentaire": "Nous arriverons tard",
  "activitesOptionnellesIds": [1]
}
```

Réponse plus complète possible:

```json
{
  "id": 1,
  "voyageId": 1,
  "userId": 2,
  "voyageNom": "Circuit Désert",
  "voyageDestination": "Maroc",
  "voyageDateDepart": "2026-06-15",
  "voyageDateRetour": "2026-06-20",
  "userNom": "Elhmaydi",
  "userPrenom": "Hicham",
  "userEmail": "hicham@example.com",
  "nombrePersonnes": 2,
  "statut": "EN_ATTENTE",
  "dateReservation": "2026-03-12T16:00:00",
  "prixBase": 1000,
  "prixActivites": 250,
  "montantTotal": 2250,
  "commentaire": "Nous arriverons tard",
  "activitesOptionnellesIds": [1],
  "motifAnnulation": null,
  "paiementEffectue": false,
  "datePaiement": null
}
```

### `POST {{BASE_URL}}/api/reservations`

Auth: `Bearer {{TOKEN_VOYAGEUR}}` ou `Bearer {{TOKEN_ADMIN}}`

Body:

```json
{
  "voyageId": {{VOYAGE_ID}},
  "nombrePersonnes": 2,
  "commentaire": "Nous arriverons tard",
  "activitesOptionnellesIds": [{{ACTIVITE_ID}}]
}
```

### `GET {{BASE_URL}}/api/reservations?page=0&size=10&sortBy=dateReservation&sortDir=DESC`

Liste paginée de toutes les réservations.

Auth: `Bearer {{TOKEN_ADMIN}}`

### `GET {{BASE_URL}}/api/reservations/{{RESERVATION_ID}}`

Détail d'une réservation.

Auth: voyageur propriétaire ou admin.

### `GET {{BASE_URL}}/api/reservations/me`

Réservations du voyageur connecté.

### `GET {{BASE_URL}}/api/reservations/voyage/{{VOYAGE_ID}}`

Réservations d'un voyage.

Auth: admin.

### `GET {{BASE_URL}}/api/reservations/statut/EN_ATTENTE`

Filtre par statut.

Auth: admin.

### `GET {{BASE_URL}}/api/reservations/en-attente`

Réservations en attente.

### `GET {{BASE_URL}}/api/reservations/recentes`

Réservations récentes.

### `GET {{BASE_URL}}/api/reservations/user/{{VOYAGEUR_ID}}`

Historique des réservations d'un voyageur spécifique (admin uniquement).

Auth: `Bearer {{TOKEN_ADMIN}}`

### `PUT {{BASE_URL}}/api/reservations/{{RESERVATION_ID}}/confirmer`

Auth: admin.

### `PUT {{BASE_URL}}/api/reservations/{{RESERVATION_ID}}/annuler`

Auth: voyageur propriétaire ou admin.

Body:

```json
{
  "motif": "Changement de programme"
}
```

### `PUT {{BASE_URL}}/api/reservations/{{RESERVATION_ID}}/completer`

Auth: admin.

### `PUT {{BASE_URL}}/api/reservations/{{RESERVATION_ID}}`

Met à jour une réservation.

Auth: voyageur propriétaire ou admin.

Body exemple:

```json
{
  "voyageId": {{VOYAGE_ID}},
  "nombrePersonnes": 3,
  "commentaire": "Mise à jour réservation",
  "activitesOptionnellesIds": [{{ACTIVITE_ID}}]
}
```

### `DELETE {{BASE_URL}}/api/reservations/{{RESERVATION_ID}}`

Auth: admin.

### `GET {{BASE_URL}}/api/reservations/count/statut/CONFIRMEE`

Auth: admin.

Note: la réponse renvoie `statut` sous forme d'ordinal numérique, pas du texte.

### `GET {{BASE_URL}}/api/reservations/me/count`

Compte des réservations de l'utilisateur courant.

### `PUT {{BASE_URL}}/api/reservations/{{RESERVATION_ID}}/payer`

Marque la réservation comme payée.

Auth: admin.

## 6. Paiements

### Important

- Le contrôleur est exposé sous `/api/paiements`.
- Tous les endpoints demandent en pratique un JWT valide à cause de la config de sécurité actuelle.
- Les webhooks Stripe ne sont pas encore traités fonctionnellement: la route renvoie juste `Webhook reçu`.

### DTO `PaymentDTO`

```json
{
  "id": 1,
  "stripeSessionId": "cs_test_xxx",
  "amount": 2250.0,
  "status": "EN_ATTENTE",
  "userId": 2,
  "reservationId": 1,
  "dateCreation": "2026-03-12T16:10:00",
  "datePaiement": null
}
```

### `POST {{BASE_URL}}/api/paiements/create-session`

Auth: `Bearer {{TOKEN_VOYAGEUR}}` ou `Bearer {{TOKEN_ADMIN}}`

Body:

```json
{
  "reservationId": {{RESERVATION_ID}}
}
```

Réponse typique:

```json
{
  "sessionId": "cs_test_xxx",
  "checkoutUrl": "https://checkout.stripe.com/..."
}
```

### `POST {{BASE_URL}}/api/paiements/confirm`

Auth: actuellement JWT nécessaire.

Body:

```json
{
  "sessionId": "cs_test_xxx"
}
```

### `POST {{BASE_URL}}/api/paiements/failure`

Auth: actuellement JWT nécessaire.

Body:

```json
{
  "sessionId": "cs_test_xxx",
  "reason": "Carte refusée"
}
```

### `POST {{BASE_URL}}/api/paiements/webhook`

Auth: actuellement JWT nécessaire.

Headers:

```http
Stripe-Signature: t=...,v1=...
```

Body: payload Stripe brut.

Réponse actuelle:

```text
Webhook reçu
```

### `GET {{BASE_URL}}/api/paiements`

Liste de tous les paiements.

Auth: admin.

### `GET {{BASE_URL}}/api/paiements/{{PAYMENT_ID}}`

Détail d'un paiement.

Auth: propriétaire ou admin.

### `GET {{BASE_URL}}/api/paiements/me`

Paiements du voyageur courant.

### `GET {{BASE_URL}}/api/paiements/reservation/{{RESERVATION_ID}}`

Paiement associé à une réservation.

### `GET {{BASE_URL}}/api/paiements/statut/REUSSI`

Liste des paiements par statut.

Auth: admin.

### `PUT {{BASE_URL}}/api/paiements/{{PAYMENT_ID}}/annuler`

Annulation d'un paiement.

Auth: propriétaire ou admin.

### `POST {{BASE_URL}}/api/paiements/{{PAYMENT_ID}}/rembourser`

Auth: admin.

Body:

```json
{
  "amount": 1000,
  "reason": "Demande client"
}
```

Le champ `amount` peut être omis pour rembourser le montant complet selon l'implémentation du service.

### `GET {{BASE_URL}}/api/paiements/statistiques/chiffre-affaires`

Chiffre d'affaires total.

Auth: admin.

### `GET {{BASE_URL}}/api/paiements/statistiques/chiffre-affaires/periode?debut=2026-03-01T00:00:00&fin=2026-03-31T23:59:59`

Chiffre d'affaires entre deux dates.

Auth: admin.

Format requis pour `debut` et `fin`: ISO `yyyy-MM-ddTHH:mm:ss`

## 7. Voyageurs

### DTO `VoyageurDTO`

```json
{
  "id": 1,
  "nom": "Elhmaydi",
  "prenom": "Hicham",
  "email": "hicham@example.com",
  "telephone": "+212600000001",
  "status": "ACTIVER",
  "idNational": "AB123456",
  "dateExpiration": "2030-12-31"
}
```

### `POST {{BASE_URL}}/api/voyageurs`

Auth: JWT requis.

Body:

```json
{
  "nom": "Ali",
  "prenom": "Youssef",
  "email": "ali@example.com",
  "telephone": "+212600000010",
  "status": "ACTIVER",
  "idNational": "XY987654",
  "dateExpiration": "2031-01-01"
}
```

### `GET {{BASE_URL}}/api/voyageurs`

Liste des voyageurs.

### `GET {{BASE_URL}}/api/voyageurs/{{VOYAGEUR_ID}}`

Détail voyageur.

### `GET {{BASE_URL}}/api/voyageurs/email/ali@example.com`

Recherche par email.

### `GET {{BASE_URL}}/api/voyageurs/status/ACTIVER`

Filtre par statut.

### `GET {{BASE_URL}}/api/voyageurs/search?query=Ali`

Recherche texte.

### `PUT {{BASE_URL}}/api/voyageurs/{{VOYAGEUR_ID}}`

Body: même structure que `VoyageurDTO`.

### `PATCH {{BASE_URL}}/api/voyageurs/{{VOYAGEUR_ID}}/toggle-status`

Bascule le statut.

### `DELETE {{BASE_URL}}/api/voyageurs/{{VOYAGEUR_ID}}`

Supprime un voyageur.

### `GET {{BASE_URL}}/api/voyageurs/stats/active`

Compte des voyageurs actifs.

### `GET {{BASE_URL}}/api/voyageurs/stats/total`

Compte total des voyageurs.

### `PATCH {{BASE_URL}}/api/voyageurs/{{VOYAGEUR_ID}}/block`

Bloque un voyageur (désactive le compte et empêche toute connexion, réservation et publication d'avis).

Auth: `Bearer {{TOKEN_ADMIN}}`

Pas de body.

Réponse: `VoyageurDTO` avec `bloque: true`, `actif: false`, `status: "DESACTIVER"`.

### `PATCH {{BASE_URL}}/api/voyageurs/{{VOYAGEUR_ID}}/unblock`

Débloque un voyageur (réactive l'accès).

Auth: `Bearer {{TOKEN_ADMIN}}`

Pas de body.

## 8. Utilisateurs

### Important

Toutes les routes `/api/users/**` demandent un token admin.

### DTO `CreateUserRequest`

```json
{
  "nom": "Admin",
  "prenom": "Support",
  "email": "support@example.com",
  "telephone": "+212600000011",
  "password": "secret123"
}
```

### DTO `UserDTO`

```json
{
  "id": 10,
  "nom": "Admin",
  "prenom": "Support",
  "email": "support@example.com",
  "telephone": "+212600000011",
  "status": "ACTIVER",
  "role": "ADMIN"
}
```

### `POST {{BASE_URL}}/api/users`

Auth: `Bearer {{TOKEN_ADMIN}}`

Body:

```json
{
  "nom": "Admin",
  "prenom": "Support",
  "email": "support@example.com",
  "telephone": "+212600000011",
  "password": "secret123"
}
```

### `GET {{BASE_URL}}/api/users`

Liste tous les utilisateurs.

### `GET {{BASE_URL}}/api/users/{{USER_ID}}`

Détail utilisateur.

### `GET {{BASE_URL}}/api/users/email/support@example.com`

Recherche par email.

### `GET {{BASE_URL}}/api/users/status/ACTIVER`

Filtre par statut.

### `GET {{BASE_URL}}/api/users/active`

Liste des utilisateurs actifs.

### `GET {{BASE_URL}}/api/users/search?query=Support`

Recherche texte.

### `PUT {{BASE_URL}}/api/users/{{USER_ID}}`

Body:

```json
{
  "nom": "Admin",
  "prenom": "Support MAJ",
  "email": "support@example.com",
  "telephone": "+212600000099",
  "status": "ACTIVER",
  "role": "ADMIN"
}
```

### `PATCH {{BASE_URL}}/api/users/{{USER_ID}}/toggle-status`

Bascule le statut.

### `PATCH {{BASE_URL}}/api/users/{{USER_ID}}/password`

Body:

```json
{
  "newPassword": "newSecret123"
}
```

### `DELETE {{BASE_URL}}/api/users/{{USER_ID}}`

Suppression utilisateur.

### `GET {{BASE_URL}}/api/users/exists/support@example.com`

Vérifie l'existence d'un email.

### `GET {{BASE_URL}}/api/users/stats/active`

Compte des utilisateurs actifs.

### `GET {{BASE_URL}}/api/users/stats/total`

Compte total des utilisateurs.

### `GET {{BASE_URL}}/api/users/stats/inactive`

Compte des utilisateurs inactifs.

## 9. Avis & Évaluations

### Règles métier importantes

- Un voyageur ne peut publier un avis que si sa réservation pour ce voyage est au statut `COMPLETEE`.
- Un avis publié passe en `EN_ATTENTE` jusqu'à modération par l'admin.
- Seuls les avis `VALIDE` sont visibles publiquement via `GET /api/avis/voyage/{voyageId}`.
- Un voyageur bloqué ne peut pas publier d'avis.

### DTO `AvisDTO`

```json
{
  "id": 1,
  "voyageId": 1,
  "voyageurId": 2,
  "voyageNom": "Circuit Désert",
  "voyageurNom": "Elhmaydi",
  "voyageurPrenom": "Hicham",
  "note": 5,
  "commentaire": "Voyage exceptionnel, guide très professionnel.",
  "statut": "EN_ATTENTE",
  "dateCreation": "2026-03-13T10:00:00",
  "dateModeration": null
}
```

### `POST {{BASE_URL}}/api/avis`

Publie un avis sur un voyage terminé.

Auth: `Bearer {{TOKEN_VOYAGEUR}}`

Body:

```json
{
  "voyageId": {{VOYAGE_ID}},
  "note": 5,
  "commentaire": "Voyage exceptionnel, guide très professionnel."
}
```

Réponse: `AvisDTO` avec `statut: "EN_ATTENTE"`.

Script Postman pour enregistrer l'ID:

```javascript
const data = pm.response.json();
pm.environment.set("AVIS_ID", data.id);
```

### `GET {{BASE_URL}}/api/avis/voyage/{{VOYAGE_ID}}`

Liste des avis **validés** d'un voyage.

Auth: non (public).

### `GET {{BASE_URL}}/api/avis/me`

Tous les avis du voyageur connecté (tous statuts).

Auth: `Bearer {{TOKEN_VOYAGEUR}}`

### `GET {{BASE_URL}}/api/avis/moderation?statut=EN_ATTENTE`

Liste des avis selon leur statut de modération.

Auth: `Bearer {{TOKEN_ADMIN}}`

Valeurs possibles du paramètre `statut`: `EN_ATTENTE`, `VALIDE`, `REFUSE`.

### `PUT {{BASE_URL}}/api/avis/{{AVIS_ID}}/moderation?statut=VALIDE`

Modére un avis (valide ou refuse).

Auth: `Bearer {{TOKEN_ADMIN}}`

Pas de body — le statut est passé en query parameter.

Valeurs possibles: `VALIDE` ou `REFUSE` (pas `EN_ATTENTE`).

Réponse: `AvisDTO` mis à jour avec `dateModeration` renseignée.

### `DELETE {{BASE_URL}}/api/avis/{{AVIS_ID}}`

Supprime un avis (modération).

Auth: `Bearer {{TOKEN_ADMIN}}`

## 10. Notifications

### Description

Les notifications sont créées automatiquement par le système lors des événements suivants:

- Création d'une réservation → type `RESERVATION_CREEE`
- Paiement initié → type `GENERAL`
- Paiement confirmé → type `PAIEMENT_REUSSI`
- Paiement échoué → type `PAIEMENT_ECHEC`
- Remboursement effectué → type `GENERAL`
- Réservation annulée → type `RESERVATION_ANNULEE`

### DTO `NotificationDTO`

```json
{
  "id": 1,
  "titre": "Réservation créée",
  "message": "Votre réservation 1 a bien été créée.",
  "lu": false,
  "dateCreation": "2026-03-13T10:05:00",
  "type": "RESERVATION_CREEE"
}
```

### `GET {{BASE_URL}}/api/notifications/me`

Liste toutes les notifications de l'utilisateur connecté, triées de la plus récente à la plus ancienne.

Auth: `Bearer {{TOKEN_VOYAGEUR}}` ou `Bearer {{TOKEN_ADMIN}}`

Pas de body.

### `GET {{BASE_URL}}/api/notifications/me/unread-count`

Nombre de notifications non lues de l'utilisateur connecté.

Auth: `Bearer {{TOKEN_VOYAGEUR}}` ou `Bearer {{TOKEN_ADMIN}}`

Réponse:

```json
{
  "count": 3
}
```

### `PATCH {{BASE_URL}}/api/notifications/{{NOTIFICATION_ID}}/read`

Marque une notification comme lue.

Auth: `Bearer {{TOKEN_VOYAGEUR}}` ou `Bearer {{TOKEN_ADMIN}}`

Pas de body.

Réponse: `204 No Content`.

Erreur si la notification appartient à un autre utilisateur: `403 Forbidden`.

## 11. Tableau de bord Admin

### `GET {{BASE_URL}}/api/admin/dashboard`

Retourne des statistiques consolidées pour le tableau de bord administrateur.

Auth: `Bearer {{TOKEN_ADMIN}}`

Pas de body.

Réponse:

```json
{
  "totalReservations": 120,
  "reservationsEnAttente": 15,
  "reservationsConfirmees": 60,
  "reservationsCompletees": 35,
  "reservationsAnnulees": 10,
  "totalVoyages": 8,
  "voyagesDisponibles": 5,
  "totalVoyageurs": 200,
  "voyageursActifs": 195,
  "voyageursBloques": 5,
  "avisEnAttente": 8,
  "avisValides": 75,
  "avisRefuses": 12
}
```

## Exemples rapides pour Postman

### Script Postman après login voyageur

Dans l'onglet `Tests` de la requête login:

```javascript
const data = pm.response.json();
pm.environment.set("TOKEN_VOYAGEUR", data.token);
pm.environment.set("USER_ID", data.id);
```

### Script Postman après création voyage

```javascript
const data = pm.response.json();
pm.environment.set("VOYAGE_ID", data.id);
```

### Script Postman après création activité

```javascript
const data = pm.response.json();
pm.environment.set("ACTIVITE_ID", data.id);
```

### Script Postman après création réservation

```javascript
const data = pm.response.json();
pm.environment.set("RESERVATION_ID", data.id);
```

### Script Postman après création session paiement

```javascript
const data = pm.response.json();
pm.environment.set("STRIPE_SESSION_ID", data.sessionId);
```

### Script Postman après publication d'un avis

```javascript
const data = pm.response.json();
pm.environment.set("AVIS_ID", data.id);
```

### Script Postman après lecture des notifications

```javascript
const data = pm.response.json();
if (data.length > 0) {
  pm.environment.set("NOTIFICATION_ID", data[0].id);
}
```

## Séquence de test minimale complète

### Flux voyageur complet

1. `POST /api/auth/register`
2. `POST /api/auth/login`
3. `GET /api/auth/me`
4. `GET /api/voyages`
5. `POST /api/reservations`
6. `GET /api/notifications/me` — notification de création reçue
7. `GET /api/notifications/me/unread-count`
8. `PATCH /api/notifications/{id}/read`
9. `GET /api/reservations/me`
10. `POST /api/paiements/create-session`
11. `POST /api/paiements/confirm`
12. `GET /api/notifications/me` — notification de paiement reçue
13. `GET /api/paiements/me`
14. (après voyage complété) `POST /api/avis`
15. `GET /api/avis/me`

### Flux admin complet

1. `POST /api/auth/login` avec admin
2. `GET /api/admin/dashboard` — vue globale
3. `POST /api/auth/register-admin`
4. `POST /api/voyages`
5. `POST /api/activites`
6. `POST /api/activites-voyages`
7. `GET /api/users`
8. `GET /api/reservations`
9. `PUT /api/reservations/{id}/confirmer`
10. `PUT /api/reservations/{id}/completer`
11. `GET /api/avis/moderation?statut=EN_ATTENTE`
12. `PUT /api/avis/{id}/moderation?statut=VALIDE`
13. `GET /api/paiements`
14. `POST /api/paiements/{id}/rembourser`
15. `PATCH /api/voyageurs/{id}/block` (si comportement inapproprié)

## Notes de validation

- Les routes `voyages` et `activites` sont ouvertes publiquement dans la version actuelle.
- Les routes `paiements` utilisent le préfixe `/api/paiements` et non `/api/payments`.
- Les dates des paiements par période doivent être passées en ISO date-time.
- Les routes photo des voyages attendent un body brut de type chaîne JSON, pas un objet.
- Le webhook Stripe est encore un stub de réception.
- Un avis ne peut être soumis que si la réservation du voyageur pour ce voyage est `COMPLETEE`.
- Un voyageur bloqué (`bloque: true`) ne peut plus se connecter (`isAccountNonLocked = false`) ni réserver ni publier d'avis.
- Les notifications sont créées automatiquement à chaque événement métier (réservation, paiement, annulation) ; aucune action manuelle admin requise.
- Le dashboard admin (`GET /api/admin/dashboard`) agrège les données en une seule requête sans paramètre.
- Les avis en attente de modération sont visibles uniquement via `GET /api/avis/moderation?statut=EN_ATTENTE` (admin).
