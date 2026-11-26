# PROJECT PLAN — PodWatch

> Responsable : rôle **Chef de projet**. Statut : **proposition à valider**.

## 1. Vision

Une console web centralisée, sécurisée et auditée pour superviser et exploiter les clusters Kubernetes PREPROD et PROD. C'est l'équivalent web et multi-utilisateurs d'OpenLens/Freelens, enrichi de Prometheus, Loki, Alertmanager, d'un RBAC par environnement et d'outils de Run.

## 2. Objectifs mesurables

| # | Objectif | Indicateur |
|---|---|---|
| O1 | Voir en un écran l'état de PREPROD et PROD | Dashboard chargé en moins de 2 s avec 2 clusters et environ 1 000 pods |
| O2 | Diagnostiquer un pod sans `kubectl` | Détail, events, logs, logs précédents et métriques accessibles en 2 clics maximum depuis le dashboard |
| O3 | Zéro action non autorisée ou non tracée | 100 % des actions Run passent l'autorisation et apparaissent dans l'audit (tests automatisés) |
| O4 | Zéro credential exposé | Aucun token, CA ou contenu de Secret dans les réponses API ou les logs (tests automatisés) |
| O5 | Déploiement reproductible | PREPROD déployé automatiquement par la CD ; PROD par promotion avec approbation humaine |

## 3. Périmètre

### 3.1 MVP (Phases 1 à 4 + authentification minimale)
- Multi-cluster PREPROD/PROD avec health-check.
- Dashboard : compteurs, statuts, events récents, filtres environnement, cluster et namespace.
- Ressources : Nodes, Namespaces, Pods, Deployments, Events (liste, recherche, détail).
- Pods : détail complet, events, logs (à la demande, streaming, `--previous`, téléchargement).
- Détections internes : CrashLoopBackOff, ImagePullBackOff, Node NotReady, Deployment indisponible.
- Authentification et rôle ReadOnly par défaut.
- Docker, CI GitHub Actions.

### 3.2 Hors MVP (phases suivantes)
Autres ressources K8s, Prometheus et graphiques, Loki, Alertmanager, RBAC complet et audit, actions Run, exec, runbooks, comparaison PREPROD/PROD, Helm, CD.

