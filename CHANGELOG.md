# Changelog

Toutes les évolutions notables de PodWatch sont consignées ici.
Format : [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) — versionnement : [SemVer](https://semver.org/lang/fr/).

## [Unreleased]

### Added
- Phase 0 : audit, architecture, plan projet, roadmap, backlog (`docs/`).
- ADR-002 (client Kubernetes officiel), ADR-007 (React + Ant Design + ECharts), ADR-008 (image Docker unique).
- Phase 1 — Fondations :
  - Backend Spring Boot 4.2 / Java 25 : santé et sondes Kubernetes (`/actuator/health`), `GET /api/v1/system/info`,
    erreurs au format `ProblemDetail`, sécurité « tout fermé » hors endpoints publics, en-têtes de sécurité (CSP…).
  - PostgreSQL + Flyway : schéma `app_user`, `role`, `user_role` (un rôle par environnement), `audit_event`.
  - Frontend React 19 / TypeScript / Vite 8 / Ant Design 6 : layout, navigation, tableau de bord minimal.
  - Image Docker unique multi-stage, exécutée en non-root.
  - `docker-compose.yml` de développement local.
  - CI GitHub Actions (build, tests, intégration Testcontainers, lint, audit npm, build et scan Trivy de l'image) ;
    actions épinglées par SHA ; Dependabot.
  - Conventions : `CONTRIBUTING.md`, modèle de Pull Request, `.gitattributes`.
  - Guide d'onboarding développeur macOS (Homebrew) / Windows (winget) : `docs/ONBOARDING.md`.
  - Quatre sous-agents Claude Code (`.claude/agents/`) : chef de projet, développeur, **DevOps**, QA ; règles communes dans `CLAUDE.md`.
  - Maven Wrapper (`mvnw`) et `frontend/package-lock.json` versionnés (builds reproductibles).
