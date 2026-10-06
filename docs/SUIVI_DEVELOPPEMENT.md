# Suivi du développement

Ce fichier est le journal chronologique du projet. Il doit être mis à jour après chaque changement significatif : nouvelle fonctionnalité, modification d'architecture, endpoint, écran, correction ou validation.

## Légende

- [ ] À faire
- [~] En cours
- [x] Terminé
- [!] Bloqué ou nécessite une décision

## État global

**Dernière mise à jour :** 2026-10-06

**Phase actuelle :** Étape 1 — Authentification et accès par rôle

**Prochaine étape :** sécuriser l'authentification puis commencer le catalogue des services.

## Avancement par domaine

| Domaine | État | Commentaire |
|---|---|---|
| Page d'accueil | [x] | Page Maison Élan avec route `/` |
| Login frontend | [x] | Identifiant général et mot de passe |
| Login backend | [x] | `POST /api/auth/login` |
| DTO d'authentification | [x] | `LoginRequest` et `LoginResponse` |
| Rôles | [x] | `CLIENT`, `STAFF`, `ADMIN` côté démonstration |
| Redirection par rôle | [x] | Routes `/client`, `/staff`, `/admin` |
| JWT | [ ] | À ajouter avec Spring Security |
| BCrypt | [ ] | À ajouter avant une utilisation réelle |
| Services | [ ] | Entité et API à créer |
| Demandes | [ ] | Entité, API et interfaces à créer |
| Suivi des statuts | [ ] | À créer avec l'historique |
| Notifications WebSocket | [ ] | À créer après le cycle des demandes |
| Administration | [ ] | À créer après les utilisateurs et services |
| Statistiques | [ ] | À créer après les demandes persistées |
| Tests complets | [ ] | À organiser par fonctionnalité |

## Journal des changements

### 2026-10-06 — Préparation du plan projet

- [x] Lecture du cahier des charges.
- [x] Répartition proposée entre deux personnes.
- [x] Définition de l'ordre des dépendances.
- [x] Création du fichier `docs/PLAN_DEVELOPPEMENT.md`.
- [x] Création du présent fichier de suivi.

### 2026-10-06 — Authentification multi-rôles

- [x] Remplacement du numéro de chambre par un identifiant général dans le formulaire.
- [x] Ajout du DTO backend `LoginRequest`.
- [x] Ajout du DTO backend `LoginResponse`.
- [x] Modification de `AuthService` pour retourner un DTO et normaliser le rôle.
- [x] Ajout des comptes de démonstration client, staff et admin.
- [x] Redirection Angular vers `/client`, `/staff` ou `/admin`.
- [x] Ajout des modèles frontend `LoginRequest` et `LoginResponse`.

## Prochaine tâche

### Tâche DEV-001 — Sécuriser l'authentification

**Responsable recommandé :** Personne A

- [ ] Ajouter Spring Security.
- [ ] Hasher les mots de passe avec BCrypt.
- [ ] Générer un JWT lors du login.
- [ ] Ajouter un interceptor HTTP Angular pour envoyer le token.
- [ ] Ajouter un guard Angular pour protéger les routes.
- [ ] Vérifier les permissions côté backend.

### Tâche DEV-002 — Préparer le catalogue des services

**Responsable recommandé :** Personne A puis Personne B

- [ ] Créer `Department`.
- [ ] Créer `ServiceType` dans `entity`.
- [ ] Créer les DTO de service.
- [ ] Créer `GET /api/services`.
- [ ] Créer la page Angular du catalogue.

## Fiche à utiliser après chaque changement

```markdown
### AAAA-MM-JJ — Titre du changement

- **Responsable :** Nom ou personne A/B
- **Fonctionnalité :** Auth / Services / Demandes / Administration / Frontend
- **Changements backend :** fichiers, endpoints, règles métier
- **Changements frontend :** pages, composants, services
- **Base de données :** tables, colonnes ou données de test
- **Validation :** commande, Postman, parcours manuel ou test réalisé
- **Résultat :** terminé, en cours ou bloqué
- **Prochaine action :** tâche suivante
```

## Décisions à conserver

- Le MVP commence par le parcours client et le traitement d'une demande.
- Le backend reste la source de vérité pour les permissions.
- Les DTO sont utilisés pour les échanges API.
- Les entités JPA restent dans `entity` et ne sont pas exposées directement.
- Les écrans Angular sont organisés par fonctionnalité dans `features`.
- Les fonctions paiement, WhatsApp, PMS réel, IA et multi-établissement sont reportées.
