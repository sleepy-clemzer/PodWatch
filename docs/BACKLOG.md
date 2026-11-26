# BACKLOG — PodWatch

> Responsable : rôle **Chef de projet**. Statut : **proposition à valider**.
> Priorité : **P0** = MVP bloquant · **P1** = MVP · **P2** = post-MVP important · **P3** = plus tard.
> Estimation en points (1, 2, 3, 5, 8, 13). Repère : 1 pt ≈ une demi-journée d'effort assisté, tests compris.
> Statut : `TODO` · `IN PROGRESS` · `IN REVIEW` · `QA` · `DONE` · `REJECTED`.

## Vue des epics

| Epic | Titre | Phase | Priorité | Points |
|---|---|---|---|---|
| E1 | Fondations | 1 | P0 | 26 |
| E2 | Connexion multi-cluster | 2 | P0 | 29 |
| E3 | Authentification minimale | 2 | P0 | 13 |
| E4 | Dashboard | 3 | P1 | 21 |
| E5 | Ressources K8s (MVP) | 3 | P1 | 21 |
| E6 | Détections internes | 3 | P1 | 8 |
| E7 | Logs | 4 | P1 | 26 |
| E8 | Monitoring Prometheus | 5 | P2 | 26 |
| E9 | Loki & Alertmanager | 5 | P2 | 16 |
| E10 | Sécurité complète & audit | 6 | P2 | 26 |
| E11 | Actions Run | 6bis | P2 | 35 |
| E12 | Ressources K8s (complètes) | 6bis | P2 | 21 |
| E13 | Livraison (Helm, CD) | 7 | P2 | 34 |
| **MVP (E1–E7)** | | | | **144** |

---

## E1 — Fondations (Phase 1)

> **Statut : IN REVIEW** (PR Phase 1). Écarts assumés, à traiter avant de clore l'epic :
> - **PW-102** : pas encore de Maven Wrapper (`mvnw`) — l'environnement de développement assisté n'avait pas accès à Maven Central pour le générer. Action : `cd backend && mvn -N wrapper:wrapper`, puis committer. La CI utilise `mvn`.
> - **PW-104** : `package-lock.json` non versionné (même raison). La CI le génère et le publie en artefact `frontend-package-lock` : le committer pour passer à `npm ci` (builds reproductibles).
> - **PW-106** : analyse des dépendances = Dependabot + `npm audit` + scan Trivy de l'image (qui couvre les jars embarqués). OWASP Dependency-Check non retenu (lent, clé NVD requise).

### PW-101 · Structure du monorepo · P0 · 2 pts
*En tant que développeur, je veux une structure de repo claire afin de savoir où placer chaque élément.*
- [ ] Dossiers `backend/`, `frontend/`, `docs/`, `infrastructure/`, `helm/`, `tests/`, `.github/`
- [ ] `.gitignore` (Java, Node, IDE, `.env`)
- [ ] `README.md` : présentation, prérequis, démarrage local, liens docs
- [ ] `CHANGELOG.md` initialisé (format Keep a Changelog)
- [ ] Le `README.md` existant est conservé et enrichi

### PW-102 · Squelette backend Spring Boot · P0 · 5 pts
- [ ] Projet Maven avec Wrapper, Java 25, Spring Boot 4.2.x (versions figées dans le `pom.xml`)
- [ ] Starters : web, websocket, security, validation, actuator, data-jpa, test
- [ ] `/actuator/health` répond `UP`
- [ ] Gestion globale des erreurs en `ProblemDetail`
- [ ] Profils `local` et `test` ; aucune valeur sensible dans `application.yml`
- [ ] Un test `@SpringBootTest` de démarrage passe

### PW-103 · PostgreSQL & Flyway · P0 · 3 pts
- [ ] `docker-compose.yml` avec PostgreSQL (volume, healthcheck)
- [ ] Migration `V1__init.sql` : `app_user`, `role`, `user_role(environment)`, `audit_event`
- [ ] Test d'intégration Testcontainers qui applique les migrations

