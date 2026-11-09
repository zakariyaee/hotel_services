# Fonctionnalités de l’application Hotel Services

> Inventaire fonctionnel établi à partir du code présent dans le dépôt le 9 novembre 2026. Les fonctionnalités sont décrites selon leur implémentation actuelle ; la présence d’un écran ou d’une route ne garantit pas à elle seule que tous les scénarios ont été testés en exécution.

## 1. Présentation

Hotel Services est une application web de gestion des services hôteliers. Elle relie les clients en séjour, les employés de terrain, les superviseurs, la réception et l’administration autour de demandes de service, de réservations, d’affectations et de notifications.

### Technologies du projet

- **Frontend :** Angular, composants standalone et interface responsive.
- **Backend :** Java et Spring Boot, API REST.
- **Persistance :** Spring Data JPA / Hibernate avec base relationnelle configurée dans le backend.
- **Sécurité :** authentification JWT, rôles et routes protégées.
- **Notifications :** API de notifications et WebSocket natif pour les événements temps réel.

## 2. Profils et accès

| Profil | Accès fonctionnel présent |
|---|---|
| **Client** | Connexion par code de séjour, consultation de ses demandes et création d’une demande de service. |
| **Employé / STAFF** | Consultation des tâches affectées, accès au pool disponible, prise en charge et clôture d’une tâche. |
| **Personnel (superviseur)** | Consultation des demandes supervisées, vue de l’équipe, affectation des demandes et alertes de retard. |
| **Réceptionniste** | Tableau de bord de réception, consultation filtrable des demandes, consultation des clients en séjour, création de demande pour un client et actions de rejet/affectation exposées par l’API. |
| **Administrateur** | Gestion des services, départements, utilisateurs, affectations aux départements et création/consultation de réservations avec pass numérique. |

Les rôles sont vérifiés côté frontend par des guards et côté backend par Spring Security. Les rôles présents dans le modèle sont `ADMIN`, `PERSONNEL`, `EMPLOYE`, `RECEPTIONNISTE`, `STAFF` et `CLIENT`.

## 3. Fonctionnalités par espace

### 3.1 Accueil et authentification

- Page d’accueil et page de connexion.
- Connexion du personnel avec email et mot de passe.
- Connexion client avec un code associé au séjour.
- Émission et utilisation d’un jeton JWT pour les appels authentifiés.
- Redirection vers l’espace associé au rôle et protection des pages privées.
- Vérification de l’expiration/révocation du code client et de la période de séjour lors de la connexion.

### 3.2 Espace client

- Affichage d’un tableau de bord client.
- Présentation de cartes de services permettant d’initier une demande.
- Formulaire de demande avec choix du moment/priorité et champ de précision.
- Enregistrement et affichage des demandes du client, de leur référence et de leur statut.
- Affichage du détail d’une demande dans une fenêtre de consultation.
- Réception de notifications associées aux demandes via le centre de notifications.

**Limites connues :** le catalogue de cartes côté client contient des services prédéfinis plutôt que d’être entièrement construit depuis les services/départements du backend. Le champ de précision est affiché dans le formulaire, mais son transport et son enregistrement doivent être vérifiés : le DTO de création actuel ne garantit pas sa conservation. Le contrôle qu’un séjour est encore actif avant chaque création de demande n’est pas appliqué de façon systématique dans le service de demande. L’annulation côté client n’est pas proposée comme action complète dans l’interface.

### 3.3 Espace employé

- Consultation des tâches affectées à l’employé connecté.
- Consultation du pool de demandes disponibles.
- Prise en charge d’une demande admissible.
- Passage d’une tâche à l’état terminé.
- Enregistrement des dates de prise en charge/résolution et calcul du temps de traitement lorsque le flux le permet.
- Notifications lors d’événements de tâche.

La disponibilité affichée par le superviseur est calculée à partir du nombre de tâches actives ; elle ne correspond pas à un statut de disponibilité que l’employé peut librement activer/désactiver dans son espace.

### 3.4 Espace Personnel / superviseur

- Liste des demandes du département à superviser.
- Liste des membres de l’équipe et indication calculée « disponible/occupé ».
- Affectation d’une demande à un employé de terrain depuis une fenêtre de sélection.
- Vue des alertes de demandes dépassant le délai prévu.
- Notifications lors des événements d’affectation et de traitement.

L’affectation met à jour l’employé assigné et le statut de la demande, puis tente d’enregistrer une ligne d’historique dans `affectation`.

**Problème opérationnel déjà observé :** les logs fournis montrent que la base de données refuse `NULL` dans `affectation.id_receptionniste`, alors que l’affectation par un superviseur n’a pas de réceptionniste à renseigner. Le schéma réel de la base doit être aligné avec le mapping JPA (colonne nullable) ou l’historique doit être modélisé autrement. Le code masque actuellement certaines erreurs d’écriture de cet historique, ce qui rend le diagnostic plus difficile.

### 3.5 Espace Réception

- Indicateurs globaux : volume de demandes, demandes en attente/en cours/terminées, retards et chambres occupées.
- Liste transversale des demandes avec recherche et filtres par département/statut.
- Liste des clients et chambres ayant un séjour actif.
- Création d’une demande de service pour un client depuis la réception.
- Endpoints backend de rejet avec motif et d’affectation à un employé.

Les actions de rejet et d’affectation sont présentes dans l’API ; leur exposition et leur intégration complète dans l’écran Réception doivent être confirmées selon les contrôles réellement visibles dans l’interface.

### 3.6 Espace Administration

Les onglets visibles couvrent les domaines suivants :

