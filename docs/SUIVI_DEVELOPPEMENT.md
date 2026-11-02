# Suivi du développement

Ce fichier est le journal chronologique du projet. Il doit être mis à jour après chaque changement significatif : nouvelle fonctionnalité, modification d'architecture, endpoint, écran, correction ou validation.

## Légende

- [ ] À faire
- [~] En cours
- [x] Terminé
- [!] Bloqué ou nécessite une décision

## État global

**Dernière mise à jour :** 2026-11-02

**Phase actuelle :** Étape 1 — Authentification et accès par rôle (TERMINÉE ✅)

**Prochaine étape :** Étape 2 — Accès client temporaire (code de réservation) & Étape 3 — Catalogue des services.

## Avancement par domaine

| Domaine | État | Commentaire |
|---|---|---|
| Page d'accueil | [x] | Page Maison Élan avec route `/` |
| Login frontend | [x] | Identifiant email et mot de passe avec validation |
| Login backend | [x] | `POST /api/auth/login` sécurisé |
| DTO d'authentification | [x] | `LoginRequest` (avec Bean Validation) et `LoginResponse` (avec token JWT) |
| Rôles | [x] | `CLIENT`, `STAFF`, `ADMIN` normalisés |
| Redirection par rôle | [x] | Routes `/client`, `/staff`, `/admin` |
| JWT | [x] | JJWT 0.12.6, JwtUtil, JwtAuthFilter avec extraction userId & role |
| BCrypt | [x] | Passwords hashés dans `data.sql` et vérifiés via `BCryptPasswordEncoder` |
| Spring Security | [x] | Stateless, CORS unifié, CSRF désactivé, RBAC par endpoints |
| Guards & Interceptor Angular | [x] | `jwtInterceptor`, `authGuard`, `roleGuard` activés sur toutes les routes privées |
| Services | [~] | Entités et endpoints existants, DTOs de réponse à affiner |
| Demandes | [~] | Entités, endpoints et écrans existants, à relier aux tokens d'accès |
| Suivi des statuts | [ ] | À aligner sur `CREATED`, `ACKNOWLEDGED`, `IN_PROGRESS`, `COMPLETED` |
| Notifications WebSocket | [ ] | Configuration STOMP à ajouter |
| Administration | [ ] | CRUD utilisateurs et départements à finaliser |
| Statistiques | [ ] | À créer après les demandes persistées |
| Tests complets | [ ] | À organiser par fonctionnalité |

## Journal des changements

### 2026-11-02 — Sécurisation complète Authentification (Étape 1)

- **Responsables :** Personne A (Backend) & Personne B (Frontend)
- **Fonctionnalité :** Sécurité, Authentification, JWT, BCrypt, RBAC, Guards
- **Changements backend :**
  - Ajout des dépendances dans `pom.xml` (`spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson` 0.12.6, `spring-boot-starter-validation`).
  - Ajout des propriétés `jwt.secret` et `jwt.expiration` dans `application.properties`.
  - Création de `JwtUtil.java` pour génération et validation des tokens JWT avec claims (userId, role).
  - Création de `UserDetailsServiceImpl.java` implémentant `UserDetailsService`.
  - Création de `JwtAuthFilter.java` interceptant le header `Authorization: Bearer <token>`.
  - Création de `SecurityConfig.java` configurant CORS centralisé, session STATELESS, et règles RBAC.
  - Mise à jour de `AuthService.java` avec `AuthenticationManager` et `BCryptPasswordEncoder`.
  - Mise à jour de `AuthController.java` avec `@Valid` et gestion d'exceptions 401.
  - Mise à jour de `data.sql` avec les hash BCrypt pour les comptes de test.
  - Suppression des annotations `@CrossOrigin` éparpillées sur les contrôleurs.
- **Changements frontend :**
  - Mise à jour de `LoginResponse` avec le champ `token`.
  - Enrichissement de `AuthService` (`getToken()`, `isLoggedIn()`, `getRole()`, gestion `localStorage`).
  - Création de `jwt.interceptor.ts` injectant le token Bearer sur les requêtes HTTP.
  - Création de `authGuard.ts` pour rediriger les utilisateurs non authentifiés vers `/login`.
  - Création de `roleGuard.ts` pour vérifier les droits d'accès selon le rôle.
  - Mise à jour de `app.config.ts` pour enregistrer `withInterceptors([jwtInterceptor])`.
  - Protection des routes `/client`, `/staff`, `/admin` dans `app.routes.ts`.
  - Nettoyage des fallbacks hardcodés dans `client-dashboard.component.ts`.
- **Validation :**
  - Compilation Java Maven : `mvnw.cmd test-compile` -> BUILD SUCCESS (0 erreurs).
  - Compilation TypeScript : `npx tsc --noEmit` -> 0 erreurs.
- **Résultat :** Terminé ✅
- **Prochaine action :** Étape 2 — Accès client temporaire (code de réservation) & Étape 3 — Catalogue des services.

## Prochaine tâche

### Tâche DEV-002 — Accès client temporaire & Catalogue des services

**Personne A (Backend) :**
- [ ] Endpoint `POST /api/auth/guest-access` pour valider un code réservation actif et générer un token guest.
- [ ] DTO `ServiceResponse` et sécurisation des endpoints de `ServiceController`.

**Personne B (Frontend) :**
- [ ] Écran/modal de saisie du code d'accès réservation client.
- [ ] Dynamisation complète du catalogue des services avec filtres par département.