### PW-104 · Squelette frontend · P0 · 5 pts
- [ ] React + TypeScript + Vite, ESLint, Prettier
- [ ] Bibliothèque UI choisie, layout (menu latéral, sélecteur environnement/cluster)
- [ ] React Router, TanStack Query, client API typé
- [ ] Proxy Vite vers le backend en dev
- [ ] Un test Vitest passe

### PW-105 · Image Docker unique · P0 · 3 pts
- [ ] Dockerfile multi-stage : build front → build back → image JRE minimale, utilisateur non-root
- [ ] Le frontend est servi par Spring Boot (fallback SPA sur `index.html`)
- [ ] `docker run` démarre et `/actuator/health` = UP

### PW-106 · CI Pull Request v1 · P0 · 5 pts
- [ ] Workflow `ci.yml` sur `pull_request` : `./mvnw verify`, `npm ci && npm test && npm run build`
- [ ] Build de l'image Docker (sans push)
- [ ] Analyse des dépendances (Dependabot et/ou OWASP Dependency-Check 🟡) et scan de l'image (Trivy 🟡)
- [ ] Rapports de tests publiés en artefacts
- [ ] Protection de `main` documentée (à activer par l'owner)

### PW-107 · Conventions de contribution · P1 · 3 pts
- [ ] Modèle de PR (US liée, tests, captures, checklist sécurité)
- [ ] `docs/CONTRIBUTING.md` : branches, commits, DoD
- [ ] `docs/adr/` initialisé avec les ADR validés en Phase 0

---

## E2 — Connexion multi-cluster (Phase 2)

### PW-201 · Configuration des clusters · P0 · 3 pts
*En tant qu'opérateur, je veux déclarer mes clusters PREPROD et PROD dans une configuration afin que PodWatch s'y connecte.*
- [ ] `podwatch.clusters[]` typé (`id`, `displayName`, `environment`, `apiServerUrl`, `credentials.tokenFile`, `caFile`, `operations.enabled`)
- [ ] Validation au démarrage (id unique, environnement obligatoire, fichiers présents) avec un message clair en cas d'erreur
- [ ] `operations.enabled` vaut `false` par défaut
- [ ] Aucun token n'apparaît dans les logs, même en DEBUG (test)

### PW-202 · Registre de clients K8s · P0 · 5 pts
- [ ] Un `ApiClient` par cluster, avec timeouts et TLS vérifié (pas d'option « insecure » en PROD)
- [ ] Support du mode *in-cluster*
- [ ] Tests unitaires du registre

### PW-203 · Health-check des clusters · P0 · 3 pts
- [ ] Vérification périodique ; états `UP` / `DEGRADED` / `DOWN`, avec la cause et l'horodatage
- [ ] `GET /api/v1/clusters` : liste, environnement, état (sans aucune donnée de credential)
- [ ] Indicateur Actuator par cluster

### PW-204 · Manifestes RBAC `podwatch-reader` · P0 · 3 pts
- [ ] `infrastructure/k8s-rbac/reader.yaml` : SA, ClusterRole (get/list/watch, **sans secrets**), ClusterRoleBinding
- [ ] Procédure documentée : création du token et de la CA, création du Secret dans le namespace de PodWatch
- [ ] Test : avec ce SA, `get secrets` est refusé par l'API server

### PW-205 · Informers par cluster · P0 · 8 pts
- [ ] Informers nodes, namespaces, pods, deployments, events
- [ ] Resynchronisation et reprise après une coupure réseau
- [ ] Cluster `DOWN` → données marquées périmées, sans crash
- [ ] Test d'intégration Testcontainers **k3s** : créer un pod puis le voir dans le cache

### PW-206 · API nodes / namespaces / pods · P0 · 5 pts
- [ ] `GET /api/v1/clusters/{id}/nodes`, `/namespaces`, `/namespaces/{ns}/pods`, `/namespaces/{ns}/pods/{name}`
- [ ] Vue multi-cluster `GET /api/v1/pods?environment=&cluster=&namespace=&search=&status=` avec `unavailableClusters`
- [ ] Pagination et tri ; DTO uniquement (aucun objet K8s brut)
- [ ] Champs pod : nom, namespace, cluster, node, IP, image(s), statut (raison incluse, ex. CrashLoopBackOff), création, âge, restarts, containers, état des probes, workload parent

### PW-207 · UI clusters & pods · P0 · 2 pts
- [ ] Liste des clusters avec un badge d'état
- [ ] Liste des pods avec recherche et filtres

---

## E3 — Authentification minimale (Phase 2)

### PW-301 · Authentification · P0 · 8 pts
*En tant que responsable sécurité, je veux qu'aucune donnée de cluster ne soit visible sans authentification.*
- [ ] Option validée : OIDC **ou** comptes locaux BCrypt
- [ ] Toutes les routes `/api/**` et WebSocket exigent une authentification (test : 401)
- [ ] Session par cookie HttpOnly, Secure, SameSite ; CSRF actif
- [ ] Page de login et déconnexion

### PW-302 · ReadOnly par défaut · P0 · 3 pts
- [ ] Tout utilisateur authentifié sans rôle explicite est READONLY sur tous les environnements
- [ ] Aucun endpoint d'écriture K8s n'existe à ce stade (test d'architecture)

### PW-303 · Bootstrap Admin · P1 · 2 pts
- [ ] Création du premier Admin par variable d'environnement ou Secret au premier démarrage, documentée

---

## E4 — Dashboard (Phase 3)

| ID | Story | Prio | Pts |
|---|---|---|---|
| PW-401 | API d'agrégation : nodes, namespaces, pods (Running/Pending/Failed/CrashLoop), deployments, restarts, par cluster et en global | P1 | 5 |
| PW-402 | Cartes d'état PREPROD / PROD (clusters UP/DOWN, compteurs clés) | P1 | 5 |
| PW-403 | Events K8s récents (Warning en premier), poussés en temps réel via WebSocket | P1 | 5 |
| PW-404 | Filtres environnement, cluster, namespace, application ; état conservé dans l'URL | P1 | 3 |
| PW-405 | Emplacement CPU/RAM globaux (« Prometheus non configuré » tant que E8 n'est pas fait) | P1 | 1 |
| PW-406 | Emplacement alertes actives (alimenté par E6, puis E9) | P1 | 2 |

**Critère commun** : dashboard chargé en moins de 2 s avec environ 1 000 pods sur 2 clusters (mesure en environnement de test, résultat consigné dans TEST_REPORT).

## E5 — Ressources K8s MVP (Phase 3)

| ID | Story | Prio | Pts |
|---|---|---|---|
| PW-501 | Composant générique liste (recherche, tri, filtres, pagination) et détail (onglets) | P1 | 8 |
| PW-502 | Fiche Pod : containers, images, probes, ressources, conditions, workload parent, YAML (lecture) | P1 | 5 |
| PW-503 | Deployments : liste et détail (replicas souhaités/prêts, stratégie, pods liés) | P1 | 3 |
| PW-504 | Nodes et Namespaces : liste et détail (conditions, capacité, taints, labels) | P1 | 3 |
| PW-505 | Events : liste globale filtrable et events par ressource | P1 | 2 |

## E6 — Détections internes (Phase 3)

| ID | Story | Prio | Pts |
|---|---|---|---|
| PW-601 | Moteur de règles sur l'état des informers : CrashLoopBackOff, ImagePullBackOff, Node NotReady, Deployment indisponible, Job en échec, restarts > seuil configurable | P1 | 5 |
| PW-602 | Liste des anomalies, avec lien vers la ressource ; sévérité ; affichage sur le dashboard | P1 | 3 |

## E7 — Logs (Phase 4)

| ID | Story | Prio | Pts |
|---|---|---|---|
| PW-701 | API logs à la demande : container, `tailLines`, `sinceSeconds`, `timestamps`, `previous` | P1 | 3 |
| PW-702 | Streaming WebSocket STOMP : envoi par lots, maximum 5 flux par utilisateur, timeout, fermeture du flux K8s à la déconnexion, autorisation à l'abonnement | P1 | 8 |
| PW-703 | Visionneuse : défilement virtuel, pause/reprise, timestamps, surlignage ERROR/WARN, recherche texte, filtre de niveau | P1 | 8 |
| PW-704 | Logs précédents (après redémarrage), avec un message clair s'ils sont indisponibles | P1 | 2 |
| PW-705 | Téléchargement des logs (`.log`, nom `cluster_ns_pod_container_date`) | P1 | 2 |
| PW-706 | Test de robustesse : 50 flux simultanés, aucune fuite de thread ni de connexion après fermeture (résultat consigné) | P1 | 3 |

---

## Post-MVP (affinage à faire en début de phase)

### E8 — Monitoring Prometheus (Phase 5) · 26 pts
PW-801 Adaptateur Prometheus et catalogue PromQL configurable (5) · PW-802 CPU/RAM par pod (3) · PW-803 par namespace et par node (3) · PW-804 réseau (3) · PW-805 graphiques ECharts et sélection de plage (5) · PW-806 restarts et disponibilité des workloads (3) · PW-807 détections CPU saturé, mémoire élevée, PVC > 85 % (3) · PW-808 liens Grafana (1)

### E9 — Loki & Alertmanager (Phase 5) · 16 pts
PW-901 Adaptateur Loki et filtres structurés traduits en LogQL (5) · PW-902 recherche historique par période dans la visionneuse (5) · PW-903 alertes actives Alertmanager (dashboard et liste) (5) · PW-904 fusion avec les détections internes (dédoublonnage) (1)

### E10 — Sécurité complète & audit (Phase 6) · 26 pts
PW-1001 Rôles par environnement et `PermissionEvaluator` (8) · PW-1002 UI Admin utilisateurs/rôles (5) · PW-1003 journal d'audit et écran de consultation (5) · PW-1004 masquage des valeurs sensibles des ConfigMaps (3) · PW-1005 en-têtes de sécurité et tests de sécurité (accès croisé PREPROD/PROD, CSRF, IDOR) (5)

### E11 — Actions Run (Phase 6bis) · 35 pts
PW-1101 SA `podwatch-operator` et manifestes (3) · PW-1102 confirmation en deux étapes, avec ressaisie du nom en PROD (5) · PW-1103 rollout restart d'un workload (3) · PW-1104 suppression contrôlée d'un pod (3) · PW-1105 comparaison PREPROD/PROD d'un workload (8) · PW-1106 runbooks liés aux types d'anomalie (5) · PW-1107 **exec** (xterm.js, Admin seulement, désactivé par défaut, audit de session) — *soumis à ta validation explicite* (8) · PW-1108 historique et diagnostic d'incident (timeline des events, restarts et alertes d'un workload) (P3, à estimer)

### E12 — Ressources K8s complètes (Phase 6bis) · 21 pts
Grâce au composant générique (PW-501), environ 1 à 2 pts par type : ReplicaSets, StatefulSets, DaemonSets, Services, Ingress, ConfigMaps, PV, PVC, Jobs, CronJobs, HPA, NetworkPolicies.

### E13 — Livraison (Phase 7) · 34 pts
PW-1301 Chart Helm et values par environnement (8) · PW-1302 validation Helm en CI (2) · PW-1303 publication des images (registre 🟡 : GHCR proposé) et tags SemVer/sha (3) · PW-1304 CD PREPROD et smoke tests (5) · PW-1305 CD PROD : environnement protégé, approbation, promotion du même digest (5) · PW-1306 health-check post-déploiement et rollback (5) · PW-1307 documentation d'exploitation (5) · PW-1308 campagne de tests finale (1)

---

## Prochaines US prêtes à démarrer (après validation)
1. PW-101 → PW-102 → PW-103 → PW-104 → PW-105 → PW-106 → PW-107 (Phase 1, environ 26 pts)