### 3.3 Hors périmètre
- Création ou édition de ressources K8s (`kubectl apply` via l'UI). PodWatch n'est pas un outil de déploiement.
- Remplacement de Grafana : PodWatch y renvoie par des liens.
- Installation de Prometheus/Loki/Alertmanager : PodWatch consomme l'existant.

## 4. Organisation : les quatre rôles IA

### 4.1 Ce qui est réel (et ce qui ne l'est pas)

Les quatre « agents » sont des **sous-agents Claude Code** définis dans le repo (`.claude/agents/`) :
`project-manager`, `developer`, `qa-tester` et `devops` (ajouté le 26/11/2026). Chaque définition fixe le rôle, le périmètre de fichiers modifiables et les règles (par exemple : le QA ne modifie pas le code applicatif, le DevOps ne touche jamais à la PROD sans approbation).

**Ils ne sont pas autonomes** : une session Claude Code (ou cette conversation) les invoque l'un après l'autre selon le workflow ci-dessous. Rien ne s'exécute seul en arrière-plan, et **c'est toi qui valides** : merge des PR, approbation PROD, arbitrages. Les règles communes sont rappelées dans `CLAUDE.md` à la racine.

### 4.2 Responsabilités

| Rôle | Fait | Ne fait pas | Documents / périmètre |
|---|---|---|---|
| Chef de projet | User stories, priorisation, critères d'acceptation, suivi, release notes | Modifier le code ou l'infra | `PROJECT_PLAN.md`, `ROADMAP.md`, `BACKLOG.md`, `CHANGELOG.md` |
| Développeur | Code backend/frontend, tests unitaires et d'intégration, documentation du code, PR | Valider sa propre PR, modifier la CI/CD ou le RBAC K8s | `backend/`, `frontend/`, `README.md`, `docs/adr/` |
| **DevOps** | Docker, docker-compose, CI/CD GitHub Actions, Helm, RBAC Kubernetes de PodWatch, environnements kind/k3d, secrets, déploiements PREPROD/PROD, runbooks et rollback | Déployer ou agir sur PROD sans approbation ; utiliser `cluster-admin` ; modifier le code applicatif | `Dockerfile`, `.github/`, `helm/`, `infrastructure/`, `DEPLOYMENT.md`, `RUNBOOK.md` |
| QA | Plan et cas de test, exécution, rapport, **refus** d'une US non conforme | Corriger le code applicatif | `TEST_PLAN.md`, `TEST_CASES.md`, `TEST_REPORT.md` |

### 4.3 Workflow par user story

```mermaid
flowchart TD
  PM1[Chef de projet : US + critères d'acceptation dans BACKLOG.md] --> DEV1
  DEV1[Dev et/ou DevOps : branche feature/PW-xxx, code / infra + tests] --> PR[Pull Request]
  PR --> CI{CI verte ?}
  CI -- non --> DEV1
  CI -- oui --> QA1[QA : exécute TEST_CASES, rédige TEST_REPORT]
  QA1 -- refus --> DEV1
  QA1 -- accepté --> PM2[Chef de projet : contrôle des livrables + CHANGELOG]
  PM2 --> HUMAN[Toi : review + merge]
```

**Règle d'honnêteté QA** : un test non exécuté est noté « NON EXÉCUTÉ », jamais « OK ». Un test d'intégration K8s qui n'a tourné que sur k3s est noté comme tel, et non « validé en PREPROD ».

## 5. Conventions

| Sujet | Convention |
|---|---|
| Branches | `main` protégée. `feature/PW-<id>-<slug>`, `fix/PW-<id>-<slug>`, `chore/…` |
| Commits | Conventional Commits : `feat(pods): add previous logs`, `fix(cluster): …` |
| PR | Une PR = une US (ou une partie cohérente). Modèle de PR avec US liée, tests réalisés et captures. CI obligatoire, 1 approbation humaine. |
| Versionnement | SemVer `0.x` jusqu'au MVP. Images taguées `X.Y.Z` et `sha-<commit>`. |
| Code Java | Conventions Spring Boot, `record` pour les DTO, injection par constructeur, pas de Lombok 🟡 (les records suffisent) |
| Code TS | ESLint + Prettier, composants fonctionnels, hooks |
| Langue | Code et commits en anglais, documentation en français |

## 6. Definition of Done

Une US est « Done » si :
1. Les critères d'acceptation sont satisfaits et vérifiés par le QA (`TEST_REPORT.md`).
2. Les tests unitaires sont écrits, la CI est verte et aucune régression n'apparaît.
3. Pas de secret dans le code, les logs ou les réponses API.
4. Toute action sensible est autorisée **et** auditée.
5. La documentation est à jour (README, ADR si décision, CHANGELOG).
6. La PR est revue et mergée par toi.

## 7. Gouvernance et comptes rendus

- **Fin de chaque phase** : compte rendu (fait, non fait, risques, décisions à prendre) et validation avant la phase suivante.
- **Décisions d'architecture** : proposées par le Dev, arbitrées par toi, tracées en ADR.
- **PROD** : aucune action ni aucun déploiement sans ton approbation explicite.

## 8. Prérequis de démarrage (Phase 1)

| # | Prérequis | Qui |
|---|---|---|
| P1 | Validation de ce plan, de l'architecture et des 🟡 | Toi |
| P2 | Accès en écriture au repository (branches et PR) | Toi |
| P3 | Versions K8s des clusters, distribution, emplacement de déploiement de PodWatch | Toi |
| P4 | Existence et URL de Prometheus, Loki et Alertmanager par cluster | Toi (peut attendre la Phase 5) |
| P5 | IdP OIDC disponible ou non | Toi (peut attendre la Phase 2) |
