# Contribuer à PodWatch

## Workflow

1. Une user story (`PW-xxx`) est décrite dans [`BACKLOG.md`](BACKLOG.md) avec ses critères d'acceptation.
2. Une branche est créée depuis `main` :
   - `feature/PW-<id>-<slug>` pour une fonctionnalité,
   - `fix/PW-<id>-<slug>` pour une correction,
   - `chore/<slug>` / `docs/<slug>` pour l'outillage ou la documentation.
3. Une Pull Request est ouverte avec le modèle fourni (US liée, tests réalisés, checklist sécurité).
4. La CI doit être verte (backend, frontend, image Docker + scan).
5. Revue et validation QA (rapport dans `TEST_REPORT.md` à partir de la Phase 2).
6. **Merge par l'owner du repository uniquement.**

## Commits

Format [Conventional Commits](https://www.conventionalcommits.org/) en anglais :

```
feat(pods): add previous container logs
fix(cluster): mark cluster DOWN after 3 failed health checks
docs(adr): record authentication decision
chore(ci): pin actions to commit SHA
```

Types : `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `ci`, `build`.

## Conventions de code

### Java / Spring Boot
- Un package par domaine sous `io.podwatch` (`cluster`, `resource`, `logs`, `security`…).
- Injection par constructeur, classes et méthodes en visibilité minimale (package-private par défaut).
- DTO en `record` Java ; les objets du client Kubernetes ne sortent jamais d'un contrôleur.
- Erreurs d'API au format `ProblemDetail` (RFC 9457) via `GlobalExceptionHandler`.
- Schéma de base modifié **uniquement** par une nouvelle migration Flyway (`V<n>__description.sql`), jamais en éditant une migration existante.
- Tests : `*Test` (unitaires, tranches `@WebMvcTest`…) et `*IT` (intégration, Testcontainers).

### TypeScript / React
- Composants fonctionnels et hooks ; une fonctionnalité = un dossier sous `src/features/`.
- Appels API centralisés dans `src/api/`, données serveur via TanStack Query.
- `npm run lint` et `npm run typecheck` sans erreur.

### Langue
Code, commits et noms techniques en anglais ; documentation et textes de l'interface en français.

## Definition of Done

Une user story est terminée si :
1. Ses critères d'acceptation sont vérifiés.
2. Les tests sont écrits et la CI est verte, sans régression.
3. Aucun secret dans le code, les logs ou les réponses API.
4. Toute action sensible est autorisée **et** auditée.
5. La documentation est à jour (README, ADR, CHANGELOG).
6. La PR est revue et fusionnée par l'owner.

## Protection de la branche `main` (à activer par l'owner)

GitHub → *Settings* → *Branches* → *Add branch ruleset* sur `main` :
- Require a pull request before merging (1 approbation).
- Require status checks to pass : `Backend (Maven verify)`, `Frontend (lint, types, tests, build)`, `Image Docker (build + scan)`.
- Block force pushes ; restrict deletions.
