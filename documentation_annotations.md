# Guide des Annotations et Concepts (Spring Boot & Angular)

Ce document regroupe les explications des différentes annotations et concepts utilisés dans le projet `hotel-services` pour faire communiquer le backend et le frontend.

---

## 1. Backend : Spring Boot (Java)

Spring Boot utilise massivement les **annotations** (`@Nom`) pour configurer les classes sans avoir à écrire de longs fichiers de configuration.

### A. Les Entités (Modèle de base de données)
Fichier : `ServiceType.java`

*   **`@Entity`** : Indique à Spring (et plus précisément à Hibernate/JPA) que cette classe correspond à une table dans la base de données.
*   **`@Table(name = "service_type")`** : (Optionnel) Permet de forcer le nom de la table dans MySQL. Sans ça, la table s'appellerait `service_type` par défaut, mais c'est une bonne pratique de l'expliciter.
*   **`@Id`** : Spécifie que le champ qui suit (ici `Long id`) est la clé primaire (Primary Key) de la table.
*   **`@GeneratedValue(strategy = GenerationType.IDENTITY)`** : Indique que l'ID est généré automatiquement par la base de données (équivalent de `AUTO_INCREMENT` en MySQL).

*(Annotations Lombok)*
*   **`@Getter` / `@Setter`** : Génère automatiquement les méthodes `getId()`, `setName()`, etc., à la compilation pour éviter d'avoir un code trop long.
*   **`@NoArgsConstructor` / `@AllArgsConstructor`** : Génère un constructeur vide (obligatoire pour JPA) et un constructeur avec tous les paramètres.

### B. Le Repository (Accès aux données)
Fichier : `ServiceTypeRepository.java`

*   **`extends JpaRepository<ServiceType, Long>`** : En héritant de cette interface, Spring génère automatiquement toutes les requêtes SQL classiques : `findAll()`, `findById()`, `save()`, `delete()`. `ServiceType` est l'entité, et `Long` est le type de la clé primaire.

### C. Le Contrôleur (API REST)
Fichier : `ServiceTypeController.java`

*   **`@RestController`** : Indique que cette classe gère des requêtes web. Spring sait alors que chaque méthode va retourner directement des données (généralement en JSON) et non pas une page HTML.
*   **`@RequestMapping("/api/service-types")`** : Définit l'URL de base pour toutes les routes de ce contrôleur.
*   **`@CrossOrigin(origins = "http://localhost:4200")`** : **Crucial pour la communication !** Par sécurité, les navigateurs bloquent les requêtes provenant d'un port différent (Angular sur 4200, Spring sur 8090). Cette annotation autorise spécifiquement Angular à appeler le backend.
*   **`@GetMapping`** : Mappe les requêtes HTTP de type `GET` sur cette méthode. Appeler `GET /api/service-types` exécutera cette méthode.

---

## 2. Frontend : Angular (TypeScript)

Angular est structuré autour de **composants** et de **services**, reliés entre eux par un mécanisme appelé "Injection de Dépendances".

### A. Le Service (Communication HTTP)
Fichier : `service-type.service.ts`

*   **`@Injectable({ providedIn: 'root' })`** : Indique qu'il s'agit d'un service (une classe contenant de la logique métier ou des appels API). `providedIn: 'root'` signifie qu'Angular crée une seule instance (Singleton) de ce service pour toute l'application.
*   **`HttpClient`** : C'est l'outil interne d'Angular pour faire des requêtes HTTP (GET, POST, etc.) vers le backend.
*   **`Observable<ServiceType[]>`** : Angular gère l'asynchrone avec des Observables. Contrairement à une simple Promesse, un Observable attend qu'on y "souscrive" (`.subscribe()`) pour réellement envoyer la requête.

### B. Le Composant (Vue et Logique UI)
Fichier : `app.ts`

*   **`@Component(...)`** : Définit que la classe est un composant visuel.
    *   `selector: 'app-root'` : La balise HTML `<app-root></app-root>` sera remplacée par ce composant.
    *   `standalone: true` : Indique que ce composant est indépendant et n'a pas besoin d'un "Module" classique (`app.module.ts`) pour fonctionner. C'est la nouvelle norme sur Angular 14+.
    *   `imports: [CommonModule]` : Permet d'importer des outils comme `ngFor`, les pipes, etc.
*   **`implements OnInit`** : Interface qui oblige à créer la méthode `ngOnInit()`.
*   **`ngOnInit()`** : C'est le cycle de vie du composant. Cette méthode s'exécute automatiquement une seule fois, juste après que le composant se soit affiché à l'écran. C'est le meilleur endroit pour lancer des requêtes API (au lieu du `constructor`).
*   **`signal<...>`** : *(Nouveauté Angular 16+)* C'est une "boîte" qui contient tes données. Quand tu modifies la donnée à l'intérieur (avec `.set()`), Angular est instantanément averti et met à jour l'écran de façon ultra-optimisée, sans avoir à vérifier toute la page.

### C. Le HTML (Template)
Fichier : `app.html`

*   **`@for (item of liste; track item.id)`** : C'est la nouvelle syntaxe Angular 17+ (qui remplace l'ancien `*ngFor`). Elle permet de boucler sur un tableau pour générer du HTML. `track` est obligatoire pour les performances (il aide Angular à repérer quel élément a été modifié ou supprimé).
*   **`@empty`** : Bloc qui s'affiche automatiquement si la liste est vide.
*   **`{{ s.name }}`** : L'interpolation. Permet d'afficher la valeur d'une variable TypeScript directement dans le texte HTML.

---

## 3. Comprendre la boucle (Le cycle de la donnée)

1. **La base de données (MySQL)** stocke la ligne `1 | Room Service | Restauration`.
2. **Spring Boot (Repository)** lit cette ligne et la transforme en Objet Java `ServiceType`.
3. **Spring Boot (Controller)** reçoit une requête `GET`, demande au Repository toutes les données, et les transforme en texte JSON (`[{"id":1, ...}]`) qu'il renvoie via le port 8090.
4. **Angular (Service)** envoie la requête `GET` grâce à `HttpClient`.
5. **Angular (Composant)** s'abonne (`subscribe`) au résultat de cette requête. Dès que le JSON arrive, il le stocke dans sa variable `services`.
6. **Angular (HTML)** détecte que la variable `services` a changé et exécute sa boucle `@for` pour générer des balises `<li>` à l'écran.