- **Séjours et pass numériques :** création d’une réservation avec données client, dates et chambre ; affichage des réservations ; génération d’un code d’accès, d’un QR code/pass numérique et envoi d’informations par email selon la configuration.
- **Services :** création, consultation, modification et suppression ; activation/désactivation de la disponibilité ; association à un département et configuration des informations de service.
- **Départements :** création, consultation, modification et suppression ; affichage des personnels et employés affectés.
- **Utilisateurs :** création, consultation, modification et suppression de comptes ; choix du rôle ; affectation/retrait d’un département et indication de poste.

**Limites connues :** le CRUD complet des chambres n’est pas fourni dans l’espace d’administration. La gestion des caractéristiques de service et un tableau de statistiques avancées ne sont pas présents comme modules complets.

## 4. Demandes et cycle de traitement

Les demandes portent notamment une référence, un service, un client, une priorité, un statut, des informations de traitement et, lorsqu’elle est renseignée, un employé assigné. Les statuts déclarés par le projet comprennent `EN_ATTENTE`, `PRISE_EN_CHARGE`, `EN_COURS`, `TERMINEE`, `REJETEE` et `ANNULEE` (ainsi que les autres valeurs définies par l’énumération du code).

Actions prises en charge par les services backend :

- création d’une demande par le client ou par la réception ;
- consultation des demandes et de celles du client ;
- affectation par le personnel autorisé ;
- auto-prise en charge d’une demande admissible par un employé ;
- clôture d’une tâche et calcul du temps de traitement ;
- rejet par la réception avec motif ;
- mise à jour et suppression via l’API générique des demandes.

La séquence métier n’est pas uniformément appliquée dans toutes les interfaces : certaines actions passent directement à `EN_COURS`, et l’annulation client n’est pas entièrement disponible dans l’interface.

## 5. Notifications

- WebSocket configuré sur `/ws/notifications`.
- Événements envoyés pour les nouvelles demandes, affectations, prises en charge, clôtures, rejets et annulations selon le flux.
- Centre de notifications frontend avec badge de non-lus, historique, marquage individuel et marquage de toutes les notifications comme lues.
- API REST pour récupérer les notifications, compter les non-lues et les marquer comme lues.
- Persistance des notifications dans la base de données.

Le WebSocket implémenté est un handler natif, pas un broker STOMP. La connexion dépend notamment de la configuration réseau et du jeton utilisé par le frontend ; une erreur WebSocket peut affecter le temps réel sans être la cause d’un échec d’écriture REST.

## 6. Principales routes de l’interface

| Route | Espace |
|---|---|
| `/` | Accueil |
| `/login` | Connexion client ou personnel |
| `/client` | Tableau de bord client |
| `/staff/employe` | Tableau de bord employé |
| `/staff/personnel` | Tableau de bord superviseur |
| `/staff/reception` | Tableau de bord réception |
| `/admin` | Administration |
| `/staff` | Redirection selon le profil |

## 7. API backend principale

Les routes ci-dessous sont déclarées dans les contrôleurs. L’accès effectif dépend aussi des règles de sécurité et des rôles associés.

| Domaine | Routes principales |
|---|---|
| Authentification | `POST /api/auth/login`, `POST /api/auth/guest-access` |
| Demandes | `/api/demandes` : `GET`, `POST`, `PUT /{id}`, `DELETE /{id}`, consultation par client et par identifiant |
| Employé | `/api/staff/employe/my-tasks`, `/available-pool`, actions `/{id}/claim` et `/{id}/complete` |
| Superviseur | `/api/staff/personnel/department-tasks`, `/team`, `/alerts`, `POST /tasks/{id}/assign` |
| Réception | `/api/staff/reception/overview`, `/demandes`, `/room-guests`, création, rejet et affectation |
| Services | `/api/services` : lecture, création, modification, suppression, disponibilité et filtrage par département |
| Départements | `/api/departements` : lecture, création, modification et suppression |
| Utilisateurs admin | `/api/admin/utilisateurs` : CRUD et affectation/retrait de département |
| Réservations admin | `/api/admin/reservations` : lecture, détail et création |
| Notifications | `/api/notifications`, `/unread-count`, `PATCH /{id}/read`, `PATCH /mark-all-read` |
| Chambres | `/api/chambres` : lecture, détail et création |

## 8. Éléments incomplets ou à surveiller

- Le schéma SQL existant et les mappings JPA peuvent diverger, notamment pour `affectation.id_receptionniste` ; c’est une cause d’échec observée lors d’une affectation.
- L’historique d’affectation est tenté, mais les exceptions sont ignorées dans le service et la cohérence transactionnelle est à revoir.
- Les précisions du formulaire client, l’éligibilité de séjour à la création de demande et l’annulation client nécessitent une vérification/complétion.
- Le catalogue client est en partie prédéfini au lieu d’être intégralement piloté par le catalogue backend.
- Les écrans CRUD des chambres et des caractéristiques de services ne sont pas complets.
- Les statistiques détaillées par service/département et le temps moyen ne constituent pas un module d’analyse complet.
- L’expiration des accès repose sur les vérifications au moment de la connexion ; aucun traitement périodique général de check-out n’est confirmé par les fonctionnalités décrites ici.
- Le dépôt contient peu de tests automatisés couvrant les parcours métier de bout en bout. Ce document est un inventaire de code, pas un rapport de validation d’exécution.

## 9. Conclusion

Le projet possède un socle fonctionnel couvrant l’authentification par profil, les demandes de service, les tableaux de bord opérationnels, la configuration des services/départements/utilisateurs, les réservations avec pass numérique et les notifications. Plusieurs parcours sont complets au niveau interface/API, tandis que d’autres restent partiels ou nécessitent une vérification en base et en exécution. Le problème d’affectation observé doit notamment être corrigé en alignant le schéma SQL de `affectation` sur la logique d’affectation par Personnel.
