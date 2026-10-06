# Plan de développement — Plateforme de services intra-hôteliers

## 1. Objectif du document

Ce document organise le développement du projet à partir du cahier des charges. Le travail est réparti entre deux personnes, avec un enchaînement qui évite de développer une interface avant que son API et ses règles métier soient disponibles.

Le périmètre prioritaire est le MVP :

```text
Authentification → Catalogue des services → Création d'une demande
→ Traitement par le personnel → Suivi client → Historique → Notifications
```

Les fonctions avancées comme le paiement, WhatsApp, l'intégration PMS réelle, l'intelligence artificielle et le multi-établissement complet sont reportées après le MVP.

## 2. Répartition des responsabilités

### Personne A — Backend, base de données et sécurité

- Modèle de données et migrations.
- API REST Spring Boot.
- DTO, validation et gestion des erreurs.
- Services métier et repositories.
- Authentification du personnel et accès client temporaire.
- Rôles et permissions.
- Notifications WebSocket/STOMP.
- Tests unitaires et tests d'intégration backend.

### Personne B — Frontend, UX et intégration visuelle

- Architecture Angular par fonctionnalités.
- Pages et parcours utilisateur.
- Formulaires et validation côté interface.
- Gestion de l'état utilisateur et des rôles.
- Interfaces client, réception, personnel et administration.
- Suivi des demandes et notifications côté interface.
- Responsive design, français/anglais et accessibilité.
- Tests fonctionnels frontend.

### Travail commun

- Valider les contrats JSON de l'API.
- Faire une intégration après chaque fonctionnalité complète.
- Tester le parcours de bout en bout.
- Résoudre les conflits Git et maintenir la documentation.
- Préparer la démonstration et le livrable final.

## 3. Règle de coordination

Pour chaque fonctionnalité, Personne A termine d'abord le contrat backend minimal : endpoint, DTO, règles métier et données de test. Personne B construit ensuite l'écran qui consomme ce contrat. Les deux personnes terminent par une intégration et une vérification du parcours.

```text
Contrat API → Backend fonctionnel → Écran Angular → Intégration → Validation
```

Les DTO et les réponses JSON doivent être validés ensemble avant de commencer l'écran correspondant.

## 4. Ordre général des étapes

### Étape 0 — Stabilisation du socle

**Objectif :** disposer d'un projet démarrable par les deux personnes.

Personne A :

- vérifier la connexion MySQL ;
- vérifier `application.properties` ;
- nettoyer les entités inutilisées ;
- définir les rôles : `CLIENT`, `RECEPTIONIST`, `STAFF`, `ADMIN` ;
- préparer le profil de données de démonstration.

Personne B :

- conserver l'architecture Angular par `core`, `features` et `shared` ;
- vérifier les routes d'accueil, login et espaces utilisateurs ;
- définir les composants réutilisables : bouton, header, carte, badge de statut.

Livrable : projet frontend et backend qui démarrent avec une base de données accessible.

### Étape 1 — Authentification et accès par rôle

**Dépendance :** Étape 0.

Personne A :

- créer l'entité utilisateur ;
- créer les DTO `LoginRequest` et `LoginResponse` ;
- créer `AuthController`, `AuthService` et `AppUserRepository` ;
- ajouter la validation des identifiants ;
- ajouter ensuite Spring Security, BCrypt et JWT ;
- protéger les endpoints selon le rôle.

Personne B :

- terminer la page d'accueil ;
- terminer le formulaire de connexion commun aux acteurs ;
- rediriger selon le rôle ;
- afficher les erreurs de connexion ;
- créer une structure de layout par espace.

Livrable : connexion fonctionnelle avec redirection `CLIENT`, `RECEPTIONIST`, `STAFF` ou `ADMIN`.

### Étape 2 — Modèle métier principal

**Dépendance :** Étape 1 pour associer les données à un utilisateur.

Personne A : créer progressivement les entités et relations suivantes :

```text
User
Role
Department
Room
Reservation
GuestAccessToken
ServiceType
ServiceRequest
RequestStatusHistory
Notification
```

Commencer par :

```text
User → Department → ServiceType → ServiceRequest
```

Ajouter les réservations et les tokens client avant l'accès client temporaire.

Personne B :

- préparer les modèles TypeScript correspondants ;
- préparer les services Angular API ;
- créer les composants génériques de liste, formulaire, statut et chargement.

Livrable : modèle partagé et contrats JSON documentés.

### Étape 3 — Catalogue des services

**Dépendance :** `Department` et `ServiceType` disponibles.

Personne A :

- `GET /api/services` pour les services actifs ;
- endpoints admin de création, modification et désactivation ;
- contrôle : un service désactivé ne peut pas recevoir une nouvelle demande ;
- associer chaque service à un département.

Personne B :

- page catalogue client ;
- filtres par catégorie ou département ;
- affichage du délai estimé et de la description ;
- page de gestion des services pour l'administrateur.

Livrable : l'administrateur gère les services et le client voit uniquement les services actifs.

### Étape 4 — Accès client temporaire

**Dépendance :** `Reservation`, `Room` et `GuestAccessToken`.

Personne A :

- `POST /api/auth/guest-access` ;
- vérifier le code et la réservation active ;
- refuser un code expiré ou une réservation inactive ;
- lier l'accès client à une chambre et une réservation.

Personne B :

