# Suivi du développement

Ce fichier est le journal chronologique du projet. Il doit être mis à jour après chaque changement significatif : nouvelle fonctionnalité, modification d'architecture, endpoint, écran, correction ou validation.

## Légende

- [ ] À faire
- [~] En cours
- [x] Terminé
- [!] Bloqué ou nécessite une décision

## État global

**Dernière mise à jour :** 2026-11-02

**Phase actuelle :** Étape 2 — Accès client temporaire (TERMINÉE ✅)

**Branche Git actuelle :** `feature/etape-2-acces-client`

**Prochaine étape :** Étape 3 — Services & Règle d'Assignation (Admin)

## Décisions Métier Validées

1. **Accès Client Séjour Temporaire** :
   - Accès sans mot de passe complexe via code réservation (`RES-2026-001` ou `STAY-201`).
   - Contrôle strict : non révoqué, date d'expiration valide, séjour en cours.
   - Génération de token JWT avec claims (`userId`, `role=CLIENT`, `reservationId`, `numChambre`).
2. **Interface Bimodale** :
   - Onglet 1 : "Accès Séjour Client" (pour les clients avec code).
   - Onglet 2 : "Équipe & Personnel" (pour Staff, Superviseur, Réception, Admin).
3. **Deux modes d'assignation selon le service (réglable par l'Admin via `necessiteSupervision`)** :
   - Simple (`false`) : Auto-prise par l'employé.
   - Supervisé (`true`) : Affectation obligatoire par le Personnel (superviseur).
4. **Alertes de Retard (SLA)** :
   - Notification si la tâche dépasse le temps estimé.

## Avancement par domaine

| Domaine | État | Commentaire |
|---|---|---|
| Sécurité & Auth | [x] | Spring Security, BCrypt, JWT, Guards Angular |
| Accès Client Séjour | [x] | Endpoint `POST /api/auth/guest-access`, DTOs, interface à onglets |
| Services & Règles Admin | [~] | Prêt pour l'Étape 3 (flag `necessiteSupervision` sur Service) |
| Demandes & Routage | [ ] | Routage automatique par département du service |
| Traitement Staff | [ ] | Séparation Vue Employé (exécutant) / Vue Personnel (superviseur) |
| Tour de Contrôle Réception | [ ] | Vue transversale et support client |
| Notifications SLA & WebSocket | [ ] | Détection automatique des retards et STOMP |
| Administration & Stats | [ ] | Configuration des services et indicateurs de performance |
| Recette & Démo | [ ] | Validation finale du scénario complet |

## Journal des changements

### 2026-11-02 — Implémentation Étape 2 (Accès Client Temporaire Séjour)

- **Responsables :** Personne A (Backend) & Personne B (Frontend)
- **Fonctionnalité :** Accès client temporaire lié à la réservation (AUTH03, AUTH04, RM-01, RM-02)
- **Changements backend :**
  - Création des DTOs `GuestAccessRequest` et `GuestAccessResponse`.
  - Enrichissement de `JwtUtil` (`generateGuestToken` avec claims `reservationId` et `numChambre`).
  - Implémentation de `AuthService.guestLogin(code)` avec vérification des dates de séjour, révocation et expiration.
  - Ajout de l'endpoint `POST /api/auth/guest-access` dans `AuthController`.
  - Ajout de codes de test dans `data.sql` : `RES-2026-001`, `STAY-201`, `EXP-999`.
- **Changements frontend :**
  - Création des modèles TypeScript `GuestAccessRequest` et `GuestAccessResponse`.
  - Enrichissement de `AuthService` Angular avec `guestLogin()`, `getRoomNumber()`, `getRoomType()`.
  - Refonte de `LoginComponent` avec onglets switchables ("Accès Séjour Client" vs "Équipe & Personnel").
  - Dynamisation de `ClientDashboardComponent` : affichage réel de la chambre (ex: 201) et du nom du client.
- **Validation :**
  - Maven compile : `mvnw.cmd test-compile` -> BUILD SUCCESS (0 erreurs).
  - TypeScript : `npx tsc --noEmit` -> 0 erreurs.
- **Résultat :** Étape 2 terminée et validée ✅
