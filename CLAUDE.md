# PodWatch — instructions pour Claude Code

Plateforme web de supervision et d'exploitation Kubernetes PREPROD/PROD. Owner : sleepy-clemzer (développeur backend Java/Spring Boot).

## À lire avant toute tâche
`docs/ARCHITECTURE.md` (architecture et décisions), `docs/BACKLOG.md` (US et statuts), `docs/CONTRIBUTING.md` (conventions, DoD), `docs/adr/`.

## Rôles (sous-agents dans `.claude/agents/`)
| Agent | Rôle | Peut modifier |
|---|---|---|
| `project-manager` | US, priorités, suivi, releases | `docs/**`, `CHANGELOG.md`, `README.md` |
| `developer` | Code backend/frontend + tests, PR | `backend/`, `frontend/` |
| `devops` | Docker, CI/CD, Helm, RBAC K8s, runbooks | `Dockerfile`, `docker-compose.yml`, `.github/`, `helm/`, `infrastructure/` |
| `qa-tester` | Tests, rapports, acceptation/refus | tests, `docs/TEST_*.md` |

Ces agents sont **invoqués par une session Claude Code** ; ils ne tournent pas de manière autonome.

## Workflow
chef de projet (US) → developer et/ou devops (branche + PR) → CI verte → qa-tester (rapport, verdict) → chef de projet (contrôle) → **merge par l'owner humain uniquement**.

## Règles non négociables
- Lecture seule par défaut ; aucune opération destructive ni aucun déploiement sur PROD sans approbation explicite de l'owner.
- Aucun secret, token ou kubeconfig dans le code, les logs, les tests ou les réponses API ; aucun credential côté frontend.
- Pas de compte Kubernetes administrateur global ; le compte de lecture n'accède pas aux `secrets`.
- Ne pas changer une technologie imposée sans l'accord de l'owner.
- Ne jamais inventer un résultat de test ou prétendre qu'une intégration fonctionne sans vérification (commande exécutée ou lien CI).

## Commandes
```bash
docker compose up -d postgres                                   # base locale
cd backend && ./mvnw verify                                     # build + tests (Docker requis pour *IT)
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd frontend && npm ci && npm run lint && npm run typecheck && npm test && npm run build
docker compose --profile app up --build                         # image complète sur :8080
```