- formulaire d'accès client ;
- messages pour code invalide, expiré ou réservation inactive ;
- affichage de la chambre et de la durée du séjour ;
- stockage de la session client côté frontend.

Livrable : un client accède uniquement à son séjour actif.

### Étape 5 — Création d'une demande

**Dépendance :** catalogue, accès client et départements.

Personne A :

- `POST /api/requests` ;
- vérifier que le service est actif ;
- créer une demande avec client, chambre, service, département, statut et date ;
- gérer la priorité et la description ;
- initialiser le statut `CREATED`.

Personne B :

- formulaire de demande ;
- choix du service ;
- champ de description ;
- choix de priorité ;
- confirmation avant l'envoi ;
- écran de confirmation.

Livrable : le parcours client complet de création d'une demande.

### Étape 6 — Tableau de bord du personnel

**Dépendance :** demandes persistées et rôles configurés.

Personne A :

- `GET /api/requests` avec filtrage selon le rôle et le département ;
- `PATCH /api/requests/{id}/status` ;
- `PATCH /api/requests/{id}/assign` ;
- ajouter les notes internes ;
- enregistrer chaque changement dans `RequestStatusHistory`.

Personne B :

- tableau de bord réception ;
- tableau de bord employé de département ;
- filtres par statut, urgence et département ;
- actions prendre en charge, réassigner, terminer ;
- affichage des détails d'une demande.

Livrable : le personnel reçoit et traite une demande sans modifier les demandes auxquelles il n'a pas accès.

### Étape 7 — Suivi client et historique

**Dépendance :** cycle de vie et historique backend.

Personne A :

- `GET /api/requests` limité aux demandes du client connecté ;
- `GET /api/requests/{id}` avec contrôle d'accès ;
- conserver les statuts : `CREATED`, `ACKNOWLEDGED`, `IN_PROGRESS`, `COMPLETED` ;
- gérer `WAITING`, `CANCELLED`, `REJECTED` et `ESCALATED` si nécessaire.

Personne B :

- page demandes en cours ;
- timeline de statut ;
- page historique ;
- bouton d'annulation uniquement si la règle métier l'autorise.

Livrable : le client suit ses demandes et conserve un historique lisible.

### Étape 8 — Notifications temps réel

**Dépendance :** changements de statut stabilisés.

Personne A :

- configurer WebSocket/STOMP ;
- publier une notification lors d'une nouvelle demande ;
- notifier les changements de statut ;
- prévoir un fonctionnement de secours par HTTP.

Personne B :

- service Angular de connexion WebSocket ;
- centre de notifications ;
- badge de notifications non lues ;
- actualisation HTTP si WebSocket indisponible.

Livrable : les acteurs autorisés reçoivent les événements importants.

### Étape 9 — Administration et statistiques

**Dépendance :** utilisateurs, services, départements et demandes disponibles.

Personne A :

- gestion des comptes et rôles ;
- gestion des départements ;
- gestion des services ;
- `GET /api/statistics/requests` ;
- statistiques par période, département, service et statut.

Personne B :

- pages utilisateurs, départements et services ;
- tableau de bord statistiques ;
- graphiques simples et filtres de période.

Livrable : l'administrateur configure la plateforme et le personnel consulte les indicateurs.

### Étape 10 — Qualité et livraison

Travail commun :

- tests unitaires backend et frontend ;
- tests d'intégration API/base de données ;
- tests des quatre parcours principaux ;
- vérification des permissions ;
- vérification responsive smartphone, tablette et desktop ;
- messages d'erreur et validation ;
- français et anglais ;
- documentation API ;
- démonstration finale.

## 5. Ordre des intégrations

| Intégration | Backend prêt | Frontend prêt | Résultat |
|---|---:|---:|---|
| Login multi-rôles | Oui | Oui | Redirection par rôle |
| Catalogue | À faire | À faire | Services actifs visibles |
| Accès client | À faire | À faire | Vérification réservation/code |
| Création demande | À faire | À faire | Demande enregistrée |
| Traitement staff | À faire | À faire | Statut modifiable |
| Suivi client | À faire | À faire | Historique consultable |
| Notifications | À faire | À faire | Temps réel et fallback HTTP |
| Administration | À faire | À faire | Services et comptes gérables |
| Statistiques | À faire | À faire | Indicateurs opérationnels |

## 6. Règles de développement

- Une fonctionnalité doit avoir son DTO, son endpoint, sa logique métier et son écran avant d'être considérée comme terminée.
- Les contrôleurs ne contiennent pas de logique métier.
- Les entités JPA ne sont pas exposées directement par l'API : utiliser des DTO.
- Les permissions sont vérifiées côté backend, même si le frontend masque les boutons.
- Toute modification importante doit être ajoutée dans `docs/SUIVI_DEVELOPPEMENT.md`.
- Chaque intégration doit être testée avec un compte et des données de démonstration.

## 7. État initial du projet

Déjà présent :

- page d'accueil Maison Élan ;
- login Angular ;
- architecture Angular par fonctionnalités ;
- endpoint Spring Boot de login ;
- entité utilisateur ;
- repository, service et controller d'authentification ;
- DTO `LoginRequest` et `LoginResponse` ;
- redirection frontend par rôle `CLIENT`, `STAFF` et `ADMIN`.

Prochaine étape recommandée :

1. sécuriser le login avec BCrypt et JWT ;
2. créer les entités `Department` et `ServiceType` ;
3. développer le catalogue des services ;
4. développer la création d'une demande.
