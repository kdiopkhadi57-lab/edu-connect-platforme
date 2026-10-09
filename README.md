# 🎓 EduConnect

Réseau social dédié à l'éducation — **lycée, enseignement supérieur et formation professionnelle**.
Élèves, étudiants, apprenants et enseignants de **toutes les écoles et universités** y partagent
cours, vidéos, exercices corrigés et sujets d'examen, organisés par **domaine, niveau et série**.

## Le principe : une adoption automatique

À l'inscription, l'utilisateur indique son parcours (domaine, niveau, série, établissement).
Il est alors **inscrit d'office** dans les communautés correspondantes, partagées entre toutes les écoles :

| Communauté automatique         | Exemple                              |
|--------------------------------|--------------------------------------|
| Domaine — tous niveaux         | Informatique — tous niveaux          |
| Domaine · Niveau               | Informatique · Licence 2             |
| Série · Niveau (lycée)         | Série S2 · Terminale                 |
| Établissement                  | Université Cheikh Anta Diop          |

Son fil « Pour toi » est immédiatement rempli par les ressources de pairs qui suivent le même parcours,
même s'ils sont dans une autre ville ou un autre établissement. Quand il change de niveau, ses communautés
sont mises à jour automatiquement.

## Fonctionnalités

- **6 types de publications** : cours/document, vidéo, exercice (corrigé masqué), sujet d'examen, question, annonce
- **Pièces jointes** : PDF, Office, images, vidéos (lecture en streaming), audio, archives, code — 200 Mo/fichier
- **Vidéos** : fichier hébergé ou lien YouTube / Vimeo intégré
- **Fil personnalisé** : communautés + domaine/niveau + personnes suivies
- **Explorer** : filtres par cycle (lycée / pro / supérieur), domaine, niveau, série, type, mots-clés ; tri récents/populaires
- **Interactions** : j'aime, commentaires, enregistrement pour les révisions, partage, compteur de téléchargements
- **Social** : profils, abonnements, communautés libres (groupes de révision, clubs)
- **Responsive** (barre de navigation mobile) et thème clair/sombre automatique

## Stack

| Couche    | Technologies                                                                 |
|-----------|------------------------------------------------------------------------------|
| Backend   | Java 21+, Spring Boot 3.5, Spring Security (JWT), Spring Data JPA, Validation |
| Base      | H2 (dev, zéro configuration) ou PostgreSQL (profil `postgres`)               |
| Frontend  | Angular 20 (composants standalone, signals, nouveau control flow)            |

## Démarrage

### Backend
```bash
cd backend
mvn spring-boot:run
# API sur http://localhost:8080 — base H2 dans ./data, fichiers dans ./uploads
```

Avec PostgreSQL :
```bash
docker compose up -d
cd backend
SPRING_PROFILES_ACTIVE=postgres APP_JWT_SECRET="<au moins 32 caractères>" mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npx ng serve            # http://localhost:4200 (proxy /api → :8080)
```

### Comptes de démo (profil par défaut)
Mot de passe : `password123`

| E-mail             | Profil                                       |
|--------------------|----------------------------------------------|
| awa@demo.sn        | Étudiante L2 Informatique — UCAD             |
| moussa@demo.sn     | Étudiant L2 Informatique — UGB               |
| fatou@demo.sn      | Élève Terminale S2 Mathématiques             |
| ibrahima@demo.sn   | Apprenant BTS Électrotechnique               |
| prof@demo.sn       | Enseignant Informatique — UCAD               |

## API REST (extraits)

| Méthode | Route                                   | Rôle                                   |
|---------|-----------------------------------------|----------------------------------------|
| POST    | `/api/auth/register`, `/api/auth/login` | Inscription / connexion (JWT)          |
| GET     | `/api/reference`                        | Domaines, niveaux, séries, types       |
| GET     | `/api/posts/feed`                       | Fil « Pour toi »                       |
| GET     | `/api/posts?fieldId&level&cycle&serie&type&q&sort` | Exploration                 |
| POST    | `/api/posts` (multipart `data` + `files`) | Publier                              |
| POST/DELETE | `/api/posts/{id}/like`, `/bookmark` | Interactions                           |
| GET/POST | `/api/posts/{id}/comments`             | Commentaires                           |
| GET     | `/api/files/{nom}?download=true`        | Lecture / téléchargement (Range OK)    |
| GET     | `/api/communities/mine`                 | Mes communautés automatiques           |
| PUT     | `/api/users/me`                         | Mise à jour du parcours                |
| POST/DELETE | `/api/users/{id}/follow`            | Abonnements                            |

## Architecture

```
backend/src/main/java/com/educonnect
├── domain/        Entités JPA (User, Post, Community, Attachment, Comment…) et énumérations
├── repository/    Spring Data (fil, exploration par Specification)
├── service/       Métier : CommunityService (adhésion automatique), PostService, UserService, StorageService
├── security/      JWT (filtre + service)
├── web/           Contrôleurs REST + DTO
└── config/        Sécurité, CORS, données initiales

frontend/src/app
├── core/          Modèles, ApiService, AuthService (signals), intercepteur, guards
├── layout/        Shell (barre haute, menu latéral, navigation mobile)
├── shared/        PostCard, Avatar, pipes
└── pages/         login, register, feed (Pour toi / Explorer), post-new, post-detail, profile, communities…
```

## Pistes pour la suite

- Notifications temps réel (WebSocket/STOMP) : nouveau commentaire, réponse à une question
- Messagerie privée et salons de révision
- Stockage objet (S3 / MinIO) et CDN pour les vidéos, génération de miniatures PDF
- Modération : signalement, badges « enseignant vérifié », validation des corrigés
- Recherche plein texte (PostgreSQL `tsvector` ou Elasticsearch)
- Gamification : points d'entraide, meilleures réponses
- Application mobile (Ionic/Capacitor à partir du code Angular)
- Migrations de schéma avec Flyway avant la mise en production
