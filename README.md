# PodWatch

Plateforme web centralisée de **supervision et d'exploitation Kubernetes** pour les environnements **PREPROD** et **PROD** : état des clusters, pods, workloads, logs, métriques, événements et alertes, avec un contrôle d'accès par environnement et un audit des opérations.

> Statut : **Phase 1 — Fondations** (squelette technique). Aucune connexion Kubernetes pour l'instant (Phase 2).
> Feuille de route : [`docs/ROADMAP.md`](docs/ROADMAP.md).

## Stack

| Couche | Technologies |
|---|---|
| Backend | Java 25, Spring Boot 4.2 (Web MVC, WebSocket, Security, Data JPA, Actuator), Flyway, Maven |
| Frontend | React 19, TypeScript, Vite 8, Ant Design 6, TanStack Query, React Router (ECharts en Phase 5) |
| Données | PostgreSQL 18 (utilisateurs, rôles, audit — aucune donnée Kubernetes stockée) |
| Livraison | Image Docker unique (frontend embarqué dans le jar), GitHub Actions ; Helm en Phase 7 |

Choix et justifications : [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) et [`docs/adr/`](docs/adr/).

## Organisation du repository

```
backend/              API Spring Boot (package io.podwatch, un sous-package par domaine)
frontend/             SPA React + TypeScript (Vite)
docs/                 Audit, architecture, plan, roadmap, backlog, ADR, contribution
.github/              CI GitHub Actions, Dependabot, modèle de PR
Dockerfile            Image unique (build frontend → build backend → JRE non-root)
docker-compose.yml    PostgreSQL local (+ image complète avec --profile app)
```
Les dossiers `infrastructure/`, `helm/` et `tests/` seront créés avec leurs premiers contenus (Phases 2 et 7).

## Prérequis

| Outil | Version | Vérification |
|---|---|---|
| JDK | 25 | `java -version` |
| Maven | 3.9.x | `mvn -v` |
| Node.js | 26 (LTS) | `node -v` |
| Docker | récent, avec Docker Compose | `docker compose version` |

## Démarrage local

### 1. Base de données
```bash
docker compose up -d postgres
docker compose ps          # postgres doit être "healthy"
```

### 2. Backend (port 8080)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
Vérifications :
```bash
curl http://localhost:8080/actuator/health      # {"status":"UP"}
curl http://localhost:8080/api/v1/system/info   # {"name":"podwatch","version":"..."}
curl -i http://localhost:8080/api/v1/clusters   # 403 : tout le reste de l'API est fermé en Phase 1
```
Flyway crée le schéma au démarrage (`backend/src/main/resources/db/migration`).

### 3. Frontend (port 5173)
```bash
cd frontend
npm install
npm run dev
```
Ouvrir http://localhost:5173 — le serveur Vite relaie `/api` vers le backend. Le tableau de bord affiche « Backend connecté ».

### Variante : image complète
```bash
docker compose --profile app up --build
```
Puis http://localhost:8080 (frontend et API servis par la même image).

## Tests

| Commande | Contenu |
|---|---|
| `cd backend && mvn test` | Tests unitaires et tranches Spring (`*Test`) |
| `cd backend && mvn verify` | + tests d'intégration Testcontainers (`*IT`, **Docker requis**) |
| `cd frontend && npm test` | Tests Vitest + Testing Library |
| `cd frontend && npm run lint && npm run typecheck` | Analyse statique |

## Configuration

| Variable | Rôle | Défaut |
|---|---|---|
| `PODWATCH_DB_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://localhost:5432/podwatch` |
| `PODWATCH_DB_USER` | Utilisateur PostgreSQL | `podwatch` |
| `PODWATCH_DB_PASSWORD` | Mot de passe PostgreSQL | vide (profil `local` : `podwatch-local`) |

Aucun secret n'est versionné. En déploiement, ces valeurs proviendront de Secrets Kubernetes (Phase 7).

## Endpoints exposés en Phase 1

| Endpoint | Accès |
|---|---|
| `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` | public (sondes Kubernetes) |
| `GET /api/v1/system/info` | public (nom et version uniquement) |
| `/` et routes de la SPA | public (fichiers statiques, aucune donnée) |
| tout le reste de `/api/**`, `/ws/**`, `/actuator/**` | **refusé (403)** jusqu'à l'authentification (Phase 2) |

## Contribuer

Workflow, conventions de branches et de commits, Definition of Done : [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md).
